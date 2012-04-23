
/*
 * 作成日: 2008/07/14
 */
package jp.ac.fit.asura.nao.strategy.actions;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.Camera.CameraID;
import jp.ac.fit.asura.nao.event.MotionEventListener;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.misc.AverageFilter;
import jp.ac.fit.asura.nao.misc.Filter.BooleanFilter;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;

import org.apache.log4j.Logger;

/**
 * @author $Author: $
 *
 * @version $Id: $
 *
 */

public class FrontShotTask extends Task implements MotionEventListener {
	private Logger log = Logger.getLogger(FrontShotTask.class);
	private boolean motionStarted;
	private boolean motionStopped;
	private int count;
	private BooleanFilter filter = new AverageFilter.Boolean(5);

	private BallTrackingTask tracking;

	public String getName() {
		return "FrontShotTask";
	}

	public boolean canExecute(StrategyContext context) {
		WorldObject ball = context.getBall();
//		if (ball.getConfidence() < 100)
//			return false;
//		if (ball.getDistance() > 280) {
//			return false;
//		}
		if (Math.abs(ball.getHeading()) > 35) {
			return false;
		}
		return true;
	}

	public void init(RobotContext context) {
		context.getMotor().addEventListener(this);
		tracking = (BallTrackingTask) context.getStrategy().getTaskManager()
				.find("BallTracking");
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(25);
		motionStarted = motionStopped = false;
		count = 0;
		context.getSuperContext().getEffector().setPower(1.0f);
		log.debug("set stiffnesses to 1.0.");
	}

	public void leave(StrategyContext context) {
		context.getBall().invalidate();
		if (context.hasMotion(Motions.NAOJI_WALKER)) {
			context.getSuperContext().getMotor().getWalker().setJointsStiffness();
		}
	}

	public void continueTask(StrategyContext context) {
		boolean can = filter.eval(canExecute(context));
		WorldObject ball = context.getBall();

		if (count > 4 && !motionStarted) {
			// if (!can) {
			// context.getScheduler().abort();
			// return;
			// }
			WorldObject goal = context.getTargetGoal();

			log.debug("ball conf:" + ball.getConfidence() + " dist:"
					+ ball.getDistance() + " head:" + ball.getHeading());

			int motionId;
			float deg = ball.getHeading();
			int goald = goal.getDistance();

			if (Math.abs(deg) < 30) {
				if (Math.abs(deg) > 4) {
					// 足の前
					if ( deg > 0) {
						if (goald > 1100)
							motionId = Motions.MOTION_SHOT_W_LEFT;
						else
							motionId = Motions.MOTION_STRONG_SHOT_LEFT;
					} else {
						if (goald > 1100)
							motionId = Motions.MOTION_SHOT_W_RIGHT;
						else
							motionId = Motions.MOTION_STRONG_SHOT_RIGHT;
					}
				} else {
					// 真ん中
					if (deg > 0) {
						motionId = Motions.MOTION_CBYS_SHOT_LEFT;
					} else {
						motionId = Motions.MOTION_CBYS_SHOT_RIGHT;
					}
				}
			} else {
				// 外側
				if (deg > 0) {
					motionId = Motions.MOTION_C_SHOT_LEFT;
				} else {
					motionId = Motions.MOTION_C_SHOT_RIGHT;
				}

			}

			context.makemotion(motionId);
			context.getScheduler().setTTL(25);
		} else {
			context.getSuperContext().getCamera().selectCamera(CameraID.BOTTOM);
			tracking.setMode(BallTrackingTask.Mode.Cont);
		}
		if (!motionStopped)
			context.getScheduler().setTTL(10);
		count++;
	}

	@Override
	public void startMotion(Motion motion) {
		if (motion.getId() == Motions.MOTION_SHOT_W_LEFT
				|| motion.getId() == Motions.MOTION_SHOT_W_RIGHT
				|| motion.getId() == Motions.MOTION_STRONG_SHOT_LEFT
				|| motion.getId() == Motions.MOTION_STRONG_SHOT_RIGHT
				|| motion.getId() == Motions.MOTION_C_SHOT_LEFT
				|| motion.getId() == Motions.MOTION_C_SHOT_RIGHT
				|| motion.getId() == Motions.MOTION_CBYS_SHOT_LEFT
				|| motion.getId() == Motions.MOTION_CBYS_SHOT_RIGHT) {
			motionStarted = true;
		}
	}

	@Override
	public void stopMotion(Motion motion) {
		if (motion.getId() == Motions.MOTION_SHOT_W_LEFT
				|| motion.getId() == Motions.MOTION_SHOT_W_RIGHT
				|| motion.getId() == Motions.MOTION_STRONG_SHOT_LEFT
				|| motion.getId() == Motions.MOTION_STRONG_SHOT_RIGHT
				|| motion.getId() == Motions.MOTION_C_SHOT_LEFT
				|| motion.getId() == Motions.MOTION_C_SHOT_RIGHT
				|| motion.getId() == Motions.MOTION_CBYS_SHOT_LEFT
				|| motion.getId() == Motions.MOTION_CBYS_SHOT_RIGHT) {
			motionStopped = true;
		}
	}

	@Override
	public void updateOdometry(float forward, float left, float turnCCW) {
	}

	@Override
	public void updatePosture() {
	}
}
