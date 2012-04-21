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

public class BackAreaTask extends Task {
	private static final Logger log = Logger.getLogger(BackAreaTask.class);
	private BallTrackingTask tracking;
	private int preBalld;
	private float preBallh;
	private int count;
	private int Goaldist;
	private int BackAreaFlg;

	@Override
	public void continueTask(StrategyContext context) {
		// 自陣に戻る処理を書く
		context.getScheduler().setTTL(1300);
		WorldObject OwnGoal = context.getOwnGoal();
		int ogd;
		float ogh;

		WorldObject TargetGoal = context.getTargetGoal();
		int tgd;
		float tgh;

		tgd = TargetGoal.getDistance();
		tgh = TargetGoal.getHeading();
		ogd = OwnGoal.getDistance();
		ogh = OwnGoal.getHeading();
		if (context.getBall().getConfidence() > 0
				&& context.getBall().getDistance() < 800) {
			// ボールが見えたら終わり
			context.pushQueue("DefenceTask");
			context.getScheduler().abort();
			return;
		}
		// 自ゴールの方を向く : 0
		// 自ゴールに近づく : 1
		// 自ゴールに近づいたらターゲットゴールを向く : 2

		if (BackAreaFlg == 0) { // 自ゴールを探す
			tracking.setMode(Mode.OwnGoal);
			log.info("ownconf = " + context.getOwnGoal().getConfidence()
					+ "  ownhead = " + ogh);
			if (context.getOwnGoal().getConfidence() > 0) {

				// 自ゴールを探して自ゴールを向く処理を書く
				if (Math.abs(context.getOwnGoal().getHeading()) > 15) { // 正面に自ゴールがない
					// GetHeadingを使って右に回るか左に回るか
					if (ogh > 0) {
						if (context.hasMotion(Motions.NAOJI_WALKER))
							context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
									.toRadians(0.4f * ogh));
						else
							context.makemotion(Motions.MOTION_LEFT_YY_TURN);
					} else {
						if (context.hasMotion(Motions.NAOJI_WALKER))
							context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
									.toRadians(0.4f * ogh));
						else
							context.makemotion(Motions.MOTION_RIGHT_YY_TURN);
					}
				} else { // 自ゴールを向いたら次へ
					BackAreaFlg = 1;
				}
			} else {
				log.info("TURN");
				if (context.hasMotion(Motions.NAOJI_WALKER))
					context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
							.toRadians(60f));
				else {
					// log.info("else");
					context.makemotion(Motions.MOTION_LEFT_YY_TURN);
				}
			}
		}

		if (BackAreaFlg == 1) { // 自ゴールに近づく
			tracking.setMode(Mode.Cont);

			log.info("ownConf = " + context.getOwnGoal().getConfidence()
					+ "  ownDist = " + ogd + "  ownHead = " + ogh);
			if (context.getOwnGoal().getConfidence() > 0
					&& Math.abs(context.getOwnGoal().getHeading()) < 15) {
				// 前進する
				if (context.hasMotion(Motions.NAOJI_WALKER))
					context.makemotion(NAOJI_WALKER, context.getBall()
							.getDistance() * 0.35f / 1e3f, 0, 0);
				else
					context.makemotion(Motions.MOTION_YY_FORWARD_STEP);
			} else {
				// ゴールの方を向いてなかったら方向調整からやり直す
				log.info("BackAreaFlg = 0");
				BackAreaFlg = 0;
			}

			if (context.getOwnGoal().getConfidence() > 0
					&& Math.abs(context.getOwnGoal().getHeading()) < 15
					&& context.getOwnGoal().getDistance() < 1500) { // 自ゴールに近づいたら次へ
				log.info("BackAreaFlg = 2");
				BackAreaFlg = 2;
			}
		}

		if (BackAreaFlg == 2) { // ターゲットゴールを向く
			tracking.setMode(Mode.TargetGoal);
			// ターゲットゴールを向く処理を書く
			log.info("TargetGoalHead:" + context.getTargetGoal().getHeading());
			if (Math.abs(context.getTargetGoal().getHeading()) > 10) {
				if (tgh > 0) {
					if (context.hasMotion(Motions.NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
								.toRadians(0.4f * tgh));
					else
						context.makemotion(Motions.MOTION_LEFT_YY_TURN);
				} else {
					if (context.hasMotion(Motions.NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
								.toRadians(0.4f * tgh));
					else
						context.makemotion(Motions.MOTION_RIGHT_YY_TURN);
				}
			} else { // 敵ゴールを向いたら終わり
				BackAreaFlg = 0;
				context.pushQueue("DefenceTask");
				context.getScheduler().abort();
			}
		}
	}

	@Override
	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(1300);
	}

	@Override
	public void init(RobotContext context) {
		tracking = (BallTrackingTask) context.getStrategy().getTaskManager()
				.find("BallTracking");
		count = 0;
		BackAreaFlg = 0;
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
		return "BackAreaTask";
	}

}
