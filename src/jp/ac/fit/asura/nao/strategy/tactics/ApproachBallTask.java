/*
 * 作成日: 2008/06/13
 */
package jp.ac.fit.asura.nao.strategy.tactics;

import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class ApproachBallTask extends Task {
	public String getName() {
		return "ApproachBallTask";
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(300);
	}

	public void continueTask(StrategyContext context) {
		WorldObject self = context.getSelf();
		WorldObject ball = context.getBall();
		if (ball.getConfidence() == 0) {
			context.getScheduler().abort();
			return;
		}

		float ballh = ball.getHeading();
		if (ballh > 30) {
			context.makemotion(Motions.MOTION_LEFT_YY_TURN);
		} else if (ballh < -30) {
			context.makemotion(Motions.MOTION_RIGHT_YY_TURN);
		} else if (ball.getDistance() > 700) {
			context.makemotion(Motions.MOTION_YY_FORWARD);
		} else {
			// ボールが近い
			int goalx = 0;
			int goaly = 2700;
			// ゴールとの相対角度
			double deg = MathUtils.normalizeAngle180((float) Math
					.toDegrees(Math.atan2(goaly - self.getY(), goalx
							- self.getX())) - 90
					+ self.getYaw() - 90);

			if (MathUtils.rand(0, 20) == 0) {
				System.out.print(String.format(
						"current position x:%d y:%d h:%f\n", self.getX(), self
								.getY(), self.getYaw()));
				System.out.println("Ball distance:" + ball.getDistance());
				System.out.println("Deg:" + deg);
			}
			if (Math.abs(deg) < 30) {
				// ゴールの方向なら方向をあわせて直進
				if (ballh > 20) {
					context.makemotion(Motions.MOTION_LEFT_YY_TURN);
				} else if (ballh < -20) {
					context.makemotion(Motions.MOTION_RIGHT_YY_TURN);
				} else {
					context.makemotion(Motions.MOTION_YY_FORWARD);
				}
			} else if (deg > 0) {
				// 左側にゴールがある -> 左に回り込む？
				context.makemotion(Motions.MOTION_CIRCLE_LEFT);
			} else {
				// 右側にゴールがある -> 右に回り込む？
				context.makemotion(Motions.MOTION_CIRCLE_RIGHT);
			}
		}
	}
}
