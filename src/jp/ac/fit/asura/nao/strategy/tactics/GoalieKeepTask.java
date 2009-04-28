/**
 *
 */
package jp.ac.fit.asura.nao.strategy.tactics;

import static jp.ac.fit.asura.nao.motion.Motions.NAOJI_WALKER;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;

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

		log.trace("bc:" + ball.getConfidence()
				+ " bd:" + balld
				+ " bh:" + ballh
				+ " sh:" + self.getYaw());
		
		tracking.setMode(BallTrackingTask.Mode.Cont);

		if (ball.getConfidence() == 0) {
			context.makemotion(Motions.MOTION_STOP);
			context.getScheduler().abort();
			return;
		}
		
		if (balld > 1000) {
			// ボールが遠い
			if (Math.abs(ballh) > 35) {
				// ボールの方向を向いていない
				if (ballh < 0) {
					if (context.hasMotion(NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER,
								0, 0,//-balld * 0.025f / 1e3f,
								MathUtils.toRadians(0.45f * ballh));
					else
						context.makemotion(Motions.MOTION_W_RIGHT_SIDESTEP);
				} else {
					if (context.hasMotion(NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER,
								0, 0, //balld * 0.025f / 1e3f,
								MathUtils.toRadians(0.45f * ballh));
					else
						context.makemotion(Motions.MOTION_W_LEFT_SIDESTEP);
				}
			} else {
				// ボールの方向を向いている
				context.makemotion(Motions.MOTION_STOP);
				tracking.setMode(BallTrackingTask.Mode.Localize);
				return;
			}
		} else {
			
			float heading = 15.0f;
			
			// ボールが近い
			if (ballh > heading) {
				if (context.hasMotion(NAOJI_WALKER))
					context.makemotion(NAOJI_WALKER,
							0, balld * 0.25f / 1e3f, 0);
				else
					context.makemotion(Motions.MOTION_CIRCLE_LEFT);
			} else if (ballh < -heading) {
				if (context.hasMotion(NAOJI_WALKER))
					context.makemotion(NAOJI_WALKER, 
							0, -balld * 0.25f / 1e3f, 0);
				else
					context.makemotion(Motions.MOTION_CIRCLE_RIGHT);
			} else {
				context.makemotion(Motions.MOTION_STOP);
				tracking.setMode(BallTrackingTask.Mode.Localize);
				
				// context.getScheduler().abort();
				// context.pushQueue("ShootTask");
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
