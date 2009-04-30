/*
 * 作成日: 2008/06/13
 */
package jp.ac.fit.asura.nao.strategy.tactics;

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
	private Logger log = Logger.getLogger(getClass());

	private BallTrackingTask tracking;

	// private ShootTask shootTask;

	private int stateChanged;
	private int prevState;

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
		stateChanged++;
		if (context.getGameState().getState() != prevState) {
			stateChanged = 0;
			prevState = context.getGameState().getState();
		}
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(400);

		ballLastSeenFrame = 0;
		prevBalld = 0;
		prevBallh = 0;
	}

	public void continueTask(StrategyContext context) {

		context.getScheduler().setTTL(400);

		WorldObject self = context.getSelf();
		WorldObject ball = context.getBall();

		float ballh;
		int balld;
		int goalx = 0;
		int goaly = 2700 + Goal.Depth;

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
				.atan2(goalx - self.getX(), goaly - self.getY()))
				- self.getYaw());

		if (context.getFrame() % 3 == 0)
			log.trace("bc:" + ball.getConfidence() + " bd:" + balld + " bh:"
					+ ballh + " deg:" + deg);

		if (balld < 150 && Math.abs(ballh) < 20) {
			// ボールにかなり近い
			if (context.hasMotion(NAOJI_WALKER))
				context.makemotion(NAOJI_WALKER, balld * 0.75f / 1e3f, 0, 0);
			else
				context.makemotion(Motions.MOTION_YY_FORWARD_STEP);
			tracking.setMode(BallTrackingTask.Mode.Cont);
			return;
		}

		if (balld < 500 && Math.abs(ballh) < 30) {
			// 距離が500以内である
			// 大体ボールの方向を向いてる
			if (Math.abs(deg) < 90) {

				if (context.hasMotion(NAOJI_WALKER))
					context
							.makemotion(NAOJI_WALKER, balld * 0.75f / 1e3f, 0,
									0);
				else
					context.makemotion(Motions.MOTION_YY_FORWARD_STEP);
				tracking.setMode(BallTrackingTask.Mode.Cont);
				return;
			} else {
				if (deg < 0) {
					// ひだりまわり
					if (context.hasMotion(NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER, 0,
								balld * 0.95f / 1e3f, 0);
					else
						context.makemotion(Motions.MOTION_CIRCLE_LEFT);
				} else {
					// みぎ
					if (context.hasMotion(NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER, 0,
								-balld * 0.95f / 1e3f, 0);
					else
						context.makemotion(Motions.MOTION_CIRCLE_RIGHT);
				}
				tracking.setMode(BallTrackingTask.Mode.Localize);
				return;
			}

		}

		// 距離が255以上ある。
		// 近づく動作
		// 方向を向いてなければ向く
		if (Math.abs(ballh) < 25) {
			if (context.hasMotion(NAOJI_WALKER))
				context.makemotion(NAOJI_WALKER, balld * 0.45f / 1e3f, 0, 0);
			else
				context.makemotion(Motions.MOTION_YY_FORWARD_STEP);
			tracking.setMode(BallTrackingTask.Mode.Localize);
			return;
		} else {
			if (ballh > 0) {
				if (context.hasMotion(NAOJI_WALKER))
					context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
							.toRadians(0.45f * ballh));
				else
					context.makemotion(Motions.MOTION_LEFT_YY_TURN);
			} else {
				if (context.hasMotion(NAOJI_WALKER))
					context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
							.toRadians(0.45f * ballh));
				else
					context.makemotion(Motions.MOTION_RIGHT_YY_TURN);
			}
			tracking.setMode(BallTrackingTask.Mode.Localize);
			return;
		}

	}

	public void leave(StrategyContext context) {
		super.leave(context);
	}
}
