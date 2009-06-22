/*
 * 作成日: 2008/07/14
 */
package jp.ac.fit.asura.nao.strategy.actions;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.event.MotionEventListener;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.misc.MedianFilter;
import jp.ac.fit.asura.nao.misc.Filter.BooleanFilter;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.physical.Goal;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 *
 * @version $Id: InsideKickTask.java 709 2008-11-23 07:40:31Z sey $
 *
 */
@Deprecated
public class InsideKickTask extends Task implements MotionEventListener {

	private Logger log = Logger.getLogger(InsideKickTask.class);
	private boolean motionStarted;
	private boolean motionStopped;
	private int count;
	private BooleanFilter filter = new MedianFilter.Boolean(5);

	private BallTrackingTask tracking;

	public String getName() {
		return "InsideKickTask";
	}

	public boolean canExecute(StrategyContext context) {
		WorldObject ball = context.getBall();
		if (ball.getConfidence() < 100)
			return false;
		if (ball.getDistance() > 350) {
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
	}

	public void leave(StrategyContext context) {
		context.getBall().invalidate();
	}

	public void continueTask(StrategyContext context) {
		// tracking.setMode(BallTrackingTask.Mode.Cont);
		boolean can = filter.eval(canExecute(context));
		if (count > 10 && !motionStarted) {
			// if (!can) {
			// context.getScheduler().abort();
			// return;
			// }
			WorldObject ball = context.getBall();
			WorldObject self = context.getSelf();
			WorldObject goal = context.getTargetGoal();

			log.debug("ball dist:" + ball.getDistance() + " head:"
					+ ball.getHeading());

			int motionId;
			int goalx = goal.getX(); // 0;
			int goaly = goal.getY(); // 2700 + Goal.Depth;
			// ゴールとの相対角度
			// float deg = MathUtils.normalizeAngle180(MathUtils
			// .toDegrees(MathUtils.atan2(goalx - self.getX(), goaly
			// - self.getY()))
			// - self.getYaw());
			float deg = MathUtils.normalizeAngle180(MathUtils
					.toDegrees(MathUtils.atan2(Goal.BlueGoalX - self.getX(),
							Goal.BlueGoalY - self.getY()))
					- self.getYaw());
			if (deg > 0) {
				motionId = Motions.MOTION_SHOT_W_INSIDE_RIGHT;
			} else {
				motionId = Motions.MOTION_SHOT_W_INSIDE_LEFT;
			}

			context.makemotion(motionId);
			context.getScheduler().setTTL(25);
			// context.getSuperContext().getEffector().say("Inside shot!");
		} else {
			tracking.setMode(BallTrackingTask.Mode.Cont);
		}
		if (!motionStopped)
			context.getScheduler().setTTL(10);
		count++;
	}

	@Override
	public void startMotion(Motion motion) {
		if (motion.getId() == Motions.MOTION_SHOT_W_INSIDE_LEFT
				|| motion.getId() == Motions.MOTION_SHOT_W_INSIDE_RIGHT) {
			motionStarted = true;
		}
	}

	@Override
	public void stopMotion(Motion motion) {
		if (motion.getId() == Motions.MOTION_SHOT_W_INSIDE_LEFT
				|| motion.getId() == Motions.MOTION_SHOT_W_INSIDE_RIGHT) {
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
