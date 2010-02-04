//--------------------------------------------------------------------------------------------
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
//  Changes:      November 6, 2008: Adapted to Webots 6 API by Yvan Bourquin
//                February 26, 2009: Added throw-in collision avoidance (thanks to Giuseppe Certo)
//                April 23, 2009: Added: robot is held in position during the INITIAL and SET states
//                May 27, 2009: Changed penalty kick rules according to latest SPL specification
//                              Changed field dimensions according to latest SPL specification
//                              Modified collision detection to support asymmetrical ball
//--------------------------------------------------------------------------------------------

#include "RoboCupGameControlData.h"
#include <webots/robot.h>
#include <webots/supervisor.h>
#include <webots/emitter.h>
#include <webots/receiver.h>
#include <stdio.h>
#include <string.h>
#include <math.h>
#include <stdlib.h>

// used for indexing arrays
enum { X, Y, Z };

// max num robots
#define NUM_ROBOTS (2*MAX_NUM_PLAYERS)

// field dimensions (in meters) according to the SPL Nao Rule Book
#define FIELD_SIZE_X         6.050   // official size of the field
#define FIELD_SIZE_Z         4.050   // for the 2008 competition
#define CIRCLE_DIAMETER      1.250   // soccer field's central circle
#define PENALTY_SPOT_DIST    1.825   // distance between penalty spot and goal
#define PENALTY_AREA_Z_DIM   3.050
#define PENALTY_AREA_X_DIM   0.650
#define GOAL_WIDTH           1.400
#define THROW_IN_LINE_LENGTH 4.000   // total length
#define THROW_IN_LINE_OFFSET 0.400   // offset from side line
#define LINE_WIDTH           0.050   // white lines
#define BALL_RADIUS          0.0325

// throw-in lines
const double THROW_IN_LINE_X_END = THROW_IN_LINE_LENGTH / 2;
const double THROW_IN_LINE_Z = FIELD_SIZE_Z / 2 - THROW_IN_LINE_OFFSET;

// ball position limits
static const double CIRCLE_RADIUS_LIMIT = CIRCLE_DIAMETER / 2 + BALL_RADIUS;
static const double FIELD_X_LIMIT = FIELD_SIZE_X / 2 + BALL_RADIUS;
static const double FIELD_Z_LIMIT = FIELD_SIZE_Z / 2 + BALL_RADIUS;

// penalties
static const double PENALTY_BALL_X_POS = FIELD_SIZE_X / 2 - PENALTY_SPOT_DIST;
static const double PENALTY_GOALIE_X_POS = (FIELD_SIZE_X - LINE_WIDTH) / 2;

// timing
static const int    TIME_STEP = 40;            // should be a multiple of WorldInfo.basicTimeSTep
static const double MAX_TIME = 10.0 * 60.0;    // a match half lasts 10 minutes

// indices of the two robots used for penalty kick shoot-out
static const int ATTACKER = 1;              // RED_PLAYER_1
static const int GOALIE = MAX_NUM_PLAYERS;  // BLUE_GOAL_KEEPER

// the information we need to keep about each robot
typedef struct {
  WbFieldRef translation;        // to track robot positions
  WbFieldRef rotation;           // ... and rotations
  WbFieldRef controller;         // to switch red/blue controllers
  const double *position;        // pointer to current robot position
} Robot;

// the Robots
static Robot *robots[NUM_ROBOTS];

// zero vector
static const double ZERO_VEC_3[3] = { 0, 0, 0 };

// global variables
static WbFieldRef ball_translation = NULL;        // to track ball position
static WbFieldRef ball_rotation = NULL;           // to reset ball rotation
static WbDeviceTag emitter;                       // to send game control data to robots
static WbDeviceTag receiver;                      // to receive 'move' requests
static const double *ball_pos = ZERO_VEC_3;       // current ball position (pointer to)
static double time;                               // time [seconds] since end of half game
static int step_count = 0;                        // number of steps since the simulation started
static int last_touch_robot_index = -1;           // index of last robot that touched the ball
static const char *message;                       // instant message: "Goal!", "Out!", etc.
static double message_steps = 0;                  // steps to live for instant message
static int swapped = 0;                           // 1 if the jersey colors were swapped (red robots use the blue controller and vice-versa)

// Robotstadium match type
enum {
  DEMO,       // DEMO, does not make a video, does not terminate the simulator, does not write scores.txt
  ROUND,      // Regular Robostadium round: 2 x 10 min + possible penalty shots
  FINAL       // Robotstadium final: add sudden death-shots if games is tied after penalty kick shoot-out
};
static int match_type = DEMO;

// default team names displayed by the Supervisor
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
  return robot < MAX_NUM_PLAYERS;
}

static int is_blue_robot(int robot) {
  return robot >= MAX_NUM_PLAYERS;
}

// create and initialize a Robot struct
static Robot *robot_new(WbNodeRef node) {
  Robot *robot = malloc(sizeof(Robot));
  robot->translation = wb_supervisor_node_get_field(node, "translation");
  robot->rotation    = wb_supervisor_node_get_field(node, "rotation");
  robot->controller  = wb_supervisor_node_get_field(node, "controller");
  robot->position    = NULL;
  return robot;
}

static const char *get_robot_def_name(int index) {
  static char defname[64];
  const char *color = index < MAX_NUM_PLAYERS ? "RED" : "BLUE";
  int id = index % MAX_NUM_PLAYERS;

  if (id == 0)
    sprintf(defname, "%s_GOAL_KEEPER", color);
  else
    sprintf(defname, "%s_PLAYER_%d", color, id);

  return defname;
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
    sprintf(text, "%s", STATE_NAMES[control_data.state]);
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
    control_data.ballZPos = ball_pos[Z];
  }
  wb_emitter_send(emitter, &control_data, sizeof(control_data));
}

// initialize devices and data
static void initialize() {
  // necessary to initialize Webots
  wb_robot_init();

  // emitter for sending game control data and receiving 'move' requests
  emitter = wb_robot_get_device("emitter");
  receiver = wb_robot_get_device("receiver");
  wb_receiver_enable(receiver, TIME_STEP);

  // create structs for the robots present in the .wbt file
  int robot_count = 0;
  int i;
  for (i = 0; i < NUM_ROBOTS; i++) {
    WbNodeRef node = wb_supervisor_node_get_from_def(get_robot_def_name(i));
    if (node) {
      robots[i] = robot_new(node);
      robot_count++;
    }
    else
      robots[i] = NULL;
  }

  // to keep track of ball position
  WbNodeRef ball = wb_supervisor_node_get_from_def("BALL");
  if (ball) {
    ball_translation = wb_supervisor_node_get_field(ball, "translation");
    ball_rotation = wb_supervisor_node_get_field(ball, "rotation");
  }

  // initialize game control data
  memset(&control_data, 0, sizeof(control_data));
  memcpy(control_data.header, GAMECONTROLLER_STRUCT_HEADER, sizeof(GAMECONTROLLER_STRUCT_HEADER) - 1);
  control_data.version = GAMECONTROLLER_STRUCT_VERSION;
  control_data.playersPerTeam = robot_count / 2;
  control_data.state = STATE_INITIAL;
  control_data.secondaryState = STATE2_NORMAL;
  control_data.teams[0].teamColour = TEAM_BLUE;
  control_data.teams[1].teamColour = TEAM_RED;

  // eventually read teams names from file
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

  if (match_type != DEMO) {
    // start webcam script in background
    system("./webcam.php &");

    // make video: format=480x360, type=MPEG4, quality=75%
    wb_supervisor_start_movie("movie.avi", 480, 360, 0, 75);
  }

  // enable keyboard for manual score control
  wb_robot_keyboard_enable(TIME_STEP * 10);
}

// detect if the ball has hit something (a robot, a goal post, a wall, etc.) during the last time step
// returns: 1 = hit, 0 = no hit
static int ball_has_hit_something() {

  // ball position at previous time step
  static double x1 = 0.0;
  static double z1 = 0.0;
  static double vel1 = 0.0;

  // current ball position
  double x2 = ball_pos[X];
  double z2 = ball_pos[Z];

  // compute ball direction
  double dx = x2 - x1;
  double dz = z2 - z1;

  // compute ball velocity at time t and t-1
  double vel2 = sqrt(dx * dx + dz * dz);

  // a strong acceleration or deceleration correspond to the ball being hit (or hitting something)
  // however some deceleration is normal because the ball slows down due to the rolling and air friction
  // filter noise: if the ball is almost still then forget it
  int hit = vel2 > 0.001 && (vel2 > vel1 * 1.2 || vel2 < vel1 * 0.8);

  // keep for next call
  vel1 = vel2;
  x1 = x2;
  z1 = z2;

  return hit;
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

// move robot to a 3d position
static void move_robot_3d(int robot_index, double tx, double ty, double tz, double alpha) {
  if (robots[robot_index]) {
    double trans[3] = { tx, ty, tz };
    double rot[4] = { 0, 1, 0, alpha };
    wb_supervisor_field_set_sf_vec3f(robots[robot_index]->translation, trans);
    wb_supervisor_field_set_sf_rotation(robots[robot_index]->rotation, rot);
  }
}

// place robot in upright position, feet on the floor
static void move_robot_2d(int robot_index, double tx, double tz, double alpha) {
  if (robots[robot_index])
    move_robot_3d(robot_index, tx, 0.35, tz, alpha);
}

// move ball to 3d position
static void move_ball_3d(double tx, double ty, double tz) {
  if (ball_translation && ball_rotation) {
    double trans[3] = { tx, ty, tz };
    double rot[4] = { 0, 1, 0, 0 };
    wb_supervisor_field_set_sf_vec3f(ball_translation, trans);
    wb_supervisor_field_set_sf_rotation(ball_rotation, rot);
  }
}

// move ball to 2d position and down on the floor
static void move_ball_2d(double tx, double tz) {
  move_ball_3d(tx, BALL_RADIUS, tz);
}

// handle a "move robot" request received from a robot controller
static void handle_move_robot_request(const char *request) {
  if (match_type != DEMO) {
    printf("not in DEMO mode: ignoring request: %s\n", request);
    return;
  }

  char team[64];
  int robotID;
  double tx, ty, tz, alpha;
  if (sscanf(request, "move robot %s %d %lf %lf %lf %lf", team, &robotID, &tx, &ty, &tz, &alpha) != 6) {
    printf("unexpected number of arguments in 'move robot' request: %s\n", request);
    return;
  }

  printf("executing: %s\n", request);
  if (strcmp(team, "BLUE") == 0)
    robotID += MAX_NUM_PLAYERS;

  // move it now!
  move_robot_3d(robotID, tx, ty, tz, alpha);
}

// handle a "move ball" request received from a robot controller
static void handle_move_ball_request(const char *request) {
  if (match_type != DEMO) {
    printf("not in DEMO mode: ignoring request: %s\n", request);
    return;
  }

  double tx, ty, tz;
  if (sscanf(request, "move ball %lf %lf %lf", &tx, &ty, &tz) != 3) {
    printf("unexpected number of arguments in 'move ball' request: %s\n", request);
    return;
  }

  printf("executing: %s\n", request);

  // move it now!
  move_ball_3d(tx, ty, tz);
}

static void read_incoming_messages() {
  // read while queue not empty
  while (wb_receiver_get_queue_length(receiver) > 0) {
    // I'm only expecting ascii messages
    const char *request = wb_receiver_get_data(receiver);
    if (memcmp(request, "move robot ", 11) == 0)
      handle_move_robot_request(request);
    else if (memcmp(request, "move ball ", 10) == 0)
      handle_move_ball_request(request);
    else
      printf("received unknown message of %d bytes\n", wb_receiver_get_data_size(receiver));

    wb_receiver_next_packet(receiver);
  }
}

// this is what is done at every time step independently of the game state
static void step() {

  // copy pointer to ball position values
  if (ball_translation)
    ball_pos = wb_supervisor_field_get_sf_vec3f(ball_translation);

  // update robot position pointers
  int i;
  for (i = 0; i < NUM_ROBOTS; i++)
    if (robots[i])
      robots[i]->position = wb_supervisor_field_get_sf_vec3f(robots[i]->translation);

  if (message_steps)
    message_steps--;

  // yield control to simulator
  wb_robot_step(TIME_STEP);

  // every 480 milliseconds
  if (step_count % 12 == 0)
    sendGameControlData();

  step_count++;

  // did I receive a message ?
  read_incoming_messages();

  // read key pressed
  check_keyboard();
}

// move robots and ball to kick-off position
static void place_to_kickoff() {

  // Manual placement according to the RoboCup SPL Rule Book:
  //   "The kicking-off robot is placed on the center circle, right in front of the penalty mark.
  //   Its feet touch the line, but they are not inside the center circle.
  //   The second field player of the attacking team is placed in front
  //   of one of the goal posts on the height of the penalty mark"

  const double KICK_OFF_X = CIRCLE_DIAMETER / 2 + LINE_WIDTH;
  const double GOALIE_X = (FIELD_SIZE_X - LINE_WIDTH) / 2;
  const double DEFENDER_X = FIELD_SIZE_X / 2 - PENALTY_AREA_X_DIM - LINE_WIDTH;

  // move the two goalies
  move_robot_2d(0, GOALIE_X, 0, -1.5708);
  move_robot_2d(MAX_NUM_PLAYERS + 0, -GOALIE_X, 0, 1.5708);

  // move other robots
  if (control_data.kickOffTeam == TEAM_RED) {
    move_robot_2d(1, KICK_OFF_X, 0, -1.5708);
    move_robot_2d(2, PENALTY_BALL_X_POS, GOAL_WIDTH / 2, -1.5708);
    move_robot_2d(MAX_NUM_PLAYERS + 1, -DEFENDER_X, -PENALTY_AREA_Z_DIM / 2, 1.5708);
    move_robot_2d(MAX_NUM_PLAYERS + 2, -DEFENDER_X, PENALTY_AREA_Z_DIM / 2, 1.5708);
  }
  else {
    move_robot_2d(1, DEFENDER_X, PENALTY_AREA_Z_DIM / 2, -1.5708);
    move_robot_2d(2, DEFENDER_X, -PENALTY_AREA_Z_DIM / 2, -1.5708);
    move_robot_2d(MAX_NUM_PLAYERS + 1, -KICK_OFF_X, 0, 1.5708);
    move_robot_2d(MAX_NUM_PLAYERS + 2, -PENALTY_BALL_X_POS, -GOAL_WIDTH / 2, 1.5708);
  }

  // reset ball position
  move_ball_2d(0, 0);
}

// run simulation for the specified number of seconds
static void run_seconds(double seconds) {
  int n = 1000.0 * seconds / TIME_STEP;
  int i;
  for (i = 0; i < n; i++)
    step();
}

static void hold_to_kickoff(double seconds) {
  int n = 1000.0 * seconds / TIME_STEP;
  int i;
  for (i = 0; i < n; i++) {
    place_to_kickoff();
    step();
  }
}

static void run_initial_state() {
  time = MAX_TIME;
  control_data.state = STATE_INITIAL;
  display();
  hold_to_kickoff(5);
}

static void run_ready_state() {
  control_data.state = STATE_READY;
  display();
  run_seconds(5);
}

static void run_set_state() {
  control_data.state = STATE_SET;
  display();
  hold_to_kickoff(5);
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

// detect if a robot has just touched the ball (in the last time step)
// returns: robot index or -1 if there is no such robot
static int detect_ball_touch() {

  if (! ball_has_hit_something())
    return -1;

  // find which robot is the closest to the ball
  double minDist2 = 0.25;  // squared robot proximity radius
  int index = -1;
  int i;
  for (i = 0; i < NUM_ROBOTS; i++) {
    if (robots[i]) {
      double dx = robots[i]->position[X] - ball_pos[X];
      double dz = robots[i]->position[Z] - ball_pos[Z];

      // squared distance between robot and ball
      double dist2 = dx * dx + dz * dz;
      if (dist2 < minDist2) {
        minDist2 = dist2;
        index = i;
      }
    }
  }

  // print info
  if (index > -1) {
    if (is_red_robot(index))
      printf("RED TOUCH\n");
    else
      printf("BLUE TOUCH\n");
  }

  return index;
}

static int is_ball_in_field() {
  return fabs(ball_pos[Z]) <= FIELD_Z_LIMIT && fabs(ball_pos[X]) <= FIELD_X_LIMIT;
}

static int is_ball_in_red_goal() {
  return ball_pos[X] > FIELD_X_LIMIT && ball_pos[X] < FIELD_X_LIMIT + 0.25 && fabs(ball_pos[Z]) < GOAL_WIDTH / 2;
}

static int is_ball_in_blue_goal() {
  return ball_pos[X] < -FIELD_X_LIMIT && ball_pos[X] > -(FIELD_X_LIMIT + 0.25) && fabs(ball_pos[Z]) < GOAL_WIDTH / 2;
}

static int is_ball_in_central_circle() {
  return ball_pos[X] * ball_pos[X] + ball_pos[Z] * ball_pos[Z] < CIRCLE_RADIUS_LIMIT * CIRCLE_RADIUS_LIMIT;
}

static double sign(double value) {
  return value > 0.0 ? 1.0 : -1.0;
}

static void update_kick_off_state() {
  if (kick_off_state == KICK_OFF_INITIAL && ! is_ball_in_central_circle())
    kick_off_state = KICK_OFF_LEFT_CIRCLE;

  int touch_index = detect_ball_touch();
  if (touch_index != -1) {
    last_touch_robot_index = touch_index;

    // "the ball must touch a player from the kick-off team after leaving the center circle
    // before a goal can be scored by the team taking the kick-off"
    if (kick_off_state == KICK_OFF_LEFT_CIRCLE && is_in_kickoff_team(last_touch_robot_index))
      kick_off_state = KICK_OFF_OK;

    if (kick_off_state == KICK_OFF_INITIAL && ! is_in_kickoff_team(last_touch_robot_index))
      kick_off_state = KICK_OFF_OK;
  }
}

// check if throwing the ball in does not collide with a robot.
// If it does collide, change the throw-in location.
static void check_throw_in(double x, double z) {

  // run some steps to see if the ball is moving: that would indicate a collision
  step();
  ball_has_hit_something();
  step();
  ball_has_hit_something(); // because after a throw in, this method return always 1 even if no collision occured
  step();

  while (ball_has_hit_something()) {
    // slope of the line formed by the throw in point and the origin point.
    double slope = z / x;
    z -= sign(z) * 0.1;
    x = z / slope;
    move_ball_2d(x, z);
    check_throw_in(x, z); // recursive call to check the new throw in point.
  }
}

// check if the ball leaves the field and throw ball in if necessary
static void check_ball_out() {

  double throw_in_pos[3];   // x and z throw-in position

  if (fabs(ball_pos[Z]) > FIELD_Z_LIMIT) {  // out at side line
    // printf("ball over side-line: %f %f\n", ball_pos[X], ball_pos[Z]);
    double back;
    if (last_touch_robot_index == -1)  // not sure which team has last touched the ball
      back = 0.0;
    else if (is_red_robot(last_touch_robot_index))
      back = 1.0;  // 1 meter towards red goal
    else
      back = -1.0;  // 1 meter towards blue goal

    throw_in_pos[X] = ball_pos[X] + back;
    throw_in_pos[Z] = sign(ball_pos[Z]) * THROW_IN_LINE_Z;

    // in any case the ball cannot be placed off the throw-in line
    if (throw_in_pos[X] > THROW_IN_LINE_X_END)
      throw_in_pos[X] = THROW_IN_LINE_X_END;
    else if (throw_in_pos[X] < -THROW_IN_LINE_X_END)
      throw_in_pos[X] = -THROW_IN_LINE_X_END;
  }
  else if (ball_pos[X] > FIELD_X_LIMIT && ! is_ball_in_red_goal()) {  // out at end line
    // printf("ball over end-line (near red goal): %f %f\n", ball_pos[X], ball_pos[Z]);
    if (last_touch_robot_index == -1) {   // not sure which team has last touched the ball
      throw_in_pos[X] = THROW_IN_LINE_X_END;
      throw_in_pos[Z] = sign(ball_pos[Z]) * THROW_IN_LINE_Z;
    }
    else if (is_red_robot(last_touch_robot_index)) { // defensive team
      throw_in_pos[X] = THROW_IN_LINE_X_END;
      throw_in_pos[Z] = sign(ball_pos[Z]) * THROW_IN_LINE_Z;
    }
    else { // offensive team
      throw_in_pos[X] = 0.0; // halfway line 
      throw_in_pos[Z] = sign(ball_pos[Z]) * THROW_IN_LINE_Z;
    }
  }
  else if (ball_pos[X] < -FIELD_X_LIMIT && ! is_ball_in_blue_goal()) {  // out at end line
    // printf("ball over end-line (near blue goal): %f %f\n", ball_pos[X], ball_pos[Z]);
    if (last_touch_robot_index == -1) {  // not sure which team has last touched the ball
      throw_in_pos[X] = -THROW_IN_LINE_X_END;
      throw_in_pos[Z] = sign(ball_pos[Z]) * THROW_IN_LINE_Z;
    }
    else if (is_blue_robot(last_touch_robot_index)) { // defensive team
      throw_in_pos[X] = -THROW_IN_LINE_X_END;
      throw_in_pos[Z] = sign(ball_pos[Z]) * THROW_IN_LINE_Z;
    }
    else { // offensive team
      throw_in_pos[X] = 0.0; // halfway line 
      throw_in_pos[Z] = sign(ball_pos[Z]) * THROW_IN_LINE_Z;
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
  move_ball_2d(throw_in_pos[X], throw_in_pos[Z]);
  check_throw_in(throw_in_pos[X], throw_in_pos[Z]);
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

    update_kick_off_state();

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

    // freeze webcam
    system("killall webcam.php");

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

static void set_controller(int robot_index, const char *controller) {
  if (robots[robot_index])
    wb_supervisor_field_set_sf_string(robots[robot_index]->controller, controller);
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
  if (control_data.secondaryState == STATE2_PENALTYSHOOT) {
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

static int ball_completely_outside_penalty_area() {
  const double X_LIMIT = FIELD_SIZE_X / 2 - PENALTY_AREA_X_DIM - BALL_RADIUS;
  const double Z_LIMIT = PENALTY_AREA_Z_DIM / 2 + BALL_RADIUS;
  return ball_pos[X] > -X_LIMIT || fabs(ball_pos[Z]) > Z_LIMIT;
}

static int ball_completely_inside_penalty_area() {
  const double X_LIMIT = FIELD_SIZE_X / 2 - PENALTY_AREA_X_DIM + BALL_RADIUS;
  const double Z_LIMIT = PENALTY_AREA_Z_DIM / 2 - BALL_RADIUS;
  return ball_pos[X] < -X_LIMIT && fabs(ball_pos[Z]) < Z_LIMIT;
}

static void run_penalty_kick(double delay) {

  // game control
  time = delay;
  control_data.kickOffTeam = TEAM_RED;
  control_data.state = STATE_SET;
  display();

  // "The ball is placed on the penalty spot"
  move_ball_2d(-PENALTY_BALL_X_POS + randomize_pos(), randomize_pos());

  // "The attacking robot is positioned at the center of the field, facing the ball"
  // "The goal keeper is placed with feet on the goal line and in the centre of the goal"
  const double ATTACKER_POS[3] = { randomize_pos(), randomize_pos(), -1.5708 + randomize_angle() };
  const double GOALIE_POS[3] = { -PENALTY_GOALIE_X_POS + randomize_pos(), randomize_pos(), 1.5708 + randomize_angle() };

  // hold attacker and goal keeper 5 seconds in place during the SET state
  int n;
  for (n = 5000 / TIME_STEP; n > 0; n--) {
    move_robot_2d(ATTACKER, ATTACKER_POS[0], ATTACKER_POS[1], ATTACKER_POS[2]);
    move_robot_2d(GOALIE, GOALIE_POS[0], GOALIE_POS[1], GOALIE_POS[2]);
    step();
  }

  // switch to PLAYING state
  control_data.state = STATE_PLAYING;

  do {
    // substract TIME_STEP to current time
    time -= TIME_STEP / 1000.0;
    display();

    if (time < 0.0) {
      time = 0.0;
      show_message("TIME OUT!");
      return;
    }

    int robot_index = detect_ball_touch();

    // "If the goal keeper touches the ball outside the penalty area then a goal will be awarded to the attacking team"
    if (robot_index == GOALIE && ball_completely_outside_penalty_area()) {
      control_data.teams[TEAM_RED].score++;
      show_message("ILLEGAL GOALIE ACTION!");
      return;
    }
    // "If the attacker touched the ball inside the penalty area then the penalty shot is deemed unsuccessful"
    else if (robot_index == ATTACKER && ball_completely_inside_penalty_area()) {
      show_message("ILLEGAL ATTACKER ACTION!");
      return;
    }

    step();
  }
  while (is_ball_in_field());

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

      if (robots[i]) {
        // move them out of the field but preserve elevation
        double elevation = wb_supervisor_field_get_sf_vec3f(robots[i]->translation)[Y];
        double out_of_field[3] = { 1.0 * i, elevation, 5.0 };
        wb_supervisor_field_set_sf_vec3f(robots[i]->translation, out_of_field);
      }
    }
  }

  // inform robots of the penalty kick shootout
  control_data.secondaryState = STATE2_PENALTYSHOOT;

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

int main(int argc, const char *argv[]) {

  // init devices and data structures
  initialize();

  // check controllerArgs
  if (argc > 1 && strcmp(argv[1], "penalty") == 0)
    // run only penalty kicks
    run_penalty_kick_shootout();
  else {
    // first half-time
    control_data.firstHalf = 1;
    run_half_time();
    run_half_time_break();

    // second half-time
    control_data.firstHalf = 0;
    run_half_time();

    // if the game is tied, start penalty kicks
    if (control_data.teams[TEAM_BLUE].score == control_data.teams[TEAM_RED].score)
      run_penalty_kick_shootout();
  }

  // terminate movie, write scores.txt, etc.
  terminate();

  return 0; // never reached
}
