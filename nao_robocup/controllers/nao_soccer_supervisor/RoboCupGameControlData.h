#ifndef RoboCupGameControlData_H
#define RoboCupGameControlData_H

//---------------------------------------------------------------------------------------
//  File:         RoboCupGameControlData.h
//  Date:         April 30, 2008
//  Description:  This is a rewritten version of the RoboCupGameControlData.h used by 
//                standard RoboCup Game Controller.
//                This simpified version is used for the Robotstadium simulation.
//                The RoboCupGameControlData struct contains global game information that
//                is sent to all players at 500 ms intervals.
//  Project:      Robotstadium, the online robot soccer competition
//  Author:       Yvan Bourquin, Cyberbotics Ltd.
//---------------------------------------------------------------------------------------

// data structure header
#define GAMECONTROLLER_STRUCT_HEADER    "RGme"

// team numbers
#define TEAM_BLUE                   0
#define TEAM_RED                    1

// game states
#define STATE_INITIAL               0
#define STATE_READY                 1
#define STATE_SET                   2
#define STATE_PLAYING               3
#define STATE_FINISHED              4

typedef unsigned char uint8;
typedef unsigned int uint32;
typedef unsigned short uint16;

// information that describes a team
struct TeamInfo {
  uint16 score;              // team's score
};

struct RoboCupGameControlData {
  // subset of the original RoboCupGameControlData fields:
  char header[4];             // header to identify the structure
  uint8 playersPerTeam;       // The number of players on a team
  uint8 state;                // state of the game (STATE_READY, STATE_PLAYING, etc)
  uint8 firstHalf;            // 1 = game in first half, 0 otherwise
  uint8 kickOffTeam;          // the next team to kick off
  uint32 secsRemaining;       // estimate of number of seconds remaining in the half
  struct TeamInfo teams[2];

  // Webots extra: global ball position
  // FOR TRAINING ONLY: THIS WILL BE DISABLED IN THE CONTEST MATCHES !!!
  float ballXPos;
  float ballYPos;
};

#endif
