/*
 * 作成日: 2009/05/19
 */
package jp.ac.fit.asura.nao.event;

import java.util.EventListener;

import jp.ac.fit.asura.nao.communication.RoboCupGameControlData;

/**
 * @author sey
 *
 * @version $Id: $
 *
 */
public interface RoboCupMessageListener extends EventListener {
	public void update(RoboCupGameControlData gameData);
}
