/*
 * 作成日: 2008/06/13
 */
package jp.ac.fit.asura.nao.strategy.tactics;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.misc.PhysicalConstants.Goal;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.actions.ShootTask;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;

import org.apache.log4j.Logger;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class ApproachBallTask extends Task {
	private Logger log = Logger.getLogger(getClass());

	private BallTrackingTask tracking;

	private int step;

	private ShootTask shootTask;

	public String getName() {
		return "ApproachBallTask";
	}

	public void init(RobotContext context) {
		shootTask = (ShootTask) context.getStrategy().getTaskManager().find(
				"ShootTask");
		tracking = (BallTrackingTask) context.getStrategy().getTaskManager()
				.find("BallTracking");
		assert shootTask != null;
		assert tracking != null;
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(400);
		step = 0;
	}

	public void continueTask(StrategyContext context) {
		WorldObject self = context.getSelf();
		WorldObject ball = context.getBall();
		if (ball.getConfidence() == 0) {
			context.getScheduler().abort();
			return;
		}

		float ballh = ball.getHeading();
		int balld = ball.getDistance();

		int goalx = 0;
		int goaly = 2700 + Goal.Depth * 2;
		// ゴールとの相対角度
		double deg = MathUtils.normalizeAngle180((float) Math.toDegrees(Math
				.atan2(goaly - self.getY(), goalx - self.getX()))
				- self.getYaw());

		if (balld < 250 && Math.abs(ballh) < 60) {
			if (Math.abs(deg) < 20 && Math.abs(ball.getX() - self.getX()) < 200) {
				context.getScheduler().abort();
				context.pushQueue("ShootTask");
				return;
			} else {
				context.makemotion(Motions.MOTION_W_BACKWARD);
				tracking.setMode(BallTrackingTask.Mode.Localize);
				return;
			}
		}

		int adjHead = 10000 / (Math.min(balld, 500));

		// 角度があってない
		// dist = 500で30度ぐらい
		if (ballh > adjHead) {
			context.makemotion(Motions.MOTION_LEFT_YY_TURN);
			return;
		} else if (ballh < -adjHead) {
			context.makemotion(Motions.MOTION_RIGHT_YY_TURN);
			return;
		} else if (balld > 350) {
			// ボールが遠いとき
			context.makemotion(Motions.MOTION_YY_FORWARD);
			tracking.setMode(BallTrackingTask.Mode.Localize);
			return;
		}

		// ボールが近い
		if (context.getSuperContext().getFrame() % 50 == 0) {
			log.debug("Deg:" + deg);
		}

		step++;
		if (Math.abs(deg) < 20) {
			// ゴールの方向なら方向をあわせてシュート！
			context.makemotion(Motions.MOTION_YY_FORWARD);
		} else if (deg > 0) {
			// 左側にゴールがある -> 右に回り込む
			context.makemotion(Motions.MOTION_CIRCLE_RIGHT);
			tracking.setMode(BallTrackingTask.Mode.Localize);
		} else {
			// 右側にゴールがある -> 左に回り込む
			context.makemotion(Motions.MOTION_CIRCLE_LEFT);
			tracking.setMode(BallTrackingTask.Mode.Localize);
		}
		return;
	}
}
