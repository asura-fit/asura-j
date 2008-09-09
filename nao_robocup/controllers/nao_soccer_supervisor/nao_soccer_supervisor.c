//---------------------------------------------------------------------------------------
//  File:         nao_soccer_supervisor.c
//  Project:      Robotstadium, the online robot soccer competition
//  Date:         July 13th, 2008
//  Description:  Supervisor controller for Robostadium/Nao soccer worlds
//                (You do not need to modify this file for the Robotstadium competition)
//                This controller has several purposes: control the current game state,
//                count the goals, display the scores, move robots and ball to kick-off
//                position, simulate the RoboCup game controller by sending data
//                to the players every 500 ms, check "kick-off shots", throw in ball
//                after it left the field, record match video, penalty kick shootout ...
//  Author:       Olivier Michel & Yvan Bourquin, Cyberbotics Ltd.
//---------------------------------------------------------------------------------------

#include "RoboCupGameControlData.h"
#include <stdio.h>
#include <string.h>
#include <device/robot.h>
#include <device/supervisor.h>
#include <device/emitter.h>
#include <math.h>
#include <stdlib.h>
#include <assert.h>

#define X 0
#define Z 1
#define TRANSLATION_X_Z (SUPERVISOR_FIELD_TRANSLATION_X | SUPERVISOR_FIELD_TRANSLATION_Z)

// field dimensions (in meters) and other constants that should match the .wbt file
#define NUM_ROBOTS 6
#define BALL_RADIUS 0.043

static const float CIRCLE_RADIUS = 0.65;   // soccer field's central circle
static const float FIELD_X_LIMIT = 3.0 + BALL_RADIUS;
static const float FIELD_Z_LIMIT = 2.0 + BALL_RADIUS;
static const int TIME_STEP = 40;           // should be a multiple of WorldInfo.basicTimeSTep
static const float MAX_TIME = 10.0 * 60.0; // a match lasts 10 minutes

// robot DEF names as specified in the .wbt files
static const char *ROBOT_DEF_NAMES[NUM_ROBOTS] = {
  "RED_GOAL_KEEPER", "RED_PLAYER_1", "RED_PLAYER_2",
  "BLUE_GOAL_KEEPER","BLUE_PLAYER_1","BLUE_PLAYER_2"
};

// robot indices for penalty kick shoot-out
static const int ATTACKER = 1;
static const int GOALIE = 3;

// global variables
static NodeRef robot[NUM_ROBOTS];       // to track robot positions
static NodeRef ball;                    // to track ball position
static DeviceTag emitter;               // to send game control data to robots
static float ball_pos[2];               // current ball position
static float robot_pos[NUM_ROBOTS][2];  // current robots position
static float time;                      // time [seconds] since end of half game
static int step_count = 0;              // number of steps since the simulation started
static int last_touch_robot_index = -1; // index of last robot that touched the ball
static float throw_in_pos[2];           // x and z throw-in position according to RoboCup rules
static const char *message;             // instant message: "Goal!", "Out!", etc.
static float message_steps = 0;         // steps to live for instant message
static int contest_match = 0;           // 1 for an official contest match at Cyberbotics, 0 otherwise
static int swapped = 0;                 // jersey colors were swapped (red robots use the blue controller and vice-versa)

// team names to be displayed in official contest matches
static char team_names[2][64] = { "blue", "red" };

// RoboCup GameController simulation
static struct RoboCupGameControlData control_data; 

// to enforce the "kick-off shot cannot score a goal" rule:
enum {
  KICK_OFF_INITIAL,      // the ball was just put in the central circle center
  KICK_OFF_LEFT_CIRCLE,  // the ball has left the central circle
  KICK_OFF_OK            // the ball was hit by a kick-off team robot after having left the central circle
};
static int kick_off_state = KICK_OFF_INITIAL;

// this must match the ROBOT_DEF_NAMES array defined above
static int is_red_robot(int robot) {
  return robot <= 2;
}

static int is_blue_robot(int robot) {
  return robot >= 3;
}

static void display() {

  const float FONT_SIZE = 0.08;

  // display team names and current score
  char text[64];
  sprintf(text, "%s - %d", team_names[TEAM_RED], control_data.teams[TEAM_RED].score);
  supervisor_set_label(0, text, 0.05, 0.01, FONT_SIZE, 0xff0000); // red
  sprintf(text, "%d - %s", control_data.teams[TEAM_BLUE].score, team_names[TEAM_BLUE]);
  supervisor_set_label(1, text, 0.99 - 0.025 * strlen(text), 0.01, FONT_SIZE, 0x0000ff); // blue

  // display game state or remaining time
  if (control_data.state == STATE_PLAYING)
    sprintf(text,"%02d:%02d",(int)(time/60),(int)time%60);
  else {
    static const char *STATE_NAMES[5] = { "INITIAL", "READY", "SET", "PLAYING", "FINISHED" };
    sprintf(text, STATE_NAMES[control_data.state]);
  }
  supervisor_set_label(2, text, 0.51 - 0.015 * strlen(text), 0.1, FONT_SIZE, 0x000000); // black

  // display instant message
  if (message_steps > 0)
    supervisor_set_label(3, message, 0.51 - 0.015 * strlen(message), 0.9, FONT_SIZE, 0x000000); // black
  else {
    // remove message
    supervisor_set_label(3, "", 1, 0.9, FONT_SIZE, 0x000000);
    message_steps = 0;
  }
}

static void set_message(const char *msg) {
  message = msg;
  message_steps = 4000 / TIME_STEP;  // show message for 4 seconds
  display();
}

static void sendGameControlData() {
  // prepare and send game control data
  control_data.secsRemaining = (uint32)time;
  if (! contest_match) {
    // ball position is not sent during official matches
    control_data.ballXPos = ball_pos[X];
    control_data.ballYPos = ball_pos[Z];
  }
  emitter_send_packet(emitter, &control_data, sizeof(control_data));
}

// this is called once only when the controller starts
static void reset(void) {
  // initialize game control data
  memset(&control_data, 0, sizeof(control_data));
  memcpy(control_data.header, GAMECONTROLLER_STRUCT_HEADER, sizeof(GAMECONTROLLER_STRUCT_HEADER));
  control_data.playersPerTeam = NUM_ROBOTS / 2;

  // emitter for sending game control data
  emitter=robot_get_device("emitter");

  // get robot handles for getting/setting their positions
  int i;
  for(i=0;i<NUM_ROBOTS;i++) {
    robot[i] = supervisor_node_get_from_def(ROBOT_DEF_NAMES[i]);
    robot_pos[i][X] = robot_pos[i][Z] = -99999.9;
    supervisor_field_get(robot[i], TRANSLATION_X_Z, robot_pos[i], TIME_STEP);
  }

  // to keep track of ball position
  ball = supervisor_node_get_from_def("BALL");
  supervisor_field_get(ball, TRANSLATION_X_Z, ball_pos, TIME_STEP);

  // read teams names from file
  FILE *file = fopen("teams.txt", "r");
  if (file) {
    fscanf(file, "%[^\n]\n%[^\n]", team_names[TEAM_BLUE], team_names[TEAM_RED]);
    fclose(file);
  }

  // variable set during official matches
  if (getenv("WEBOTS_ROBOTSTADIUM")) {
    contest_match = 1;
    robot_console_printf("Running Robotstadium contest match\n");

    // format=640x480, type=MPEG4, quality=75%
    supervisor_start_movie("movie.avi", 640, 480, 0, 75);
  }

  // enable keyboard for manual score control
  robot_keyboard_enable(TIME_STEP * 10);
}

// detect if the ball has hit something (robot goal post, etc.)
// returns: 1 = hit, 0 = no hit
// hit detection is based on a trajectory or velocity change measured
// over the two most recent time steps
static int ball_has_hit_something() {

  // ball position during the 3 last time steps
  // index 2 is the newest, index 0 is the oldest
  static float x[3] = { 0, 0, 0 };  // init to empty history
  static float z[3] = { 0, 0, 0 };

  // shift table down
  x[0] = x[1];
  z[0] = z[1];
  x[1] = x[2];
  z[1] = z[2];
  x[2] = ball_pos[X];
  z[2] = ball_pos[Z];

  // filter noise: if the ball is almost still then forget it
  if (fabs(x[2] - x[1]) < 0.0001 && fabs(z[2] - z[1]) < 0.0001)
    return 0;

  // compute ball direction at time t and t-1
  float dir2_x = x[2] - x[1]; // now
  float dir2_z = z[2] - z[1];
  float dir1_x = x[1] - x[0]; // before
  float dir1_z = z[1] - z[0];

  // compute ball velocity at time t and t-1
  float vel2 = sqrt(dir2_x * dir2_x + dir2_z * dir2_z);  // now
  float vel1 = sqrt(dir1_x * dir1_x + dir1_z * dir1_z);  // before

  // a strong acceleration or deceleration correspond to the ball being hit (or hitting something)
  // however some deceleration is normal because the ball slows down due to the simulated friction
  if (vel2 > vel1 * 1.001 || vel2 < 0.9 * vel1)
    return 1;

  // compute ball direction at time t and t-1
  float angle2 = atan2(dir2_z, dir2_x);  // now
  float angle1 = atan2(dir1_z, dir1_x);  // before

  // measure change in trajectory angle
  float angle_diff = fabs(angle2 - angle1);

  // normalize if one angle is positive and the other negative
  if (angle_diff > M_PI)
    angle_diff = fabs(angle_diff - 2.0 * M_PI);

  // if the direction changed, the ball has hit something
  if (angle_diff > 0.001)
    return 1;

  // no hit
  return 0;
}

static void check_keyboard() {

  // allow to modify score manually
  switch (robot_keyboard_get_key()) {
  case 65618: // R
    if (control_data.teams[TEAM_RED].score > 0) {
      control_data.teams[TEAM_RED].score--;
      display();
    }
    break;

  case 82: // r
    control_data.teams[TEAM_RED].score++;
    display();
    break;

  case 65602: // B
    if (control_data.teams[TEAM_BLUE].score > 0) {
      control_data.teams[TEAM_BLUE].score--;
      display();
    }
    break;

  case 66: // b
    control_data.teams[TEAM_BLUE].score++;
    display();
    break;
  }
}

// this is what we do at every time step
// independently of the game state
static void step() {

  if (message_steps)
    message_steps--;

  // yield control to simulator
  robot_step(TIME_STEP);

  // every 480 milliseconds
  if (step_count++ % 12 == 0)
    sendGameControlData();

  // read key pressed
  check_keyboard();
}

// move robots and ball to kick-off position
static void place_to_kickoff() {
  const float RED_KICK_OFF[NUM_ROBOTS][7] = {
    { 2.950, 0.325, 0.000, 0, 1, 0,-1.57 },
    { 0.625, 0.325, 0.000, 0, 1, 0,-1.57 },
    { 2.000, 0.325, 1.200, 0, 1, 0,-1.57 },
    {-2.950, 0.325, 0.000, 0, 1, 0, 1.57 },
    {-2.000, 0.325,-0.300, 0, 1, 0, 1.57 },
    {-2.000, 0.325, 1.200, 0, 1, 0, 1.57 }
  };
  const float BLUE_KICK_OFF[NUM_ROBOTS][7] = {
    { 2.950, 0.325, 0.000, 0, 1, 0,-1.57 },
    { 2.000, 0.325, 0.300, 0, 1, 0,-1.57 },
    { 2.000, 0.325,-1.200, 0, 1, 0,-1.57 },
    {-2.950, 0.325, 0.000, 0, 1, 0, 1.57 },
    {-0.625, 0.325, 0.000, 0, 1, 0, 1.57 },
    {-2.000, 0.325,-1.200, 0, 1, 0, 1.57 }
  };

  // move robots to kick-off position
  const float *robot_initial_position;
  if (control_data.kickOffTeam == TEAM_RED)
    robot_initial_position=RED_KICK_OFF[0];
  else
    robot_initial_position=BLUE_KICK_OFF[0];

  int i;
  for(i=0;i<NUM_ROBOTS;i++) {
    supervisor_field_set(robot[i],
                         SUPERVISOR_FIELD_TRANSLATION |
                         SUPERVISOR_FIELD_ROTATION,
                         &robot_initial_position[i*7]);
  }

  // reset ball position
  const float ball_initial_position[2]={0.0f, 0.0f};
  supervisor_field_set(ball, TRANSLATION_X_Z, ball_initial_position);
}

// run simulation for the specified number of seconds while sending control data
static void run_seconds(float seconds) {
  int n = 1000.0f * seconds / TIME_STEP;
  int i;
  for (i = 0; i < n; i++) step();
}

static void run_initial_state() {
  time = MAX_TIME;
  control_data.state = STATE_INITIAL;
  display();
  run_seconds(5);
}

static void run_ready_state() {
  control_data.state = STATE_READY;
  display();
  run_seconds(5);
  place_to_kickoff();
}

static void run_set_state() {
  control_data.state = STATE_SET;
  display();
  run_seconds(5);
}

static void run_finished_state() {
  control_data.state = STATE_FINISHED;
  display();
  run_seconds(5);
}

static int is_in_kickoff_team(int robot) {
  if (control_data.kickOffTeam == TEAM_RED && is_red_robot(robot)) return 1;
  if (control_data.kickOffTeam == TEAM_BLUE && is_blue_robot(robot)) return 1;
  return 0;
}

// detect if the ball has hit something and what it was
static void detect_touch() {
  if (ball_has_hit_something()) {

    // find if a robot has hit (or was hit by the) the ball
    float minDist2 = 0.25; // squared robot proximity radius
    int minIndex = -1;
    int i;
    for (i = 0; i < NUM_ROBOTS; i++) {
      float dx = robot_pos[i][X] - ball_pos[X];
      float dz = robot_pos[i][Z] - ball_pos[Z];

      // squared distance between robot and ball
      float dist2 = dx * dx + dz * dz;
      if (dist2 < minDist2) {
        minDist2 = dist2;
        minIndex = i;
      }
    }

    if (minIndex != -1) {
      last_touch_robot_index = minIndex;
      //printf("last touch: %s\n", ROBOT_DEF_NAMES[last_touch_robot_index]);

      // "the ball must touch a player from the kick-off team after leaving the center circle
      // before a goal can be scored by the team taking the kick-off"
      if (kick_off_state == KICK_OFF_LEFT_CIRCLE && is_in_kickoff_team(last_touch_robot_index))
        kick_off_state = KICK_OFF_OK;

      if (kick_off_state == KICK_OFF_INITIAL && ! is_in_kickoff_team(last_touch_robot_index))
        kick_off_state = KICK_OFF_OK;
    }
  }
}

static int is_ball_in_field() {
  return fabs(ball_pos[Z]) <= FIELD_Z_LIMIT && fabs(ball_pos[X]) <= FIELD_X_LIMIT;
}

static int is_ball_in_red_goal() {
  return ball_pos[X] > FIELD_X_LIMIT && ball_pos[X] < FIELD_X_LIMIT + 0.25 && fabs(ball_pos[Z]) < 0.7;
}

static int is_ball_in_blue_goal() {
  return ball_pos[X] < -FIELD_X_LIMIT && ball_pos[X] > -FIELD_X_LIMIT - 0.25 && fabs(ball_pos[Z]) < 0.7;
}

static int is_ball_in_central_circle() {
  return ball_pos[X] * ball_pos[X] + ball_pos[Z] * ball_pos[Z] < CIRCLE_RADIUS * CIRCLE_RADIUS;
}

static float sign(float value) {
  return value > 0.0 ? 1.0 : -1.0;
}

// check if the ball leaves the field and throw ball in if necessary
static void check_ball_out() {
  const float THROW_IN_LINE_Y     = 1.6;
  const float THROW_IN_LINE_END_X = 2.0;
  const float CORNER_KICK_X       = 2.0;
  const float CORNER_KICK_Y       = 1.2;

  if (ball_pos[Z] > FIELD_Z_LIMIT || ball_pos[Z] < -FIELD_Z_LIMIT) {  // out at side line
    // printf("ball over side-line: %f %f\n", ball_pos[X], ball_pos[Z]);
    float back;
    if (last_touch_robot_index == -1)  // not sure which team has last touched the ball
      back = 0.0;
    else if (is_red_robot(last_touch_robot_index))
      back = 1.0;  // 1 meter towards red goal
    else
      back = -1.0;  // 1 meter towards blue goal

    throw_in_pos[X] = ball_pos[X] + back;
    throw_in_pos[Z] = sign(ball_pos[Z]) * THROW_IN_LINE_Y;

    // in any case the ball cannot be placed off the throw-in line
    if (throw_in_pos[X] > THROW_IN_LINE_END_X)
      throw_in_pos[X] = THROW_IN_LINE_END_X;
    else if (throw_in_pos[X] < -THROW_IN_LINE_END_X)
      throw_in_pos[X] = -THROW_IN_LINE_END_X;
  }
  else if (ball_pos[X] > FIELD_X_LIMIT && ! is_ball_in_red_goal()) {  // out at end line
    // printf("ball over end-line (near red goal): %f %f\n", ball_pos[X], ball_pos[Z]);
    if (last_touch_robot_index == -1) {   // not sure which team has last touched the ball
      throw_in_pos[X] = CORNER_KICK_X;
      throw_in_pos[Z] = sign(ball_pos[Z]) * THROW_IN_LINE_Y;
    }
    else if (is_red_robot(last_touch_robot_index)) { // defensive team
      throw_in_pos[X] = CORNER_KICK_X;
      throw_in_pos[Z] = sign(ball_pos[Z]) * CORNER_KICK_Y;
    }
    else { // offensive team
      throw_in_pos[X] = 0.0; // halfway line 
      throw_in_pos[Z] = sign(ball_pos[Z]) * THROW_IN_LINE_Y;
    }
  }
  else if (ball_pos[X] < -FIELD_X_LIMIT && ! is_ball_in_blue_goal()) {  // out at end line
    // printf("ball over end-line (near blue goal): %f %f\n", ball_pos[X], ball_pos[Z]);
    if (last_touch_robot_index == -1) {  // not sure which team has last touched the ball
      throw_in_pos[X] = -CORNER_KICK_X;
      throw_in_pos[Z] = sign(ball_pos[Z]) * THROW_IN_LINE_Y;
    }
    else if (is_blue_robot(last_touch_robot_index)) { // defensive team
      throw_in_pos[X] = -CORNER_KICK_X;
      throw_in_pos[Z] = sign(ball_pos[Z]) * CORNER_KICK_Y;
    }
    else { // offensive team
      throw_in_pos[X] = 0.0; // halfway line 
      throw_in_pos[Z] = sign(ball_pos[Z]) * THROW_IN_LINE_Y;
    }
  }
  else
    return; // ball is not out

  // the ball is out:
  set_message("OUT!");
  kick_off_state = KICK_OFF_OK;

  // let it roll for 2 seconds
  int n = 2000.0 / TIME_STEP;
  int i;
  for (i = 0; i < n; i++)
    step();

  // throw the ball in
  supervisor_field_set(ball, TRANSLATION_X_Z, throw_in_pos);
}

static void run_playing_state() {

  control_data.state = STATE_PLAYING;
  set_message("KICK-OFF!");
  kick_off_state = KICK_OFF_INITIAL;
  last_touch_robot_index = -1;

  while (1) {
    // substract TIME_STEP to current time
    time -= (float)TIME_STEP/1000.0f;
    display();

    if (time < 0.0f) {
      time = 0.0f;
      control_data.state = STATE_FINISHED;
      return;
    }

    detect_touch();

    if (kick_off_state == KICK_OFF_INITIAL && ! is_ball_in_central_circle())
      kick_off_state = KICK_OFF_LEFT_CIRCLE;

    check_ball_out();

    if (is_ball_in_red_goal()) {  // ball in the red goal

      // a goal cannot be scored directly from a kick-off
      if (control_data.kickOffTeam == TEAM_RED || kick_off_state == KICK_OFF_OK) {
        control_data.teams[TEAM_BLUE].score++;
        set_message("GOAL!");
      }
      else
        set_message("KICK-OFF SHOT!");

      control_data.state = STATE_READY;
      control_data.kickOffTeam = TEAM_RED;
      return;
    }
    else if (is_ball_in_blue_goal()) {  // ball in the blue goal

      // a goal cannot be scored directly from a kick-off
      if (control_data.kickOffTeam == TEAM_BLUE || kick_off_state == KICK_OFF_OK) {
        control_data.teams[TEAM_RED].score++;
        set_message("GOAL!");
      }
      else
        set_message("KICK-OFF SHOT!");

      control_data.state = STATE_READY;
      control_data.kickOffTeam = TEAM_BLUE;
      return;
    }

    step();
  }
}

static void run_termination() {

  if (contest_match) {
    FILE *file = fopen("scores.txt", "w");
    if (file) {
      if (! swapped) 
        fprintf(file, "%d\n%d\n", control_data.teams[TEAM_BLUE].score, control_data.teams[TEAM_RED].score);
      else
        fprintf(file, "%d\n%d\n", control_data.teams[TEAM_RED].score, control_data.teams[TEAM_BLUE].score);
      fclose(file);
    }
    else
      robot_console_printf("could not write: scores.txt\n");

    // give some time to show scores
    run_seconds(10);

    // terminate movie recording and quit
    supervisor_stop_movie();
    robot_step(0);
    supervisor_simulation_quit();
  }

  while (1) step();  // wait forever
}

const char *get_controller_name(int robot) {
  if ((is_red_robot(robot) && ! swapped) || (is_blue_robot(robot) && swapped))
    return "nao_soccer_player_red";
  else
    return "nao_soccer_player_blue";
}

// switch controllers, jersey colors and scores
static void swap_teams_and_scores() {

  swapped = swapped ? 0 : 1;
  
  // swap red and blue scores
  int swap = control_data.teams[TEAM_BLUE].score;
  control_data.teams[TEAM_BLUE].score = control_data.teams[TEAM_RED].score;
  control_data.teams[TEAM_RED].score = swap;

  // swap team names
  char tmp[64];
  strcpy(tmp, team_names[0]);
  strcpy(team_names[0], team_names[1]);
  strcpy(team_names[1], tmp);

  // restart controllers so that they can figure out if they are red of blue
  if (control_data.playersPerTeam == 1) {
    // penalty shootout
    supervisor_robot_set_controller(robot[ATTACKER], get_controller_name(ATTACKER));
    supervisor_robot_set_controller(robot[GOALIE], get_controller_name(GOALIE));
  }
  else {
    // half time break
    int i;
    for (i = 0; i < NUM_ROBOTS; i++)
      supervisor_robot_set_controller(robot[i], get_controller_name(i));
  }
}

static void run_half_time_break() {
  set_message("HALF TIME BREAK!");
  step();
  swap_teams_and_scores();
}

static void run_half_time() {

  // first kick-off of the half is always for the reds
  control_data.kickOffTeam = TEAM_RED;

  run_initial_state();

  do {
    run_ready_state();
    run_set_state();
    run_playing_state();
  }
  while (control_data.state != STATE_FINISHED);

  run_finished_state();
}

// randomize initial position for penalty kick shootout
static float randomize_pos() {
  return (float)rand() / (float)RAND_MAX * 0.01 - 0.005;   // +/- 1 cm
}

// randomize initial angle for penalty kick shootout
static float randomize_angle() {
  return (float)rand() / (float)RAND_MAX * 0.1745 - 0.0873;  // +/- 5 degrees
}

static void run_penalty_kick(float delay) {

  // "The ball is placed 1.5m from the center of the field in the direction of the defender's goal"
  const float BALL_TRANS[3] = { -1.5 + randomize_pos(), 0.043, randomize_pos() };
  supervisor_field_set(ball, SUPERVISOR_FIELD_TRANSLATION, BALL_TRANS);

  // "The attacking robot is positioned 50cm behind the ball"
  const float ATTACKER_TRANS_ROT[7] = { -1.0 + randomize_pos(), 0.325, 0.0 + randomize_pos(), 0, 1, 0, -1.57 + randomize_angle() };
  supervisor_field_set(robot[ATTACKER], SUPERVISOR_FIELD_TRANSLATION | SUPERVISOR_FIELD_ROTATION, ATTACKER_TRANS_ROT);

  // "The goalie is placed with feet on the goal line and in the centre of the goal"
  const float GOALIE_TRANS_ROT[7] = { -3.0 + randomize_pos(), 0.325, 0.0 + randomize_pos(), 0, 1, 0, 1.57 + randomize_angle() };
  supervisor_field_set(robot[GOALIE], SUPERVISOR_FIELD_TRANSLATION | SUPERVISOR_FIELD_ROTATION, GOALIE_TRANS_ROT);

  time = delay;
  control_data.kickOffTeam = TEAM_RED;

  run_set_state();

  control_data.state = STATE_PLAYING;
  display();

  do {
    // substract TIME_STEP to current time
    time -= (float)TIME_STEP / 1000.0f;
    display();

    if (time < 0.0f) {
      time = 0.0f;
      set_message("TIME OUT!");
      return;
    }
    step();
  }
  while (fabs(ball_pos[X] - BALL_TRANS[0]) < 0.01 && fabs(ball_pos[Z] - BALL_TRANS[2]) < 0.01);

  set_message("SHOOTING!");

  // ball was hit: give it 10 seconds to reach goal or leave field
  int n = 15000 / TIME_STEP;
  do {
    step();
    n--;
  }
  while (n > 0 && is_ball_in_field());
  
  if (is_ball_in_blue_goal()) {
    control_data.teams[TEAM_RED].score++;
    set_message("GOAL!");
  }
  else
    set_message("MISSED!");
}

static void run_victory(int winner) {
  char message[128];
  sprintf(message, "%s WINS!", team_names[winner]);
  set_message(message);
  run_finished_state();
}

// "A winner can be declared before the conclusion of the penalty shoot-out
// if a team can no longer win, {...}"
static int check_victory(int remaining_attempts) {
  int diff = control_data.teams[TEAM_RED].score - control_data.teams[TEAM_BLUE].score;
  if (diff > remaining_attempts) {
    run_victory(TEAM_RED);
    return 1;
  }
  else if (diff < -remaining_attempts) {
    run_victory(TEAM_BLUE);
    return 1;
  }
  return 0;
}

static void run_penalty_kick_shootout() {

  set_message("PENALTY KICK SHOOT-OUT!");
  step();

  // power off robots and move them out of the field
  int i;
  for (i = 0; i < NUM_ROBOTS; i++) {
    // keep RED_PLAYER_1 and BLUE_GOAL_KEEPER for the shootout
    if (i != ATTACKER && i != GOALIE) {
      // make them zombies and move them away
      supervisor_robot_set_controller(robot[i], "void");
      float out_of_field[2] = { 1.0 * i, 5.0 };
      supervisor_field_set(robot[i], TRANSLATION_X_Z, out_of_field);
    }
  }

  // inform robots of the penalty kick shootout
  // (this is not RoboCup standard !)
  control_data.playersPerTeam = 1;

  // five penalty shots per team
  for (i = 0; i < 5; i++) {
    run_penalty_kick(60);
    if (check_victory(5 - i)) return;
    run_finished_state();
    swap_teams_and_scores();
    run_penalty_kick(60);
    if (check_victory(4 - i)) return;
    run_finished_state();
    swap_teams_and_scores();
  }
  
  set_message("SUDDEN DEATH SHOOT-OUT!");

  // sudden death shots
  while (1) {
    run_penalty_kick(120);
    run_finished_state();
    swap_teams_and_scores();
    run_penalty_kick(120);
    if (check_victory(0)) return;
    run_finished_state();
    swap_teams_and_scores();
  }
}

static int run(int ms) {

  control_data.firstHalf = 1;

  run_half_time();
  run_half_time_break();

  control_data.firstHalf = 0;

  run_half_time();

  if (control_data.teams[TEAM_BLUE].score == control_data.teams[TEAM_RED].score)
    run_penalty_kick_shootout();

  run_termination();

  return 1; // never reached
}

int main() {
  robot_live(reset);
  robot_run(run); // never returns
  return 0;
}
