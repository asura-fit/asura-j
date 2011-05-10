package jp.ac.fit.asura.nao.strategy.tactics;

import org.apache.log4j.Logger;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask.Mode;
import static jp.ac.fit.asura.nao.motion.Motions.NAOJI_WALKER;

public class GoalieKeepTask extends Task {
	private BallTrackingTask tracking;
	private Logger log = Logger.getLogger(GoalieKeepTask.class);

	private StrategyContext context;

	private boolean flag;
	private boolean ballDistFlag;
	private int count;

	@Override
	public String getName() {
		return "GoalieKeepTask";
	}

	@Override
	public void init(RobotContext context) {
		tracking = (BallTrackingTask) context.getStrategy().getTaskManager()
				.find("BallTracking");

		assert tracking != null;
	}

	@Override
	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(400);
		this.context = context;
	}

	@Override
	public void continueTask(StrategyContext context) {
		WorldObject self = context.getSelf();
		WorldObject target = context.getTargetGoal();
		WorldObject own = context.getOwnGoal();
		WorldObject ball = context.getBall();

		if (count < 30) {
			tracking.setMode(Mode.TargetGoal);
			count++;
			return;
		}

		if ((target.getHeading() < -15 || target.getHeading() > 35)
				|| target.getDistance() < 4900) {
			backToGoal();
		} else {
			// ちゃんとOwnGoalに居る
			tracking.setMode(Mode.Cont);
			if (ball.getConfidence() > 0) {
				// ボールが見えてる
				if (Math.abs(ball.getHeading()) > 15) {
					if (ball.getHeading() > 0) {
						if (context.hasMotion(NAOJI_WALKER))
							context.makemotion(NAOJI_WALKER, 0, 0.2f, 0);
						else
							context.makemotion(Motions.MOTION_W_LEFT_SIDESTEP);
					} else {
						if (context.hasMotion(NAOJI_WALKER))
							context.makemotion(NAOJI_WALKER, 0, -0.2f, 0);
						else
							context.makemotion(Motions.MOTION_W_RIGHT_SIDESTEP);
					}
				} else if (ball.getDistance() < 500) {
					if (ball.getDistance() > 230) {
						if (context.hasMotion(NAOJI_WALKER))
							context.makemotion(NAOJI_WALKER,
									ball.getDistance() * 0.4f / 1e3f, 0, 0);
						else
							context.makemotion(Motions.MOTION_YY_FORWARD_STEP);
					} else {
						context.makemotion(Motions.MOTION_L_GORI_GUARD);
						count = 0;
					}
				}
				return;
			} else {
				// ボールが見えてない
				return;
			}
		}
		// if ((Math.abs(self.getYaw()) > 35)
		// || target.getDistance() < 4900) {
		// backToGoal();
		// } else {
		// // ちゃんとOwnGoalに居る
		// tracking.setMode(Mode.Cont);
		// if (ball.getConfidence() > 0) {
		// // ボールが見えてる
		// if (Math.abs(ball.getHeading()) > 15) {
		// if (ball.getHeading() > 0) {
		// if (context.hasMotion(NAOJI_WALKER))
		// context.makemotion(NAOJI_WALKER, 0, 0.2f, 0);
		// else
		// context.makemotion(Motions.MOTION_W_LEFT_SIDESTEP);
		// } else {
		// if (context.hasMotion(NAOJI_WALKER))
		// context.makemotion(NAOJI_WALKER, 0, -0.2f, 0);
		// else
		// context.makemotion(Motions.MOTION_W_RIGHT_SIDESTEP);
		// }
		// } else if (ball.getDistance() < 500) {
		// if (ball.getDistance() > 230) {
		// if (context.hasMotion(NAOJI_WALKER))
		// context.makemotion(NAOJI_WALKER,
		// ball.getDistance() * 0.4f / 1e3f, 0, 0);
		// else
		// context.makemotion(Motions.MOTION_YY_FORWARD_STEP);
		// } else {
		// context.makemotion(Motions.MOTION_L_GORI_GUARD);
		// count = 0;
		// }
		// }
		// return;
		// } else {
		// // ボールが見えてない
		// return;
		// }
		// }

	}

	@Override
	public void after(StrategyContext context) {

	}

	@Override
	public void leave(StrategyContext context) {
	}

	/**
	 * OwnGoalまで戻る.
	 *
	 */
	private void backToGoal() {
		tracking.setMode(Mode.Goal);
		WorldObject own = context.getOwnGoal();
		WorldObject target = context.getTargetGoal();
		WorldObject self = context.getSelf();
		float selfYaw = self.getYaw();
		float ownh = own.getHeading();
		int ownd = own.getDistance();

		if (!flag) {
			// OwnGoalの近くまで行く
			if (own.getConfidence() > 0) {
				if (Math.abs(ownh) > 20) {
					// ゴールの方を向いていない
					if (ownh > 0) {
						if (context.hasMotion(NAOJI_WALKER))
							context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
									.toRadians(0.4f * ownh));
						else
							context.makemotion(Motions.MOTION_LEFT_YY_TURN);
					} else {
						if (context.hasMotion(NAOJI_WALKER))
							context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
									.toRadians(0.4f * ownh));
						else
							context.makemotion(Motions.MOTION_RIGHT_YY_TURN);
					}
				} else if (ownd > 300) {
					// ゴールから遠い
					if (context.hasMotion(NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER, ownd * 0.5f / 1e3f, 0,
								0);
					else
						context.makemotion(Motions.MOTION_YY_FORWARD_STEP);
				} else {

					flag = true;
				}

			} else {
				// OwnGoalが見えていないときは、とりあえずその場でターンする
				if (context.hasMotion(NAOJI_WALKER))
					context.makemotion(NAOJI_WALKER, 0, 0, 0.5f);
				else
					context.makemotion(Motions.MOTION_LEFT_YY_TURN);
			}
		} else {
			// selfのyaw姿勢が0に近づくまでターン
			// if (Math.abs(selfYaw) > 35) {
			if (target.getHeading() > -15 && target.getHeading() < 35) {
				log.info("targeth: " + target.getHeading());
				// TargetGoalの方を向いていない
				if (context.hasMotion(NAOJI_WALKER))
					context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
					// .toRadians(Math.abs(selfYaw) * 0.4f));
							.toRadians(target.getHeading() * 0.4f));
				else
					context.makemotion(Motions.MOTION_LEFT_YY_TURN);
			} else {
				// ゴールまで戻り終わった
				flag = false;
			}
		}

	}
}
