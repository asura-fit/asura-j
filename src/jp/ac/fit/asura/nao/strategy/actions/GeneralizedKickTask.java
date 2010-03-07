/**
 *
 */
package jp.ac.fit.asura.nao.strategy.actions;

import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.event.MotionEventListener;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.misc.MedianFilter;
import jp.ac.fit.asura.nao.misc.Filter.BooleanFilter;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.MotionParam;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;
import jp.ac.fit.asura.nao.vision.perception.BallVisualObject;
import jp.ac.fit.asura.nao.vision.perception.VisualObject;

import org.apache.log4j.Logger;

/**
 * @author sey
 *
 */
public class GeneralizedKickTask extends Task implements MotionEventListener {
	private Logger log = Logger.getLogger(GeneralizedKickTask.class);
	private boolean motionStarted;
	private boolean motionStopped;
	private int count;

	private BallTrackingTask tracking;

	public String getName() {
		return "GeneralizedKickTask";
	}

	public boolean canExecute(StrategyContext context) {
		WorldObject ball = context.getBall();
		if (ball.getConfidence() < 50)
			return false;
		if (ball.getDistance() > 270) {
			return false;
		}
		if (Math.abs(ball.getHeading()) > 60)
			return false;
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
	}

	public void leave(StrategyContext context) {
		context.getBall().invalidate();
	}

	public void continueTask(StrategyContext context) {
		if (count > 10 && !motionStarted) {
			WorldObject ball = context.getBall();

			log.debug("ball conf:" + ball.getConfidence() + " dist:"
					+ ball.getDistance() + " head:" + ball.getHeading());

			VisualObject vo = ball.getVision();
			if (vo != null) {
				BallVisualObject bvo = (BallVisualObject) vo;
				context.makemotion(Motions.GENERALIZED_KICK,
						new MotionParam.ShotParam(bvo.robotPosition, 0, 0));
				// context.makemotion(Motions.GENERALIZED_KICK,
				// new MotionParam.ShotParam(new Vector3f(-50, -310, 100),
				// 0, 0));
			} else {
				log.warn("Ball not found.");
			}
			context.getScheduler().setTTL(25);
		} else {
			tracking.setMode(BallTrackingTask.Mode.Cont);
		}
		if (!motionStopped)
			context.getScheduler().setTTL(10);
		count++;
	}

	@Override
	public void startMotion(Motion motion) {
		if (motion.getId() == Motions.GENERALIZED_KICK) {
			motionStarted = true;
		}
	}

	@Override
	public void stopMotion(Motion motion) {
		if (motion.getId() == Motions.GENERALIZED_KICK) {
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
