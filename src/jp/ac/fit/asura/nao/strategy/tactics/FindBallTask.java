/*
 * 作成日: 2008/06/13
 */
package jp.ac.fit.asura.nao.strategy.tactics;

import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.vision.VisualObjects;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class FindBallTask extends Task {

	private int onSiteTime;

	public String getName() {
		return "FindBallTask";
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(300);
		onSiteTime = 100;
	}

	public void continueTask(StrategyContext context) {
		if (context.getBall().getConfidence() > 0) {
			context.makemotion(Motions.MOTION_STOP);
			context.getScheduler().abort();
			return;
		}

		if (onSiteTime > 0) {
			onSiteTime--;
			context.makemotion(Motions.MOTION_KAGAMI);
		} else {
			context.makemotion(Motions.MOTION_RIGHT_YY_TURN);
		}
	}
}
