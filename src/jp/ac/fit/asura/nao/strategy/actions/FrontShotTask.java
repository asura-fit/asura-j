/*
 * 作成日: 2008/07/14
 */
package jp.ac.fit.asura.nao.strategy.actions;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.event.MotionEventListener;
import jp.ac.fit.asura.nao.localization.WorldObject;
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

	private BallTrackingTask tracking;

	public String getName() {
		return "FrontShotTask";
	}

	public boolean canExecute(StrategyContext context) {
		WorldObject ball = context.getBall();
		if (ball.getConfidence() < 100)
			return false;
		if (ball.getDistance() > 500) {
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
		motionStarted = motionStopped = false;
		count = 0;
	}

	public void leave(StrategyContext context) {
		context.getBall().invalidate();
	}

	public void continueTask(StrategyContext context) {
//		tracking.setMode(BallTrackingTask.Mode.Cont);
		if (count > 5 && !motionStarted) {
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
		if (!motionStopped)
			context.getScheduler().setTTL(10);
		count++;
	}

	@Override
	public void startMotion(Motion motion) {
		if (motion.getId() == Motions.MOTION_SHOT_LEFT
				|| motion.getId() == Motions.MOTION_SHOT_RIGHT) {
			motionStarted = true;
		}
	}

	@Override
	public void stopMotion(Motion motion) {
		if (motion.getId() == Motions.MOTION_SHOT_LEFT
				|| motion.getId() == Motions.MOTION_SHOT_RIGHT) {
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
