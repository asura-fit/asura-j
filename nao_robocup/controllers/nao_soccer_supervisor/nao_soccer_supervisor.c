//---------------------------------------------------------------------------------------
//  File:         nao_soccer_supervisor.c
//  Project:      Robotstadium, the online robot soccer competition
//  Date:         April 30, 2008
//  Description:  Supervisor controller for Robostadium/Nao soccer worlds
//                (You do not need to modify this file for the Robotstadium competition)
//                This controller has several purposes: control the current game state,
//                count the goals, display the scores, move robots and ball to kick-off
//                position, simulate the RoboCup game controller by sending data
//                to the players every 500 ms, check "kick-off shots", throw in ball
//                after it left the field, record a match video, ...
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


#define X 0
#define Z 1
#define TRANSLATION_X_Z (SUPERVISOR_FIELD_TRANSLATION_X | SUPERVISOR_FIELD_TRANSLATION_Z)

// field dimensions (in meters) and other constants that should match the .wbt file
#define NUM_ROBOTS 8
#define BALL_RADIUS 0.043

static const float CIRCLE_RADIUS = 0.65;   // soccer field's central circle
static const float FIELD_X_LIMIT = 3.0 + BALL_RADIUS;
static const float FIELD_Y_LIMIT = 2.0 + BALL_RADIUS;
static const int TIME_STEP = 40;           // should be a multiple of WorldInfo.basicTimeSTep
static const float MAX_TIME = 10.0 * 60.0; // a match lasts 10 minutes

static const char *ROBOT_DEF_NAMES[NUM_ROBOTS] = {
  "RED_GOAL_KEEPER", "RED_PLAYER_1", "RED_PLAYER_2", "RED_PLAYER_3",
  "BLUE_GOAL_KEEPER","BLUE_PLAYER_1","BLUE_PLAYER_2","BLUE_PLAYER_3"
};

// global variables
static NodeRef robot[NUM_ROBOTS];       // to track robot positions
static NodeRef ball;                    // to track ball position
static DeviceTag emitter;               // to send game control data to robots
static float ball_pos[2];               // current ball position
static float robot_pos[NUM_ROBOTS][2];  // current robots position
static float time;                      // time [seconds] since end of half game
static int step_count = 0;              // number of steps since the simulation started
static int i;                           // super global loop index
static int last_touch_robot_index = -1; // index of last robot that touched the ball
static float throw_in_pos[2];           // x and z throw-in position according to RoboCup rules
static const char *message;             // instant message: "Goal!", "Out!", etc.
static float message_steps = 0;         // steps to live for instant message
static int contest_match = 0;           // 1 for an official contest match at Cyberbotics, 0 otherwise

// team names to be displayed in official contest matches
static char team_names[2][64] = { "", "" };

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
static int is_red_robot(int robotIndex) {
  return robotIndex <= 3;
}

static int is_blue_robot(int robotIndex) {
  return robotIndex >= 4;
}

static void display() {

  // display current score
  char text[64];
  if (control_data.state == STATE_INITIAL) {
    // briefly display team names
    supervisor_set_label(0, team_names[TEAM_RED], 0.2, 0.01, 0.1, 0xff0000); // red
    supervisor_set_label(1, team_names[TEAM_BLUE], 0.6, 0.01, 0.1, 0x0000ff); // blue
  }
  else {
    sprintf(text, "%d", control_data.teams[TEAM_RED].score);
    supervisor_set_label(0,text,0.29,0.01,0.1,0xff0000); // red
    sprintf(text, "%d", control_data.teams[TEAM_BLUE].score);
    supervisor_set_label(1,text,0.67,0.01,0.1,0x0000ff); // blue
  }

  // display state
  if (control_data.state == STATE_PLAYING)
    sprintf(text,"%02d:%02d",(int)(time/60),(int)time%60);
  else {
    static const char *STATE_NAMES[5] = { "", "READY", "SET", "PLAYING", "FINISHED" };
    sprintf(text, STATE_NAMES[control_data.state]);
  }
  int length = strlen(text);
  supervisor_set_label(3, text, 0.51 - 0.018 * length, 0.01, 0.1, 0x000000); // black

  if (message_steps > 0) {
    int length = strlen(message);
    supervisor_set_label(4, message, 0.51 - 0.018 * length, 0.89, 0.1, 0x000000); // black
  }
  else {
    supervisor_set_label(4, "", 1, 0.89, 0.1, 0x000000); // empty
    message_steps = 0;
  }
}

static void set_message(const char *msg) {
  message = msg;
  message_steps = 2000 / TIME_STEP;  // show message for 2 seconds
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
  control_data.firstHalf = 1;
  control_data.kickOffTeam = TEAM_RED;

  // emitter for sending game control data
  emitter=robot_get_device("emitter");

  // get robot handles for getting/setting their positions
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
    robot_console_printf("Running Robotstadium contest round\n");
    contest_match = 1;
  }
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

static void step() {

  if (message_steps)
    message_steps--;

  // yield control to simulator
  robot_step(TIME_STEP);

  if (step_count++ % 12 == 0) // every 480 milliseconds
    sendGameControlData();
}

// move robots and ball to kick-off position
static void place_to_kickoff() {
  const float RED_KICK_OFF[NUM_ROBOTS][7] = {
    { 2.950, 0.325, 0.000, 0, 1, 0,-1.57 },
    { 0.625, 0.325, 0.000, 0, 1, 0,-1.57 },
    { 2.000, 0.325, 1.200, 0, 1, 0,-1.57 },
    { 2.000, 0.325,-1.200, 0, 1, 0,-1.57 },
    {-2.950, 0.325, 0.000, 0, 1, 0, 1.57 },
    {-2.000, 0.325,-0.300, 0, 1, 0, 1.57 },
    {-2.000, 0.325, 1.200, 0, 1, 0, 1.57 },
    {-2.000, 0.325,-1.200, 0, 1, 0, 1.57 }
  };
  const float BLUE_KICK_OFF[NUM_ROBOTS][7] = {
    { 2.950, 0.325, 0.000, 0, 1, 0,-1.57 },
    { 2.000, 0.325, 0.300, 0, 1, 0,-1.57 },
    { 2.000, 0.325, 1.200, 0, 1, 0,-1.57 },
    { 2.000, 0.325,-1.200, 0, 1, 0,-1.57 },
    {-2.950, 0.325, 0.000, 0, 1, 0, 1.57 },
    {-0.625, 0.325, 0.000, 0, 1, 0, 1.57 },
    {-2.000, 0.325, 1.200, 0, 1, 0, 1.57 },
    {-2.000, 0.325,-1.200, 0, 1, 0, 1.57 }
  };

  // move robots to kick-off position
  const float *robot_initial_position;
  if (control_data.kickOffTeam == TEAM_RED)
    robot_initial_position=RED_KICK_OFF[0];
  else
    robot_initial_position=BLUE_KICK_OFF[0];

  for(i=0;i<NUM_ROBOTS;i++) {
    supervisor_field_set(robot[i],
                         SUPERVISOR_FIELD_TRANSLATION |
                         SUPERVISOR_FIELD_ROTATION,
                         &robot_initial_position[i*7]);
  }

  // reset ball position
  const float ball_initial_position[2]={0.0f, 0.0f};
  supervisor_field_set(ball, TRANSLATION_X_Z, ball_initial_position);

  set_message("KICK-OFF!");
}

// run simulation for the specified number of seconds while sending control data
static void run_seconds(float seconds) {
  int n = 1000.0f * seconds / TIME_STEP;
  for (i = 0; i < n; i++) step();
}

static void run_initial_state() {
  time = MAX_TIME;
  control_data.state = STATE_INITIAL;
  display();

  if (contest_match) {
    // format=640x480, type=MPEG4, quality=50%
    supervisor_start_movie("movie.avi", 640, 480, 0, 50);
  }

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

static int is_in_kickoff_team(int robotIndex) {
  if (control_data.kickOffTeam == TEAM_RED && is_red_robot(robotIndex)) return 1;
  if (control_data.kickOffTeam == TEAM_BLUE && is_blue_robot(robotIndex)) return 1;
  return 0;
}

// detect if the ball has hit something and what it was
static void detect_touch() {
  if (ball_has_hit_something()) {

    // find if a robot has hit (or was hit by the) the ball
    float minDist2 = 0.25; // squared robot proximity radius
    int minIndex = -1;
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

  if (ball_pos[Z] > FIELD_Y_LIMIT || ball_pos[Z] < -FIELD_Y_LIMIT) {  // out at side line
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

  //let it roll for 2 seconds
  int n = 2000.0 / TIME_STEP;
  for (i = 0; i < n; i++)
    step();

  // throw the ball in
  supervisor_field_set(ball, TRANSLATION_X_Z, throw_in_pos);
}

static void run_playing_state() {

  control_data.state = STATE_PLAYING;
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

static void run_finished_state() {
  control_data.state = STATE_FINISHED;
  display();

  if (contest_match) {
    FILE *file = fopen("scores.txt", "w");
    if (file) {
      fprintf(file, "%d\n%d\n", control_data.teams[TEAM_BLUE].score, control_data.teams[TEAM_RED].score);
      fclose(file);
    }
    else
      robot_console_printf("could not write: scores.txt\n");

    // give some time to show scores
    run_seconds(5);

    // terminate movie recording and quit
    supervisor_stop_movie();
    robot_step(0);
    supervisor_simulation_quit();
  }

  while (1) step();  // wait forever
}

static int run(int ms) {

  run_initial_state();

  do {
    run_ready_state();
    run_set_state();
    run_playing_state();
  }
  while (control_data.state != STATE_FINISHED);

  run_finished_state();

  return 1; // never reached
}

int main() {
  robot_live(reset);
  robot_run(run); // never returns
  return 0;
}
