//---------------------------------------------------------------------------------------
//  File:         nao_soccer_supervisor.c
//  Project:      Robotstadium, the online robot soccer competition
//  Date:         April 30, 2008
//  Description:  Supervisor controller for Robostadium/Nao soccer worlds
//                (You do not need to modify this file for the Robotstadium competition)
//                This controller has several purposes: control the current game state,
//                counts the goals, displays the scores, moves robots and ball to kick-off
//                position, simulate the RoboCup game controller by sending data
//                to the players every 500 ms
//  Author:       Olivier Michel & Yvan Bourquin, Cyberbotics Ltd.
//---------------------------------------------------------------------------------------

#include <stdio.h>
#include <string.h>
#include <device/robot.h>
#include <device/supervisor.h>
#include <device/emitter.h>
#include "RoboCupGameControlData.h"

#define ROBOTS 8             // number of robots
#define BALL (ROBOTS*4)
#define BALL_X BALL
#define BALL_Y (BALL+1)
#define FIELD_X_LIMIT 3      // meters
#define FIELD_Y_LIMIT 2      // meters
#define TIME_STEP 40         // milliseconds
#define MAX_TIME (10*60)     // a match lasts for 10 minutes

static NodeRef robot[ROBOTS];
static NodeRef ball;
static DeviceTag emitter;
static float position[BALL+2];  // current ball position
static float time = MAX_TIME;
static int stepCount = 0;
static int i;                   // super global loop index
static struct RoboCupGameControlData controlData;

static void display() {

  // display score
  char text[64];
  sprintf(text, "%d", controlData.teams[0].score);
  supervisor_set_label(0,text,0.67,0.01,0.1,0x0000ff); // blue
  sprintf(text, "%d", controlData.teams[1].score);
  supervisor_set_label(1,text,0.29,0.01,0.1,0xff0000); // red

  // display state
  if (controlData.state == STATE_PLAYING)
    sprintf(text,"%02d:%02d",(int)(time/60),(int)time%60);
  else {
    static const char *STATE_NAMES[5] = { "INITIAL", "READY", "SET", "PLAYING", "FINISHED" };
    sprintf(text, STATE_NAMES[controlData.state]);
  }
  int length = strlen(text);
  supervisor_set_label(3,text,0.51-0.018*length,0.01,0.1,0x000000); // black
}

static void sendGameControlData() {
  // prepare and send game control data
  controlData.secsRemaining = (uint32)time;
  controlData.ballXPos = position[BALL_X];
  controlData.ballYPos = position[BALL_Y];
  emitter_send_packet(emitter, &controlData, sizeof(controlData));
}

static void reset(void) {
  // initialize game control data
  memset(&controlData, 0, sizeof(controlData));
  memcpy(controlData.header, GAMECONTROLLER_STRUCT_HEADER, sizeof(GAMECONTROLLER_STRUCT_HEADER));
  controlData.playersPerTeam = ROBOTS / 2;
  controlData.firstHalf = 1;
  controlData.kickOffTeam = TEAM_RED;

  // emitter for sending game control data
  emitter=robot_get_device("emitter");

  // get robot handles for moving them to kick-off position
  const char *ROBOT_DEF_NAMES[ROBOTS] = {
    "RED_GOAL_KEEPER", "RED_PLAYER_1", "RED_PLAYER_2", "RED_PLAYER_3",
    "BLUE_GOAL_KEEPER","BLUE_PLAYER_1","BLUE_PLAYER_2","BLUE_PLAYER_3"
  };
  for(i=0;i<ROBOTS;i++)
    robot[i] = supervisor_node_get_from_def(ROBOT_DEF_NAMES[i]);

  // keep track of ball position
  ball = supervisor_node_get_from_def("BALL");
  supervisor_field_get(ball,
                       SUPERVISOR_FIELD_TRANSLATION_X |
                       SUPERVISOR_FIELD_TRANSLATION_Z,
                       &position[BALL], TIME_STEP);
}

static void step() {

  // run virtual time
  robot_step(TIME_STEP);

  if (stepCount++ % 12 == 0) // every 480 milliseconds
    sendGameControlData();
}

static void place_to_kickoff() {
  const float RED_KICK_OFF[ROBOTS][7]={
    { 2.950, 0.325, 0.000, 0, 1, 0,-1.57 },
    { 0.625, 0.325, 0.000, 0, 1, 0,-1.57 },
    { 2.000, 0.325, 1.200, 0, 1, 0,-1.57 },
    { 2.000, 0.325,-1.200, 0, 1, 0,-1.57 },
    {-2.950, 0.325, 0.000, 0, 1, 0, 1.57 },
    {-2.000, 0.325,-0.300, 0, 1, 0, 1.57 },
    {-2.000, 0.325, 1.200, 0, 1, 0, 1.57 },
    {-2.000, 0.325,-1.200, 0, 1, 0, 1.57 }
  };
  const float BLUE_KICK_OFF[ROBOTS][7]={
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
  if (controlData.kickOffTeam == TEAM_RED)
    robot_initial_position=RED_KICK_OFF[0];
  else
    robot_initial_position=BLUE_KICK_OFF[0];

  for(i=0;i<ROBOTS;i++) {
    supervisor_field_set(robot[i],
                         SUPERVISOR_FIELD_TRANSLATION |
                         SUPERVISOR_FIELD_ROTATION,
                         &robot_initial_position[i*7]);
  }

  // reset ball position
  const float ball_initial_position[2]={0.0f, 0.0f};
  supervisor_field_set(ball,
                       SUPERVISOR_FIELD_TRANSLATION_X |
                       SUPERVISOR_FIELD_TRANSLATION_Z,
                       ball_initial_position);
}

// wait specified number of seconds while sending control data
static void wait_seconds(float seconds) {
  int n = 1000.0f * seconds / TIME_STEP;
  for (i = 0; i < n; i++) step();
}

static void run_initial_state() {
  controlData.state = STATE_INITIAL;
  display();
  wait_seconds(5);
}

static void run_ready_state() {
  controlData.state = STATE_READY;
  display();
  wait_seconds(5);
  place_to_kickoff();
}

static void run_set_state() {
  controlData.state = STATE_SET;
  display();
  wait_seconds(5);
}

static void run_playing_state() {

  controlData.state = STATE_PLAYING;

  while (1) {
    // substract TIME_STEP to current time
    time -= (float)TIME_STEP/1000.0f;
    display();

    if (time < 0.0f) {
      time = 0.0f;
      controlData.state = STATE_FINISHED;
      return;
    }

    if (position[BALL_X]>FIELD_X_LIMIT && position[BALL_X]<FIELD_X_LIMIT+0.25 &&
        position[BALL_Y]<0.75 && position[BALL_Y]>-0.75) {  // ball in the red goal
      controlData.teams[0].score++;
      controlData.state = STATE_READY;
      controlData.kickOffTeam = TEAM_RED;
      return;
    }
    else if (position[BALL_X]<-FIELD_X_LIMIT && position[BALL_X]>-FIELD_X_LIMIT-0.25 &&
             position[BALL_Y]<0.75 && position[BALL_Y]>-0.75) {  // ball in the blue goal
      controlData.teams[1].score++;
      controlData.state = STATE_READY;
      controlData.kickOffTeam = TEAM_BLUE;
      return;
    }
    step();
  }
}

static void run_finished_state() {
  controlData.state = STATE_FINISHED;
  display();
  while (1) step();  // wait forever
}

static int run(int ms) {

  run_initial_state();

  do {
    run_ready_state();
    run_set_state();
    run_playing_state();
  }
  while (controlData.state != STATE_FINISHED);

  run_finished_state();

  return 1; // never reached
}

int main() {
  robot_live(reset);
  robot_run(run); // never returns
  return 0;
}
