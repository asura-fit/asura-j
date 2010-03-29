package jp.ac.fit.asura.nao.strategy.tactics;

import org.apache.log4j.Logger;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask.Mode;

public class GetBallTask extends Task {
	private Logger log = Logger.getLogger(GetBallTask.class);
	private BallTrackingTask tracking;

	private int preBalld;
	private float preBallh;

	@Override
	public String getName() {
		return "GetBallTask";
	}

	public void init(RobotContext context) {
		tracking = (BallTrackingTask) context.getStrategy().getTaskManager()
				.find("BallTracking");
		assert tracking != null;
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(400);

		preBalld = 999;
	}

	public void continueTask(StrategyContext context) {
		WorldObject ball = context.getBall();
		int balld;
		float ballh;

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

		if (balld < 500) {
			tracking.setMode(Mode.Cont);
			if (Math.abs(ballh) > 25) {
				if (ballh > 0) {
					context.makemotion(Motions.MOTION_LEFT_YY_TURN);
				} else {
					context.makemotion(Motions.MOTION_RIGHT_YY_TURN);
				}
			} else if (balld > 210) {
				context.makemotion(Motions.MOTION_YY_FORWARD_STEP);
			} else {
				log.info("Getting ball finished.");
				context.pushQueue("TurnTask");
				context.getScheduler().abort();
			}

			return;

		} else {
			tracking.setMode(Mode.Localize);
			if (Math.abs(ballh) > 30) {
				if (ballh > 0) {
					context.makemotion(Motions.MOTION_LEFT_YY_TURN);
				} else {
					context.makemotion(Motions.MOTION_RIGHT_YY_TURN);
				}
			} else {
				context.makemotion(Motions.MOTION_YY_FORWARD_STEP);
			}

			return;
		}
	}

	@Override
	public void leave(StrategyContext context) {
	}

}
