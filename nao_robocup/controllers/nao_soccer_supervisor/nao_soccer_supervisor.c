/*
 * File:         nao_soccer_supervisor.c
 * Date:         November 11th, 2007
 * Description:  Supervisor the soccer game from nao_soccer.wbt
 * Author:       Olivier Michel
 *
 * Copyright (c) 2007 Cyberbotics - www.cyberbotics.com
 */

#include <stdio.h>
#include <string.h>
#include <device/robot.h>
#include <device/supervisor.h>
#include <device/emitter.h>
#include <device/receiver.h>

#define ROBOTS 8                /* number of robots */
#define BALL (ROBOTS*4)
#define BALL_X BALL
#define BALL_Y (BALL+1)
#define FIELD_X_LIMIT 3
#define FIELD_Y_LIMIT 2
#define TIME_STEP 40

static const char *robot_name[ROBOTS]=
  { "RED_GOAL_KEEPER", "RED_PLAYER_1", "RED_PLAYER_2", "RED_PLAYER_3",
   "BLUE_GOAL_KEEPER","BLUE_PLAYER_1","BLUE_PLAYER_2","BLUE_PLAYER_3"};
static NodeRef robot[ROBOTS];
static NodeRef ball;
static DeviceTag emitter,receiver;
static float position[BALL+2];

static void set_scores(int b, int r) {
  char score[16];
  sprintf(score,"%d",b);
  supervisor_set_label(0,score,0.68,0.01,0.1,0x0000ff); /* blue */
  sprintf(score,"%d",r);
  supervisor_set_label(1,score,0.3,0.01,0.1,0xff0000); /* red */
}

static void reset(void) {
  int i;

  emitter=robot_get_device("emitter");
  receiver=robot_get_device("receiver");
  receiver_enable(receiver,TIME_STEP);
  for(i=0;i<ROBOTS;i++) {
    robot[i] = supervisor_node_get_from_def(robot_name[i]);
    supervisor_field_get(robot[i],
                         SUPERVISOR_FIELD_TRANSLATION_X |
                         SUPERVISOR_FIELD_TRANSLATION_Z |
                         SUPERVISOR_FIELD_ROTATION_Y    |
                         SUPERVISOR_FIELD_ROTATION_ANGLE,
                         &position[i*4],TIME_STEP);
  }
  ball = supervisor_node_get_from_def("BALL");
  supervisor_field_get(ball,
                       SUPERVISOR_FIELD_TRANSLATION_X |
                       SUPERVISOR_FIELD_TRANSLATION_Z,
                       &position[BALL], TIME_STEP);
  set_scores(0,0);
}

static int run(int ms) {
  static int score[2]={0,0};
  static float time=10*60,    /* a match lasts for 10 minutes */
               ball_reset_timer=0,
               ball_initial_position[2]={0,0},
               robot_initial_position_red_kick_off[ROBOTS][7]={
                 { 2.950, 0.325, 0.000, 0, 1, 0,-1.57},
                 { 0.625, 0.325, 0.000, 0, 1, 0,-1.57},
                 { 2.000, 0.325, 1.200, 0, 1, 0,-1.57},
                 { 2.000, 0.325,-1.200, 0, 1, 0,-1.57},
                 {-2.950, 0.325, 0.000, 0, 1, 0, 1.57},
                 {-2.000, 0.325,-0.300, 0, 1, 0, 1.57},
                 {-2.000, 0.325, 1.200, 0, 1, 0, 1.57},
                 {-2.000, 0.325,-1.200, 0, 1, 0, 1.57}},
               robot_initial_position_blue_kick_off[ROBOTS][7]={
                 { 2.950, 0.325, 0.000, 0, 1, 0,-1.57},
                 { 2.000, 0.325, 0.300, 0, 1, 0,-1.57},
                 { 2.000, 0.325, 1.200, 0, 1, 0,-1.57},
                 { 2.000, 0.325,-1.200, 0, 1, 0,-1.57},
                 {-2.950, 0.325, 0.000, 0, 1, 0, 1.57},
                 {-0.625, 0.325, 0.000, 0, 1, 0, 1.57},
                 {-2.000, 0.325, 1.200, 0, 1, 0, 1.57},
                 {-2.000, 0.325,-1.200, 0, 1, 0, 1.57}},
               *robot_initial_position=NULL,
               robot_position[7];
  int i,j;
  const char *text;
  char time_string[64];

  /* Adds TIME_STEP ms to the time */
  time-=(float)TIME_STEP/1000;
  if (time<0) { /* game over */
    time=10*60; /* restart */
  }
  sprintf(time_string,"%02d:%02d",(int)(time/60),(int)time%60);
  supervisor_set_label(2,time_string,0.45,0.01,0.1,0x000000); /* black */
  
  i = receiver_get_queue_length(receiver);
  while(i--) {
    text = receiver_get_data(receiver);
    if (sscanf(text,"move %d %f %f %f %f %f %f %f",&j,
           &robot_position[0],
           &robot_position[1],
           &robot_position[2],
           &robot_position[3],
           &robot_position[4],
           &robot_position[5],
           &robot_position[6])==8) {
      robot_console_printf("moving robot %d to T(%g,%g,%g) R(%g,%g,%g,%g)\n",j,
                           robot_position[0],
                           robot_position[1],
                           robot_position[2],
                           robot_position[3],
                           robot_position[4],
                           robot_position[5],
                           robot_position[6]);
      if (j==8) supervisor_field_set(ball,SUPERVISOR_FIELD_TRANSLATION|SUPERVISOR_FIELD_ROTATION,robot_position);
      else supervisor_field_set(robot[j],SUPERVISOR_FIELD_TRANSLATION|SUPERVISOR_FIELD_ROTATION,robot_position);
    }
    receiver_next_packet(receiver);
  }
  
  if (ball_reset_timer==0) {
    if (position[BALL_X]>FIELD_X_LIMIT && position[BALL_X]<FIELD_X_LIMIT+0.25 &&
        position[BALL_Y]<0.75 && position[BALL_Y]>-0.75) {  /* ball in the blue goal */
      set_scores(++score[0],score[1]);
      ball_reset_timer=3;   /* wait for 3 seconds before reseting the ball */
      robot_initial_position=robot_initial_position_red_kick_off[0];
    } else if (position[BALL_X]<-FIELD_X_LIMIT && position[BALL_X]>-FIELD_X_LIMIT-0.25 &&
               position[BALL_Y]<0.75 && position[BALL_Y]>-0.75) {  /* ball in the red goal */
      set_scores(score[0],++score[1]);
      ball_reset_timer=3; /* wait for 3 seconds before reseting the ball */
      robot_initial_position=robot_initial_position_blue_kick_off[0];
    }
  } else {
    ball_reset_timer-=(float)TIME_STEP/1000;
    if (ball_reset_timer<=0) {
      ball_reset_timer=0;
      supervisor_field_set(ball,
                           SUPERVISOR_FIELD_TRANSLATION_X |
                           SUPERVISOR_FIELD_TRANSLATION_Z,
                           ball_initial_position);
      for(i=0;i<ROBOTS;i++) {
        supervisor_field_set(robot[i],
                             SUPERVISOR_FIELD_TRANSLATION |
                             SUPERVISOR_FIELD_ROTATION,
                             &robot_initial_position[i*7]);
      }
    }
  }
  return TIME_STEP;
}

int main() {
  robot_live(reset);
  robot_run(run); /* never returns */
  return 0;
}
