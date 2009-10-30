/*
 * 作成日: 2008/06/13
 */
package jp.ac.fit.asura.nao.strategy.tactics;

import static jp.ac.fit.asura.nao.motion.Motions.MOTION_CIRCLE_LEFT;
import static jp.ac.fit.asura.nao.motion.Motions.MOTION_CIRCLE_RIGHT;
import static jp.ac.fit.asura.nao.motion.Motions.MOTION_LEFT_YY_TURN;
import static jp.ac.fit.asura.nao.motion.Motions.MOTION_RIGHT_YY_TURN;
import static jp.ac.fit.asura.nao.motion.Motions.BASIC_WALK;
import static jp.ac.fit.asura.nao.motion.Motions.NAOJI_WALKER;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.physical.Goal;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 *
 * @version $Id: ApproachBallTask.java 709 2008-11-23 07:40:31Z sey $
 *
 */
public class ApproachBallTask extends Task {
	private static final Logger log = Logger.getLogger(ApproachBallTask.class);

	private BallTrackingTask tracking;

	// private ShootTask shootTask;

	private int ballLastSeenFrame;
	private int prevBalld;
	private float prevBallh;

	public String getName() {
		return "ApproachBallTask";
	}

	public void init(RobotContext context) {
		// shootTask = (ShootTask) context.getStrategy().getTaskManager().find(
		// "ShootTask");
		tracking = (BallTrackingTask) context.getStrategy().getTaskManager()
				.find("BallTracking");
		// assert shootTask != null;
		assert tracking != null;
	}

	public void before(StrategyContext context) {
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(400);

		ballLastSeenFrame = 0;
		prevBalld = 999;
		prevBallh = 0;
	}

	public void continueTask(StrategyContext context) {

		context.getScheduler().setTTL(400);

		WorldObject self = context.getSelf();
		WorldObject ball = context.getBall();

		float ballh;
		int balld;
		int goalx = context.getTargetGoal().getX();
		int goaly = context.getTargetGoal().getY();

		if (ball.getConfidence() == 0) {
			context.getScheduler().abort();
			return;
		}

		if (ball.getConfidence() > 0 && ball.getDistance() != 0) {
			balld = ball.getDistance();
			ballh = ball.getHeading();
			prevBalld = balld;
			// prevBallh = ballh;
		} else {
			balld = prevBalld;
			// ballh = prevBallh;
		}

		// balld = ball.getDistance();
		ballh = ball.getHeading();

		// ゴールとの相対角度
		float deg = MathUtils.normalizeAngle180(MathUtils.toDegrees(MathUtils
				.atan2(Goal.BlueGoalX - self.getX(), Goal.BlueGoalY
						- self.getY()))
				- self.getYaw());
		// float deg = MathUtils.normalizeAngle180(MathUtils.toDegrees(MathUtils
		// .atan2(goalx - self.getX(), goaly - self.getY()))
		// - self.getYaw());

		if (context.getFrame() % 3 == 0)
			log.trace("bc:" + ball.getConfidence() + " bd:" + balld + " bh:"
					+ ballh + " deg:" + deg + " syaw:" + self.getYaw());

		if (balld < 250) {
			if (Math.abs(ballh) < 25) {
				// ボールが足元にある
				if (Math.abs(deg) < 35) {
					// ゴールは前方

					if (Math.abs(ballh) < 15) {

						log.debug("front shot dist:" + balld);
						context.makemotion(Motions.MOTION_STOP);
						context.getScheduler().abort();
						context.pushQueue("FrontShotTask");

					} else {

						if (ballh > 0) {
							if (context.hasMotion(NAOJI_WALKER))
								context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
										.toRadians(0.4f * ballh));
							else
								context.makemotion(Motions.MOTION_W_LEFT_SIDESTEP);
						} else {
							if (context.hasMotion(NAOJI_WALKER))
								context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
										.toRadians(0.4f * ballh));
							else
								context.makemotion(Motions.MOTION_W_RIGHT_SIDESTEP);
						}

					}

					tracking.setMode(BallTrackingTask.Mode.Cont);
					return;
				} else if (Math.abs(deg - 90) < 35 || Math.abs(deg + 90) < 35) {
					// ゴールはよこ

					if (Math.abs(ballh) < 15) {
						log.debug("inside shot deg:" + deg);
						context.makemotion(Motions.MOTION_STOP);
						context.getScheduler().abort();
						context.pushQueue("InsideKickTask");
					} else {

						if (ballh > 0) {
							if (context.hasMotion(NAOJI_WALKER))
								context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
										.toRadians(0.4f * ballh));
							else
								context.makemotion(Motions.MOTION_W_LEFT_SIDESTEP);
						} else {
							if (context.hasMotion(NAOJI_WALKER))
								context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
										.toRadians(0.4f * ballh));
							else
								context.makemotion(Motions.MOTION_W_RIGHT_SIDESTEP);
						}

					}

					return;
				} else {
					if (ballh > 0) {
						if (context.hasMotion(NAOJI_WALKER))
							context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
									.toRadians(0.4f * ballh));
						else
							context.makemotion(MOTION_LEFT_YY_TURN);
					} else {
						if (context.hasMotion(NAOJI_WALKER))
							context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
									.toRadians(0.4f * ballh));
						else
							context.makemotion(MOTION_RIGHT_YY_TURN);
					}

					return;

				}
			} else if (balld < 200) {

				log.debug("go back");
				context.makemotion(Motions.MOTION_W_BACKWARD);
				tracking.setMode(BallTrackingTask.Mode.Cont);
				return;

			} else {
				if (ballh > 0) {
					if (context.hasMotion(NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
								.toRadians(0.4f * ballh));
					else
						context.makemotion(MOTION_LEFT_YY_TURN);
				} else {
					if (context.hasMotion(NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
								.toRadians(0.4f * ballh));
					else
						context.makemotion(MOTION_RIGHT_YY_TURN);
				}

				return;
			}
		}

		if (balld < 500) {
			if (Math.abs(ballh) < 40) {
				// 大体ボールの方向を向いてる
				if (Math.abs(deg) < 50) {
					if (context.hasMotion(NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER, balld * 0.35f / 1e3f,
								0, 0);
					else
						context.makemotion(Motions.BASIC_WALK);
					tracking.setMode(BallTrackingTask.Mode.Localize);
					return;

				} else {
					if (deg < 0) {
						// ひだり
						if (context.hasMotion(NAOJI_WALKER))
							context.makemotion(NAOJI_WALKER, 0,
									balld * 0.5f / 1e3f, 0);
						else
							context.makemotion(MOTION_CIRCLE_LEFT);
					} else {
						// みぎ
						if (context.hasMotion(NAOJI_WALKER))
							context.makemotion(NAOJI_WALKER, 0,
									-balld * 0.5f / 1e3f, 0);
						else
							context.makemotion(MOTION_CIRCLE_RIGHT);
					}
					tracking.setMode(BallTrackingTask.Mode.Localize);
					return;
				}
			}
		}

		// 距離がある。
		// 近づく動作
		// 方向を向いてなければ向く
		if (Math.abs(ballh) < 25) {
			if (context.hasMotion(NAOJI_WALKER))
				context.makemotion(NAOJI_WALKER, balld * 0.5f / 1e3f, 0, 0);
			else
				context.makemotion(Motions.BASIC_WALK);
			tracking.setMode(BallTrackingTask.Mode.Localize);
			return;
		} else {
			if (ballh > 0) {
				if (context.hasMotion(NAOJI_WALKER))
					context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
							.toRadians(0.85f * ballh));
				else
					context.makemotion(MOTION_LEFT_YY_TURN);
			} else {
				if (context.hasMotion(NAOJI_WALKER))
					context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
							.toRadians(0.85f * ballh));
				else
					context.makemotion(MOTION_RIGHT_YY_TURN);
			}
			tracking.setMode(BallTrackingTask.Mode.Localize);
			return;
		}

	}

	public void leave(StrategyContext context) {
		super.leave(context);
	}
}
