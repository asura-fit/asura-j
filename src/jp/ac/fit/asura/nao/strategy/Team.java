/*
 * 作成日: 2008/06/12
 */
package jp.ac.fit.asura.nao.strategy;

import jp.ac.fit.asura.nao.communication.RoboCupGameControlData;

/**
 * @author $Author: sey $
 * 
 * @version $Id$
 * 
 */
public enum Team {
	Red, Blue;

	public int toInt() {
		if (this == Red) {
			return RoboCupGameControlData.TEAM_RED;
		} else {
			return RoboCupGameControlData.TEAM_BLUE;
		}
	}
}
