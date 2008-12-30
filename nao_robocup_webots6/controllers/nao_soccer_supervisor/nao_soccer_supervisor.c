//---------------------------------------------------------------------------------------
//  File:         nao_soccer_supervisor.c
//  Project:      Robotstadium, the online robot soccer competition
//  Description:  Supervisor controller for Robostadium/Nao soccer worlds
//                (You do not need to modify this file for the Robotstadium competition)
//                This controller has several purposes: control the current game state,
//                count the goals, display the scores, move robots and ball to kick-off
//                position, simulate the RoboCup game controller by sending data
//                to the players every 500 ms, check "kick-off shots", throw in ball
//                after it left the field, record match video, penalty kick shootout ...
//  Author:       Olivier Michel & Yvan Bourquin, Cyberbotics Ltd.
//  Date:         July 13th, 2008
//  Changes:      November 6, 2008: Adapted to Webots6 API
//---------------------------------------------------------------------------------------

#include "RoboCupGameControlData.h"
#include <webots/robot.h>
#include <webots/supervisor.h>
#include <webots/emitter.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include <stdlib.h>

enum { X, Y, Z };

// field dimensions (in meters) and other constants that should match the .wbt file
#define NUM_ROBOTS 6
#define BALL_RADIUS 0.043
#define FIELD_SIZE_X 6.8   // official size of the field
#define FIELD_SIZE_Z 4.4   // for the 2008 competition

static const double CIRCLE_RADIUS = 0.65;      // soccer field's central circle
static const double FIELD_X_LIMIT = FIELD_SIZE_X / 2 + BALL_RADIUS;
static const double FIELD_Z_LIMIT = FIELD_SIZE_Z / 2 + BALL_RADIUS;
static const int    TIME_STEP = 40;            // should be a multiple of WorldInfo.basicTimeSTep
static const double MAX_TIME = 10.0 * 60.0;    // a match lasts 10 minutes

// robot DEF names as specified in the .wbt files
static const char *ROBOT_DEF_NAMES[NUM_ROBOTS] = {
  "RED_GOAL_KEEPER", "RED_PLAYER_1", "RED_PLAYER_2",
  "BLUE_GOAL_KEEPER","BLUE_PLAYER_1","BLUE_PLAYER_2"
};

// robot indices used for penalty kick shoot-out
static const int ATTACKER = 1;
static const int GOALIE = 3;

// global variables
static WbFieldRef robot_translation[NUM_ROBOTS];  // to track robot positions
static WbFieldRef robot_rotation[NUM_ROBOTS];     // and rotations
static WbFieldRef robot_controller[NUM_ROBOTS];   // to switch red/blue controllers
static WbFieldRef ball_translation = NULL;        // to track ball position

static WbDeviceTag emitter;                  // to send game control data to robots
static const double *ball_pos;               // current ball position (pointer to)
static const double *robot_pos[NUM_ROBOTS];  // current robots position (pointer to)
static double time;                          // time [seconds] since end of half game
static int step_count = 0;                   // number of steps since the simulation started
static int last_touch_robot_index = -1;      // index of last robot that touched the ball
static const char *message;                  // instant message: "Goal!", "Out!", etc.
static double message_steps = 0;             // steps to live for instant message
static int swapped = 0;                      // jersey colors was swapped (red robots use the blue controller and vice-versa)

// Robotstadium match type
enum {
  DEMO,       // DEMO, does not make a video, does not terminate the simulator, does not write scores.txt
  ROUND,      // regular contest round: 2 x 10 min + possible penalty shots
  FINAL       // add sudden death-shots if games is tied after penalty kick shoot-out
};
static int match_type = DEMO;        // 1 for an official contest match at Cyberbotics, 0 otherwise

// team names to be displayed in official contest matches
static char team_names[2][64] = { "Team-A", "Team-B" };

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

  const double FONT_SIZE = 0.08;

  // display team names and current score
  char text[64];
  sprintf(text, "%s - %d", team_names[TEAM_RED], control_data.teams[TEAM_RED].score);
  wb_supervisor_set_label(0, text, 0.05, 0.01, FONT_SIZE, 0xff0000, 0.0); // red
  sprintf(text, "%d - %s", control_data.teams[TEAM_BLUE].score, team_names[TEAM_BLUE]);
  wb_supervisor_set_label(1, text, 0.99 - 0.025 * strlen(text), 0.01, FONT_SIZE, 0x0000ff, 0.0); // blue

  // display game state or remaining time
  if (control_data.state == STATE_PLAYING)
    sprintf(text,"%02d:%02d",(int)(time/60),(int)time%60);
  else {
    static const char *STATE_NAMES[5] = { "INITIAL", "READY", "SET", "PLAYING", "FINISHED" };
    sprintf(text, STATE_NAMES[control_data.state]);
  }
  wb_supervisor_set_label(2, text, 0.51 - 0.015 * strlen(text), 0.1, FONT_SIZE, 0x000000, 0.0); // black

  // display instant message
  if (message_steps > 0)
    wb_supervisor_set_label(3, message, 0.51 - 0.015 * strlen(message), 0.9, FONT_SIZE, 0x000000, 0.0); // black
  else {
    // remove instant message
    wb_supervisor_set_label(3, "", 1, 0.9, FONT_SIZE, 0x000000, 0.0);
    message_steps = 0;
  }
}

// add an instant message
static void show_message(const char *msg) {
  message = msg;
  message_steps = 4000 / TIME_STEP;  // show message for 4 seconds
  display();
  printf("%s\n", msg);
}

static void sendGameControlData() {
  // prepare and send game control data
  control_data.secsRemaining = (uint32)time;
  if (match_type == DEMO) {
    // ball position is not sent during official matches
    control_data.ballXPos = ball_pos[X];
    control_data.ballYPos = ball_pos[Z];
  }
  wb_emitter_send(emitter, &control_data, sizeof(control_data));
}

// initialize devices and data
static void initialize() {

  // necessary to initialize Webots
  wb_robot_init();

  // initialize game control data
  memset(&control_data, 0, sizeof(control_data));
  memcpy(control_data.header, GAMECONTROLLER_STRUCT_HEADER, sizeof(GAMECONTROLLER_STRUCT_HEADER));
  control_data.playersPerTeam = NUM_ROBOTS / 2;

  // emitter for sending game control data
  emitter = wb_robot_get_device("emitter");

  // get robot handles for getting/setting their positions
  int i;
  for(i = 0; i < NUM_ROBOTS; i++) {
    WbNodeRef robot = wb_supervisor_node_get_from_def(ROBOT_DEF_NAMES[i]);
    if (robot) {
      robot_translation[i] = wb_supervisor_node_get_field(robot, "translation");
      robot_rotation[i] = wb_supervisor_node_get_field(robot, "rotation");
      robot_controller[i] = wb_supervisor_node_get_field(robot, "controller");
    }
    else {
      robot_translation[i] = NULL;
      robot_rotation[i] = NULL;
      robot_controller[i] = NULL;
    }
  }

  // to keep track of ball position
  WbNodeRef ball = wb_supervisor_node_get_from_def("BALL");
  if (ball)
    ball_translation = wb_supervisor_node_get_field(ball, "translation");

  // read teams names from file
  FILE *file = fopen("teams.txt", "r");
  if (file) {
    fscanf(file, "%[^\n]\n%[^\n]", team_names[TEAM_BLUE], team_names[TEAM_RED]);
    fclose(file);
  }

  // variable set during official matches
  const char *WEBOTS_ROBOTSTADIUM = getenv("WEBOTS_ROBOTSTADIUM");
  if (WEBOTS_ROBOTSTADIUM) {
    if (strcmp(WEBOTS_ROBOTSTADIUM, "ROUND") == 0) {
      match_type = ROUND;
      printf("Running Robotstadium ROUND match\n");
    }
    else if (strcmp(WEBOTS_ROBOTSTADIUM, "FINAL") == 0) {
      match_type = FINAL;
      printf("Running Robotstadium FINAL match\n");
    }
  }

  // make video
  if (match_type != DEMO) {
    // format=640x480, type=MPEG4, quality=75%
    wb_supervisor_start_movie("movie.avi", 640, 480, 0, 75);
  }

  // enable keyboard for manual score control
  wb_robot_keyboard_enable(TIME_STEP * 10);
}

// detect if the ball has hit something (robot goal post, etc.)
// returns: 1 = hit, 0 = no hit
// hit detection is based on a trajectory or velocity change measured
// over the two most recent time steps
static int ball_has_hit_something() {

  // ball position during the 3 last time steps
  // index 2 is the newest, index 0 is the oldest
  static double x[3] = { 0, 0, 0 };  // init to empty history
  static double z[3] = { 0, 0, 0 };

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
  double dir2_x = x[2] - x[1]; // now
  double dir2_z = z[2] - z[1];
  double dir1_x = x[1] - x[0]; // before
  double dir1_z = z[1] - z[0];

  // compute ball velocity at time t and t-1
  double vel2 = sqrt(dir2_x * dir2_x + dir2_z * dir2_z);  // now
  double vel1 = sqrt(dir1_x * dir1_x + dir1_z * dir1_z);  // before

  // a strong acceleration or deceleration correspond to the ball being hit (or hitting something)
  // however some deceleration is normal because the ball slows down due to the simulated friction
  if (vel2 > vel1 * 1.001 || vel2 < 0.9 * vel1)
    return 1;

  // compute ball direction at time t and t-1
  double angle2 = atan2(dir2_z, dir2_x);  // now
  double angle1 = atan2(dir1_z, dir1_x);  // before

  // measure change in trajectory angle
  double angle_diff = fabs(angle2 - angle1);

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
  switch (wb_robot_keyboard_get_key()) {
  case WB_ROBOT_KEYBOARD_SHIFT + 'R':
    if (control_data.teams[TEAM_RED].score > 0) {
      control_data.teams[TEAM_RED].score--;
      display();
    }
    break;

  case 'R':
    control_data.teams[TEAM_RED].score++;
    display();
    break;

  case WB_ROBOT_KEYBOARD_SHIFT + 'B':
    if (control_data.teams[TEAM_BLUE].score > 0) {
      control_data.teams[TEAM_BLUE].score--;
      display();
    }
    break;

  case 'B':
    control_data.teams[TEAM_BLUE].score++;
    display();
    break;
  }
}

// this is what we do at every time step
// independently of the game state
static void step() {

  // copy pointer to ball position values
  if (ball_translation)
    ball_pos = wb_supervisor_field_get_sf_vec3f(ball_translation);

  int i;
  for (i = 0; i < NUM_ROBOTS; i++)
    // copy pointer to robot position values
    if (robot_translation[i])
      robot_pos[i] = wb_supervisor_field_get_sf_vec3f(robot_translation[i]);

  if (message_steps)
    message_steps--;

  // yield control to simulator
  wb_robot_step(TIME_STEP);

  // every 480 milliseconds
  if (step_count++ % 12 == 0)
    sendGameControlData();

  // read key pressed
  check_keyboard();
}

static void move_robot(int robot, double tx, double ty, double tz, double alpha) {
  if (robot_translation[robot] && robot_rotation[robot]) {
    double trans[3] = { tx, ty, tz };
    double rot[4] = { 0, 1, 0, alpha };
    wb_supervisor_field_set_sf_vec3f(robot_translation[robot], trans);
    wb_supervisor_field_set_sf_rotation(robot_rotation[robot], rot);
  }
}

static void move_ball(double tx, double tz) {
  if (ball_translation) {
    double trans[3] = { tx, BALL_RADIUS, tz };
    wb_supervisor_field_set_sf_vec3f(ball_translation, trans);
  }
}

// move robots and ball to kick-off position
static void place_to_kickoff() {

  // move the two goalies
  move_robot(0,  2.950, 0.325, 0.000,-1.57);
  move_robot(3, -2.950, 0.325, 0.000, 1.57);

  // move other robots
  if (control_data.kickOffTeam == TEAM_RED) {
    move_robot(1, 0.625, 0.325, 0.000,-1.57);
    move_robot(2, 2.000, 0.325, 1.200,-1.57);
    move_robot(4,-2.000, 0.325,-0.300, 1.57);
    move_robot(5,-2.000, 0.325, 1.200, 1.57);
  }
  else {
    move_robot(1, 2.000, 0.325, 0.300,-1.57);
    move_robot(2, 2.000, 0.325,-1.200,-1.57);
    move_robot(4,-0.625, 0.325, 0.000, 1.57);
    move_robot(5,-2.000, 0.325,-1.200, 1.57);
  }

  // reset ball position
  move_ball(0, 0);
}

// run simulation for the specified number of seconds while sending control data
static void run_seconds(double seconds) {
  int n = 1000.0 * seconds / TIME_STEP;
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
    double minDist2 = 0.25; // squared robot proximity radius
    int minIndex = -1;
    int i;
    for (i = 0; i < NUM_ROBOTS && robot_translation[i]; i++) {
      double dx = robot_pos[i][X] - ball_pos[X];
      double dz = robot_pos[i][Z] - ball_pos[Z];

      // squared distance between robot and ball
      double dist2 = dx * dx + dz * dz;
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

static double sign(double value) {
  return value > 0.0 ? 1.0 : -1.0;
}

// check if the ball leaves the field and throw ball in if necessary
static void check_ball_out() {
  const double THROW_IN_LINE_Y     = 1.6;
  const double THROW_IN_LINE_END_X = 2.0;
  const double CORNER_KICK_X       = 2.0;
  const double CORNER_KICK_Y       = 1.2;

  double throw_in_pos[2];   // x and z throw-in position according to RoboCup rules

  if (ball_pos[Z] > FIELD_Z_LIMIT || ball_pos[Z] < -FIELD_Z_LIMIT) {  // out at side line
    // printf("ball over side-line: %f %f\n", ball_pos[X], ball_pos[Z]);
    double back;
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
  show_message("OUT!");
  kick_off_state = KICK_OFF_OK;

  // let the ball roll for 2 seconds
  run_seconds(2.0);

  // throw the ball in
  move_ball(throw_in_pos[X], throw_in_pos[Z]);
}

static void run_playing_state() {

  control_data.state = STATE_PLAYING;
  show_message("KICK-OFF!");
  kick_off_state = KICK_OFF_INITIAL;
  last_touch_robot_index = -1;

  while (1) {
    // substract TIME_STEP to current time
    time -= TIME_STEP / 1000.0;
    display();

    if (time < 0.0) {
      time = 0.0;
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
        show_message("GOAL!");
      }
      else
        show_message("KICK-OFF SHOT!");

      control_data.state = STATE_READY;
      control_data.kickOffTeam = TEAM_RED;
      return;
    }
    else if (is_ball_in_blue_goal()) {  // ball in the blue goal

      // a goal cannot be scored directly from a kick-off
      if (control_data.kickOffTeam == TEAM_BLUE || kick_off_state == KICK_OFF_OK) {
        control_data.teams[TEAM_RED].score++;
        show_message("GOAL!");
      }
      else
        show_message("KICK-OFF SHOT!");

      control_data.state = STATE_READY;
      control_data.kickOffTeam = TEAM_BLUE;
      return;
    }

    step();
  }
}

static void terminate() {

  if (match_type != DEMO) {
    FILE *file = fopen("scores.txt", "w");
    if (file) {
      if (! swapped) 
        fprintf(file, "%d\n%d\n", control_data.teams[TEAM_BLUE].score, control_data.teams[TEAM_RED].score);
      else
        fprintf(file, "%d\n%d\n", control_data.teams[TEAM_RED].score, control_data.teams[TEAM_BLUE].score);
      fclose(file);
    }
    else
      printf("could not write: scores.txt\n");

    // give some time to show scores
    run_seconds(10);

    // terminate movie recording and quit
    wb_supervisor_stop_movie();
    wb_robot_step(0);
    wb_supervisor_simulation_quit();
  }

  while (1) step();  // wait forever
}

const char *get_controller_name(int robot) {
  if ((is_red_robot(robot) && ! swapped) || (is_blue_robot(robot) && swapped))
    return "nao_soccer_player_red";
  else
    return "nao_soccer_player_blue";
}

static void set_controller(int robot, const char *controller) {
  if (robot_controller[robot])
    wb_supervisor_field_set_sf_string(robot_controller[robot], controller);
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
    set_controller(ATTACKER, get_controller_name(ATTACKER));
    set_controller(GOALIE, get_controller_name(GOALIE));
  }
  else {
    // half time break
    int i;
    for (i = 0; i < NUM_ROBOTS; i++)
      set_controller(i, get_controller_name(i));
  }
}

static void run_half_time_break() {
  show_message("HALF TIME BREAK!");
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
static double randomize_pos() {
  return (double)rand() / (double)RAND_MAX * 0.01 - 0.005;   // +/- 1 cm
}

// randomize initial angle for penalty kick shootout
static double randomize_angle() {
  return (double)rand() / (double)RAND_MAX * 0.1745 - 0.0873;  // +/- 5 degrees
}

static void run_penalty_kick(double delay) {

  // "The ball is placed 1.5m from the center of the field in the direction of the defender's goal"
  const double BALL_TRANS[2] = { -1.5 + randomize_pos(), randomize_pos() };
  move_ball(BALL_TRANS[0], BALL_TRANS[1]);

  // "The attacking robot is positioned 50cm behind the ball"
  move_robot(ATTACKER, -1.0 + randomize_pos(), 0.325, 0.0 + randomize_pos(), -1.57 + randomize_angle());

  // "The goalie is placed with feet on the goal line and in the centre of the goal"
  move_robot(GOALIE, -3.0 + randomize_pos(), 0.325, 0.0 + randomize_pos(), 1.57 + randomize_angle());

  time = delay;
  control_data.kickOffTeam = TEAM_RED;

  run_set_state();

  control_data.state = STATE_PLAYING;
  display();

  do {
    // substract TIME_STEP to current time
    time -= TIME_STEP / 1000.0;
    display();

    if (time < 0.0) {
      time = 0.0;
      show_message("TIME OUT!");
      return;
    }
    step();
  }
  while (fabs(ball_pos[X] - BALL_TRANS[0]) < 0.01 && fabs(ball_pos[Z] - BALL_TRANS[1]) < 0.01);

  show_message("SHOOTING!");

  // ball was hit: give it 15 seconds to reach goal or leave field
  int n = 15000 / TIME_STEP;
  do {
    step();
    n--;
  }
  while (n > 0 && is_ball_in_field());

  if (is_ball_in_blue_goal()) {
    control_data.teams[TEAM_RED].score++;
    show_message("GOAL!");
  }
  else
    show_message("MISSED!");
}

static void run_victory(int winner) {
  char message[128];
  sprintf(message, "%s WINS!", team_names[winner]);
  show_message(message);
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

  show_message("PENALTY KICK SHOOT-OUT!");
  step();

  // power off robots and move them out of the field
  int i;
  for (i = 0; i < NUM_ROBOTS; i++) {
    // keep RED_PLAYER_1 and BLUE_GOAL_KEEPER for the shootout
    if (i != ATTACKER && i != GOALIE) {
      // make them zombies
      set_controller(i, "void");

      if (robot_translation[i]) {
        // move them out of the field but preserve elevation
        double elevation = wb_supervisor_field_get_sf_vec3f(robot_translation[i])[1];
        double out_of_field[3] = { 1.0 * i, elevation, 5.0 };
        wb_supervisor_field_set_sf_vec3f(robot_translation[i], out_of_field);
      }
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
  
  if (match_type == FINAL) {
    show_message("SUDDEN DEATH SHOOT-OUT!");
    
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
}

int main() {
  // init devices and data structures
  initialize();

  // first half-time
  control_data.firstHalf = 1;
  run_half_time();
  run_half_time_break();

  // second half-time
  control_data.firstHalf = 0;
  run_half_time();

  // if the game is tied, start penalty kicks
  if (control_data.teams[TEAM_BLUE].score == control_data.teams[TEAM_RED].score) {
    run_penalty_kick_shootout();
  }

  // terminate movie, write scores.txt, etc.
  terminate();

  return 0; // never reached
}
