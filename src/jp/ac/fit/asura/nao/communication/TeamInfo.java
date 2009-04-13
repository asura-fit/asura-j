package jp.ac.fit.asura.nao.communication;

//---------------------------------------------------------------------------------------
//File:         TeamInfo.java (to be used in a Webots java controllers)
//Date:         February 26, 2009
//Description:  This class is used for reading a binary TeamInfo struct inside a
//            RoboCupGameControlData struct sent by nao_soccer_supervisor.c
//Project:      Robotstadium, the online robot soccer competition
//Author:       Yvan Bourquin - www.cyberbotics.com
//---------------------------------------------------------------------------------------

import java.nio.ByteBuffer;

public class TeamInfo {

public static final int MAX_NUM_PLAYERS = 4;

private byte teamColour;
private short score;

// each team has max 4 players
private RobotInfo[] players = new RobotInfo[MAX_NUM_PLAYERS];

public TeamInfo(byte teamColour) {
this.teamColour = teamColour;
for (int i = 0; i < MAX_NUM_PLAYERS; i++)
  players[i] = new RobotInfo();
}

public void readBytes(ByteBuffer buffer) {
buffer.get();  // skip teamNumber unused
teamColour = buffer.get();
score = buffer.getShort();
for (int i = 0; i < MAX_NUM_PLAYERS; i++)
  players[i].readBytes(buffer);
}

// get the robots in the team, return as an array
public RobotInfo[] getPlayers() {
return players;
}

public byte getTeamColour() {
return teamColour;
}

public short getScore() {
return score;
}

public void debug() {
System.out.println("  teamColour: " + teamColour);
System.out.println("  score: " + score);
for (int i = 0; i < MAX_NUM_PLAYERS; i++)
  players[i].debug();
}
}
