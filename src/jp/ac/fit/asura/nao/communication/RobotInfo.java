package jp.ac.fit.asura.nao.communication;

//---------------------------------------------------------------------------------------
//File:         RobotInfo.java (to be used in a Webots java controllers)
//Date:         February 26, 2009
//Description:  This class is used to read a binary RobotInfo struct inside a
//            RoboCupGameControlData sent by nao_soccer_supervisor.c
//Project:      Robotstadium, the online robot soccer competition
//Author:       Yvan Bourquin - www.cyberbotics.com
//---------------------------------------------------------------------------------------

import java.nio.ByteBuffer;

public class RobotInfo {

// penalties
public static final short PENALTY_NONE = 0;
public static final short PENALTY_ILLEGAL_DEFENDER = 4;

private short penalty;             // the penalty state of the robot
private short secsTillUnpenalise;  // estimated seconds till unpenalised

public RobotInfo() {
}

public void readBytes(ByteBuffer buffer) {
penalty = buffer.getShort();
secsTillUnpenalise = buffer.getShort();
}

public short getPenalty() {
return penalty;
}

public short getSecsTillUnpenalised() {
  return secsTillUnpenalise;
}

public void debug() {
System.out.println("    penalty: " + penalty);
System.out.println("    secsTillUnpenalise: " + secsTillUnpenalise);
}
}
