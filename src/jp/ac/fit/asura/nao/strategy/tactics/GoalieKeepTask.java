/**
 *
 */
package jp.ac.fit.asura.nao.strategy.tactics;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask.Mode;

import org.apache.log4j.Logger;

/**
 * @author kilo
 *
 */
public class GoalieKeepTask extends Task {
	private Logger log = Logger.getLogger(getClass());

	private BallTrackingTask tracking;

	private int step;

	public String getName() {
		return "GoalieKeepTask";
	}

	public void init(RobotContext context) {
		tracking = (BallTrackingTask) context.getStrategy().getTaskManager()
				.find("BallTracking");
		assert tracking != null;
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(400);
		step = 0;
		super.enter(context);
	}

	public void continueTask(StrategyContext context) {
		WorldObject ball = context.getBall();
		WorldObject self = context.getSelf();

		int balld = ball.getDistance();
		float ballh = ball.getHeading();

		tracking.setMode(Mode.Cont);

		if (balld > 700) {
			// ボールが遠い
			if (Math.abs(ballh) > 50) {
				// ボールの方向を向いていない
				if (ballh < 0) {
					context.makemotion(Motions.MOTION_RIGHT_YY_TURN);
				} else {
					context.makemotion(Motions.MOTION_LEFT_YY_TURN);
				}
			} else {
				// ボールの方向を向いている
				context.makemotion(Motions.MOTION_STOP);
			}
		} else {
			// ボールが近い
			if (ballh > 15) {
				context.makemotion(Motions.MOTION_SIDEKEEP_LEFT);
			} else if (ballh < -15) {
				context.makemotion(Motions.MOTION_SIDEKEEP_RIGHT);
			} else {
				context.makemotion(Motions.MOTION_STOP);
//				context.getScheduler().abort();
//				context.pushQueue("ShootTask");
				return;
			}
			// if (ballh > 0) {
			// context.makemotion(Motions.MOTION_KAKICK_LEFT);
			// } else {
			// context.makemotion(Motions.MOTION_KAKICK_RIGHT);
			// }
		}

		step++;
		super.continueTask(context);
	}

}
