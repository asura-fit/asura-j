package jp.ac.fit.asura.nao.strategy.tactics;

import static jp.ac.fit.asura.nao.motion.Motions.NAOJI_WALKER;

import org.apache.log4j.Logger;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.motion.Motion;

import jp.ac.fit.asura.nao.naoji.motion.NaojiWalker;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask.Mode;

public class KickOff01Task extends Task {
	private Logger log = Logger.getLogger(KickOff01Task.class);
	private BallTrackingTask tracking;

	private int preBalld;
	private float preBallh;

	@Override
	public String getName() {
		return "KickOff01Task";
	}

	public void init(RobotContext context) {
		tracking = (BallTrackingTask) context.getStrategy().getTaskManager()
				.find("BallTracking");
		assert tracking != null;
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(3000);//時間をここで決める

		preBalld = 999;
	}

	public void continueTask(StrategyContext context) {
		WorldObject ball = context.getBall();
		int balld;
		float ballh;
		//if (context.getWalkFlag() == false){

		if (ball.getConfidence() == 0) {
			context.getScheduler().abort();
			return;
		}

		if (ball.getConfidence() > 0 && ball.getDistance() != 0) {
			balld = ball.getDistance();
			preBalld = balld;
		} else {
			balld = preBalld;
		}

		ballh = ball.getHeading();

		if (context.getFrame() % 3 == 0)
			log.trace("bc:" + ball.getConfidence() + " bd:" + balld + " bh:"
					+ ballh);

		if (balld < 800) {
			tracking.setMode(Mode.Cont);
			if (Math.abs(ballh) > 20f) {
				if (ballh > 0) {
					if (context.hasMotion(Motions.NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
								.toRadians(0.4f * ballh));
					else
						context.makemotion(Motions.MOTION_LEFT_YY_TURN);
				} else {
					if (context.hasMotion(Motions.NAOJI_WALKER))
						if (context.hasMotion(NAOJI_WALKER))
							context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
									.toRadians(0.4f * ballh));
						else
							context.makemotion(Motions.MOTION_RIGHT_YY_TURN);
				}
			} else if (balld > 160) {
				if (context.hasMotion(Motions.NAOJI_WALKER))
					context
							.makemotion(NAOJI_WALKER, balld * 0.35f / 1e3f, 0,
									0);
				else
					context.makemotion(Motions.MOTION_YY_FORWARD_STEP);
			} else {
				log.info("Getting ball finished.");
				//ここでキックを決める
				context.makemotion(Motions.MOTION_SHOT_INSIDE_RIGHT );
				context.getScheduler().abort();
			}

			return;

		} else {
			tracking.setMode(Mode.Localize);
			if (Math.abs(ballh) > 27) {
				if (ballh > 0) {
					if (context.hasMotion(Motions.NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
								.toRadians(0.4f * ballh));
					else
						context.makemotion(Motions.MOTION_LEFT_YY_TURN);
				} else {
					if (context.hasMotion(NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
								.toRadians(0.4f * ballh));
					else
						context.makemotion(Motions.MOTION_RIGHT_YY_TURN);
				}
			} else {
				if (context.hasMotion(Motions.NAOJI_WALKER))
					context.makemotion(NAOJI_WALKER, balld * 0.5f / 1e3f, 0, 0);
				else
					context.makemotion(Motions.MOTION_YY_FORWARD_STEP);
			}

			return;
		}
	}

	@Override
	public void leave(StrategyContext context) {
		context.setWalkFlag(true);
	}

}
