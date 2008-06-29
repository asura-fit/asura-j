package jp.ac.fit.asura.nao.communication;
//---------------------------------------------------------------------------------------
//  File:         RoboCupGameControlData.java (to be used in a Webots java controllers)
//  Date:         May 20, 2008
//  Description:  For decoding a the bytes of a RoboCupGameControlData sent by nao_soccer_supervisor.c
//  Project:      Robotstadium, the online robot soccer competition
//  Author:       Yvan Bourquin, Cyberbotics Ltd.
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

  private int playersPerTeam;
  private int state;
  private boolean firstHalf;
  private int kickOffTeam;
  private int secsRemaining;
  private short[] score = new short[2]; 
  private float[] ballPos = new float[2];

  public RoboCupGameControlData() {
  }

  static boolean hasValidHeader(byte[] bytes) {
    return bytes[0] == 82 && bytes[1] == 71 && bytes[2] == 109 && bytes[3] == 101;
  }

  public void update(byte[] bytes) {
    ByteBuffer buffer = ByteBuffer.allocate(bytes.length);
    buffer.order(ByteOrder.LITTLE_ENDIAN);
    buffer.put(bytes);
    buffer.rewind();
    buffer.getInt();  // skip header
    playersPerTeam = buffer.get();
    state = buffer.get();
    firstHalf = buffer.get() == 1;
    kickOffTeam = buffer.get();
    secsRemaining = buffer.getInt();
    score[0] = buffer.getShort();
    score[1] = buffer.getShort();
    ballPos[0] = buffer.getFloat();
    ballPos[1] = buffer.getFloat();
  }

  public int getPlayersPerTeam() {
    return playersPerTeam;
  }

  public int getState() {
    return state;
  }

  public boolean getFirstHalf() {
    return firstHalf;
  }

  public int getKickOffTeam() {
    return kickOffTeam;
  }

  public int getSecsRemaining() {
    return secsRemaining;
  }

  public short getScore(int team) {
    return score[team];
  }

  // getBallPosX() and getBallPosY() will be disabled during contest matches
  // they should be used for training purposes only
  public float getBallPosX() {
    return ballPos[0];
  }

  public float getBallPosY() {
    return ballPos[1];
  }

  public String toString() {
    return "playersPerTeam: " + playersPerTeam +
      ", state: " + state +
      ", firstHalf: " + firstHalf +
      ", kickOffTeam: " + kickOffTeam +
      ", secsRemaining: " + secsRemaining +
      ", score[0]: " + score[0] +
      ", score[1]: " + score[1] +
      ", ballPosX: " + ballPos[0] +
      ", ballPosY: " + ballPos[1];
  }
}
