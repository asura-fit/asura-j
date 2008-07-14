/*
 * 作成日: 2008/07/07
 */
package jp.ac.fit.asura.nao.strategy.actions;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.motion.parameterized.ShootAction.LeftShootAction;
import jp.ac.fit.asura.nao.motion.parameterized.ShootAction.RightShootAction;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class BackShootTask extends Task {
	private Logger log = Logger.getLogger(BackShootTask.class);

	public String getName() {
		return "BackShootTask";
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

		log.debug("ball dist:" + ball.getDistance() + " head:"
				+ ball.getHeading());

		int motionId = Motions.MOTION_BACKSHOT_LEFT;
		int x = ball.getX() - context.getSelf().getX();
		int y = ball.getY() - context.getSelf().getY();

		if (ball.getHeading() > 0) {
			motionId = Motions.MOTION_BACKSHOT_RIGHT;
		} else {
			motionId = Motions.MOTION_BACKSHOT_LEFT;
		}

		context.makemotion(motionId);
		context.getScheduler().setTTL(50);
	}

	public void leave(StrategyContext context) {
		context.getBall().invalidate();
	}

	public void continueTask(StrategyContext context) {

	}
}
