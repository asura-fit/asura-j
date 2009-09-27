/*
 * 作成日: 2008/06/12
 */
package jp.ac.fit.asura.nao.strategy;

import jp.ac.fit.asura.nao.communication.RoboCupGameControlData;

/**
 *
 * チームを表現します. RedとBlueがあります.
 *
 * @author $Author: sey $
 *
 * @version $Id: Team.java 709 2008-11-23 07:40:31Z sey $
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
