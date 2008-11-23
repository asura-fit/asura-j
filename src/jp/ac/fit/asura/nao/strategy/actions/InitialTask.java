/*
 * 作成日: 2008/06/13
 */
package jp.ac.fit.asura.nao.strategy.actions;

import jp.ac.fit.asura.nao.communication.RoboCupGameControlData;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;

/**
 * @author $Author: sey $
 * 
 * @version $Id$
 * 
 */
public class InitialTask extends Task {
	public String getName() {
		return "InitialTask";
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(100);
	}

	public void continueTask(StrategyContext context) {
		if(context.getGameState().getState() == RoboCupGameControlData.STATE_INITIAL){
			context.makemotion(Motions.MOTION_TAKA);
		}else{
			context.makemotion(Motions.MOTION_STOP);
		}
	}
}
