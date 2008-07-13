/*
 * 作成日: 2008/07/14
 */
package jp.ac.fit.asura.nao.strategy.actions;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.misc.PhysicalConstants.Goal;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class InsideKickTask extends Task {

	private Logger log = Logger.getLogger(InsideKickTask.class);

	public String getName() {
		return "InsideKickTask";
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
		WorldObject self = context.getSelf();

		log.debug("ball dist:" + ball.getDistance() + " head:"
				+ ball.getHeading());

		int motionId;
		int goalx = 0;
		int goaly = 2700 + Goal.Depth;
		// ゴールとの相対角度
		double deg = MathUtils.normalizeAngle180((float) Math.toDegrees(Math
				.atan2(goaly - self.getY(), goalx - self.getX()))
				- self.getYaw());
		if (deg > 0) {
			motionId = Motions.MOTION_KAKICK_INSIDE_RIGHT;
		} else {
			motionId = Motions.MOTION_KAKICK_INSIDE_LEFT;
		}

		context.makemotion(motionId);
		context.getScheduler().setTTL(50);
	}

	public void continueTask(StrategyContext context) {

	}
}
