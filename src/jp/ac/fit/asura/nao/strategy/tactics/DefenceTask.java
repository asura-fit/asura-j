package jp.ac.fit.asura.nao.strategy.tactics;

import static jp.ac.fit.asura.nao.motion.Motions.NAOJI_WALKER;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask.Mode;

import org.apache.log4j.Logger;

public class DefenceTask extends Task {
	private static final Logger log = Logger.getLogger(DefenceTask.class);
	private BallTrackingTask tracking;
	private int preBalld;
	private float preBallh;
	private int count;
	private int Goaldist;

	@Override
	public void continueTask(StrategyContext context) {
		WorldObject ball = context.getBall();
		int balld;
		float ballh;
		// if (ball.getConfidence() == 0) {
		// log.info("!!!:" + count);
		// tracking.setMode(Mode.Cont);
		// context.getScheduler().abort();
		// return;
		// }

		context.getScheduler().setTTL(900);

		if (ball.getConfidence() > 0 && ball.getDistance() != 0) {
			balld = ball.getDistance();
			preBalld = balld;
		} else {
			balld = preBalld;
		}
		ballh = ball.getHeading();

		if (context.getFrame() % 10 == 0)
			log.trace("bc:" + ball.getConfidence() + "bd:" + balld + "bh:"
					+ ballh);

		// ここが分かれ目ってやつなのよね多分。
		WorldObject TargetGoal = context.getTargetGoal();
		int tgd;
		float tgh;

		tgd = TargetGoal.getDistance();
		tgh = TargetGoal.getHeading();

		if (count < 20) {
			count++;
			tracking.setMode(Mode.TargetGoal);
		} else if (count < 80) {
			count++;
			tracking.setMode(Mode.Cont);
		} else {
			count = 0;
			log.info("ballc" + ball.getConfidence() + "   balld:" + balld
					+ "   ballh:" + ballh + "   goald:" + tgd);
		}

		// if (count < 300) {
		if (ball.getConfidence() == 0) {
			tracking.setMode(Mode.Goal);
			// tracking.setMode(Mode.Alt);
			Goaldist = 4000;
			if (count % 20 == 0)
				log.info("Lost Ball");
			if (tgd < Goaldist || context.getOwnGoal().getDistance() > 2000) {
				if (count % 20 == 0)
					log.info("GoalDist < 2500");

				context.pushQueue("BackAreaTask");
				context.getScheduler().abort();
			} else {
				if (count % 20 == 0) {
					log.info("BallSearch");
				}
				tracking.setMode(Mode.Alt);
				// if (context.getTargetGoal().getDistance() < 4000
				// || context.getOwnGoal().getDistance() > 2000) {
				// context.pushQueue("BackAreaTask");
				// context.getScheduler().abort();
				// } else {
				if (context.hasMotion(Motions.NAOJI_WALKER)) {
					context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
							.toRadians(0.4f * ballh));
				} else {
					context.makemotion(Motions.MOTION_RIGHT_YY_TURN);
				}
				// }
				// ボールを捜す処理を書く
				// 保留↓
				// if (context.hasMotion(Motions.NAOJI_WALKER)) {
				// context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
				// .toRadians(0.4f * ballh));
				// } else {
				// context.makemotion(Motions.MOTION_LEFT_YY_TURN);
				// }
			}
		} else {
			if (balld < 800) {
				if (count % 20 == 0)
					log.info("Near Ball");// ボールに近い
				tracking.setMode(Mode.Cont);
				// if (Math.abs(ballh) > 22) {
				// if (ballh > 0) {
				// if (context.hasMotion(Motions.NAOJI_WALKER))
				// context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
				// .toRadians(0.4f * ballh));
				// else
				// context.makemotion(Motions.MOTION_LEFT_YY_TURN);
				// } else {
				// if (context.hasMotion(Motions.NAOJI_WALKER))
				// context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
				// .toRadians(0.4f * ballh));
				// else
				// context.makemotion(Motions.MOTION_RIGHT_YY_TURN);
				// }
				//
				// } else if (balld > 210) {
				// if (context.hasMotion(Motions.NAOJI_WALKER))
				// context.makemotion(NAOJI_WALKER, balld * 0.35f / 1e3f,
				// 0, 0);
				// else
				// context.makemotion(Motions.MOTION_YY_FORWARD_STEP);
				// } else {
				// log.debug("front shot dist:" + balld);
				log.info("Getting ball finishued.   "
						+ context.getBall().getHeading());
				context.pushQueue("GetBallTask");
				// context.pushQueue("BackAreaTask");
				context.getScheduler().abort();
				// context.getScheduler().abort();
				// }
				// return;
			} else {
				if (count % 20 == 0)
					log.info("Far Ball");
				if (tgd < Goaldist || context.getOwnGoal().getDistance() > 2500) {
					if (count % 20 == 0)
						log.info("GoalDist < 2500");

					// tracking.setMode(Mode.TargetGoal);
					context.pushQueue("BackAreaTask");
					context.getScheduler().abort();
					// ↓保留
					// if (context.hasMotion(Motions.NAOJI_WALKER))
					// // 後ろに下がる
					// context.makemotion(NAOJI_WALKER, -(tgd * 0.1f), 0, 0);
					// else
					// context.makemotion(Motions.MOTION_W_BACKWARD);
				} else {
					if (count % 20 == 0)
						log.info("GoalDist > 2500 : GetBallTask!");

					// context.pushQueue("DefenderTurnTask");
					// context.getScheduler().abort();

					// context.pushQueue("BackAreaTask");
					context.pushQueue("GetBallTask");
					context.getScheduler().abort();
				}

			}
		}
	}

	@Override
	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(900);
	}

	@Override
	public void init(RobotContext context) {
		tracking = (BallTrackingTask) context.getStrategy().getTaskManager()
				.find("BallTracking");
		count = 0;
		Goaldist = 1000;
	}

	@Override
	public void leave(StrategyContext context) {
		// TODO 自動生成されたメソッド・スタブ
		super.leave(context);
	}

	@Override
	public String getName() {
		// TODO 自動生成されたメソッド・スタブ
		return "DefenceTask";
	}

}
