/*
 * 作成日: 2008/07/14
 */
package jp.ac.fit.asura.nao.strategy.actions;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.physical.Goal;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;

import org.apache.log4j.Logger;

/**
 * @author $Author: $
 *
 * @version $Id: $
 *
 */

public class FrontShotTask extends Task {

	private Logger log = Logger.getLogger(FrontShotTask.class);

	public String getName() {
		return "FrontShotTask";
	}

	public boolean canExecute(StrategyContext context) {
		WorldObject ball = context.getBall();
		if (ball.getConfidence() < 100)
			return false;
		if (ball.getDistance() > 400) {
			return false;
		}
		return true;
	}

	public void init(RobotContext context) {

	}

	public void enter(StrategyContext context) {
		WorldObject ball = context.getBall();

		log.debug("ball conf:" + ball.getConfidence() + " dist:"
				+ ball.getDistance() + " head:" + ball.getHeading());

		int motionId;
		float deg = ball.getHeading();
		if (deg > 0) {
			motionId = Motions.MOTION_SHOT_LEFT;
		} else {
			motionId = Motions.MOTION_SHOT_RIGHT;
		}
		
		context.makemotion(motionId);
		context.getScheduler().setTTL(25);
	}

	public void leave(StrategyContext context) {
		context.getBall().invalidate();
	}

	public void continueTask(StrategyContext context) {

	}
}
