/*
 * 作成日: 2008/12/31
 */
package jp.ac.fit.asura.nao.event;

import java.util.EventListener;

import jp.ac.fit.asura.nao.physical.Robot;

/**
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public interface RobotFrameEventListener extends EventListener {
	public void updateRobot(Robot newRobot);
}
