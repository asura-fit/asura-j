package jp.ac.fit.asura.nao.communication;

//---------------------------------------------------------------------------------------
//  File:         RoboCupGameControlData.java (to be used in a Webots java controllers)
//  Date:         May 20, 2008
//  Description:  For decoding the bytes of a RoboCupGameControlData struct sent by nao_soccer_supervisor.c
//  Project:      Robotstadium, the online robot soccer competition
//  Author:       Yvan Bourquin - www.cyberbotics.com
//---------------------------------------------------------------------------------------

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class RoboCupGameControlData {

  // team colours
  public static final byte TEAM_BLUE = 0;
  public static final byte TEAM_RED  = 1;

  // game states
  public static final byte STATE_INITIAL  = 0;
  public static final byte STATE_READY    = 1;
  public static final byte STATE_SET      = 2;
  public static final byte STATE_PLAYING  = 3;
  public static final byte STATE_FINISHED = 4;

  // secondary game states
  public static final byte STATE2_NORMAL       = 0;
  public static final byte STATE2_PENALTYSHOOT = 1;

  private byte playersPerTeam;
  private byte state = STATE_PLAYING;
  private byte firstHalf;
  private byte kickOffTeam;
  private byte secondaryState;
  private int secsRemaining;
  private TeamInfo[] teams = new TeamInfo[2];  // two Teams: red and a blue
  private float ballPosX;
  private float ballPosZ;

  public RoboCupGameControlData() {
    teams[0] = new TeamInfo(TEAM_BLUE);
    teams[1] = new TeamInfo(TEAM_RED);
  }

  static boolean hasValidHeader(byte[] bytes) {
    return bytes[0] == 82 && bytes[1] == 71 && bytes[2] == 109 && bytes[3] == 101;
  }

  public void update(byte[] bytes) {
    ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    buffer.put(bytes);
    buffer.rewind();  // start from the beginning
    buffer.getInt();  // skip header (4 bytes)
    buffer.getInt();  // skip unused struct version number
    playersPerTeam = buffer.get();
    state = buffer.get();
    firstHalf = buffer.get();
    kickOffTeam = buffer.get();
    secondaryState = buffer.get();
    buffer.get();       // skip unsupported dropInTeam
    buffer.getShort();  // skip unsupported dropInTime
    secsRemaining = buffer.getInt();
    teams[0].readBytes(buffer);
    teams[1].readBytes(buffer);
    ballPosX = buffer.getFloat();
    ballPosZ = buffer.getFloat();
  }

  public byte getPlayersPerTeam() {
    return playersPerTeam;
  }

  public byte getState() {
    return state;
  }

  public byte getFirstHalf() {
    return firstHalf;
  }

  public byte getKickOffTeam() {
    return kickOffTeam;
  }

  public byte getSecondaryState() {
    return secondaryState;
  }

  public int getSecsRemaining() {
    return secsRemaining;
  }

  // return the specified team (0 = blue, 1 = red)
  public TeamInfo getTeam(byte team) {
    return teams[team];
  }

  // getBallPosX() and getBallPosZ() will be disabled during contest matches
  // they should be used for training purposes only
  public float getBallPosX() {
    return ballPosX;
  }

  public float getBallPosZ() {
    return ballPosZ;
  }

  public void debug() {
    System.out.println("playersPerTeam: " + playersPerTeam);
    System.out.println("state: " + state);
    System.out.println("firstHalf: " + firstHalf);
    System.out.println("kickOffTeam: " + kickOffTeam);
    System.out.println("secondaryState: " + secondaryState);
    System.out.println("secsRemaining: " + secsRemaining);
    teams[0].debug();
    teams[1].debug();
    System.out.println("ballPosX: " + ballPosX);
    System.out.println("ballPosZ: " + ballPosZ);
  }
}
