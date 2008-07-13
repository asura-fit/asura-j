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

	private ShootTask shootTask;

	private int stateChanged;
	private int prevState;

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

	public void before(StrategyContext context) {
		stateChanged++;
		if (context.getGameState().getState() != prevState) {
			stateChanged = 0;
			prevState = context.getGameState().getState();
		}
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(400);
	}

	public void continueTask(StrategyContext context) {
		WorldObject self = context.getSelf();
		WorldObject ball = context.getBall();
		if (ball.getConfidence() == 0) {
			context.getScheduler().abort();
			return;
		}
		context.getScheduler().setTTL(400);

		float ballh = ball.getHeading();
		int balld = ball.getDistance();

		int goalx = 0;
		int goaly = 2700 + Goal.Depth;
		// ゴールとの相対角度
		double deg = MathUtils.normalizeAngle180((float) Math.toDegrees(Math
				.atan2(goaly - self.getY(), goalx - self.getX()))
				- self.getYaw());

		if (balld < 255 && Math.abs(ballh) < 40) {
			if (Math.abs(deg) < 20) {
				if (Math.abs(ball.getX() - self.getX()) < 100) {
					if (context.getGameState().getKickOffTeam() == context
							.getTeam().toInt()) {
						// 簡易キックオフ対策
						if (stateChanged < 500) {
							log.debug("kickoff mode");
							context.makemotion(Motions.MOTION_YY_FORWARD_STEP);
							tracking.setMode(BallTrackingTask.Mode.Localize);
							return;
						}
					}
					context.getScheduler().abort();
					context.pushQueue("ShootTask");
					return;
				} else if (ball.getX() > 0)
					context.makemotion(Motions.MOTION_CIRCLE_LEFT);
				else
					context.makemotion(Motions.MOTION_CIRCLE_RIGHT);

				tracking.setMode(BallTrackingTask.Mode.LookFront);
				return;
			} else {
				context.makemotion(Motions.MOTION_W_BACKWARD);
				tracking.setMode(BallTrackingTask.Mode.Localize);
				return;
			}
		}

		int adjHead = 20;

		// 角度があってない
		if (ballh > adjHead) {
			context.makemotion(Motions.MOTION_LEFT_YY_TURN);
			tracking.setMode(BallTrackingTask.Mode.LookFront);
			return;
		} else if (ballh < -adjHead) {
			context.makemotion(Motions.MOTION_RIGHT_YY_TURN);
			tracking.setMode(BallTrackingTask.Mode.LookFront);
			return;
		} else if (balld > 440) {
			// ボールが遠いとき
			context.makemotion(Motions.MOTION_YY_FORWARD);
			tracking.setMode(BallTrackingTask.Mode.Localize);
			return;
		}

		// ボールが近い
		if (context.getSuperContext().getFrame() % 50 == 0) {
			log.debug("Deg:" + deg);
		}

		tracking.setMode(BallTrackingTask.Mode.Localize);
		if (Math.abs(deg) < 15) {
			// ゴールの方向なら方向をあわせてシュート！
			if (balld < 340) {
				context.makemotion(Motions.MOTION_YY_FORWARD_STEP);
			}
			context.makemotion(Motions.MOTION_YY_FORWARD);
		} else if (deg > 0) {
			// 左側にゴールがある -> 右に回り込む
			context.makemotion(Motions.MOTION_CIRCLE_RIGHT);
		} else {
			// 右側にゴールがある -> 左に回り込む
			context.makemotion(Motions.MOTION_CIRCLE_LEFT);
		}
		return;
	}
}
