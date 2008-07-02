/*
 * 作成日: 2008/06/13
 */
package jp.ac.fit.asura.nao.strategy.tactics;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;

import org.apache.log4j.Logger;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class ApproachBallTask extends Task {
	private Logger log = Logger.getLogger(getClass());

	private static final int MAX_PITCH = 45;
	private static final int MIN_PITCH = 0;

	private int step;

	private int destPitch;

	public String getName() {
		return "ApproachBallTask";
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(400);
		step = 0;
		destPitch = MAX_PITCH;
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
		} else if (ball.getDistance() > 1000) {
			context.makemotion(Motions.MOTION_YY_FORWARD);
		} else {
			// ボールが近い
			int goalx = 0;
			int goaly = 2700;
			// ゴールとの相対角度
			double deg = MathUtils.normalizeAngle180((float) Math
					.toDegrees(Math.atan2(goaly - self.getY(), goalx
							- self.getX()))
					- 90 + self.getYaw() - 90);

			if (context.getSuperContext().getFrame() % 50 == 0) {
				log.debug("Ball distance:" + ball.getDistance());
				log.debug("Deg:" + deg);
			}

			if (step > 150 && step < 200) {
				float pitch = context.getSuperContext().getSensor()
						.getJointDegree(Joint.HeadPitch);
				// 100～200stepの間は頭を上下に振る

				if (Math.abs(pitch - destPitch) < 2) {
					destPitch = destPitch == MAX_PITCH ? MIN_PITCH : MAX_PITCH;
				}
				context.makemotion_head_rel(0, -(pitch - destPitch) / 10.0f);
			}

			step++;
			if (Math.abs(deg) < 15) {
				// ゴールの方向なら方向をあわせて直進
				if (ballh > 15) {
					context.makemotion(Motions.MOTION_LEFT_YY_TURN);
				} else if (ballh < -15) {
					context.makemotion(Motions.MOTION_RIGHT_YY_TURN);
				} else if (ballh > 7) {
					context.makemotion(Motions.MOTION_CIRCLE_LEFT);
				} else if (ballh < -7) {
					context.makemotion(Motions.MOTION_CIRCLE_RIGHT);
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
