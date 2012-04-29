package jp.ac.fit.asura.nao.strategy.tactics;

/**
 * ボールの加速度を   ボールの加速度＝前のボール位置（balld0）- 後のボールの位置（balld）   という風にする。
 * もし加速度が20以上だったら対応した方向の腕を動かすモーションを使う。
 * headingで対応する腕を決める。
 *
 * @author aqua
 */

import org.apache.log4j.Logger;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask.Mode;

public class GoalieTask extends Task {

	private static final Logger log = Logger.getLogger(GoalieTask.class);
	private double balld;
	private double balld0;
	private float ballh;
	private float ballh0;
	private int ballc;
	private BallTrackingTask tracking;

	@Override
	public String getName() {
		// TODO 自動生成されたメソッド・スタブ
		return "GoalieTask";
	}

	@Override
	public void init(RobotContext context) {
		tracking = (BallTrackingTask) context.getStrategy().getTaskManager()
				.find("BallTracking");

	}

	public void after(StrategyContext context) {

//		log.debug("balld(after) =" + balld);
//		log.debug("balld0(after)=" + balld0);
//		log.debug("ballh(after) =" + ballh);
//		log.debug("ballh0(after)=" + ballh0);
//		log.info("after");

		if (ballc != 0) {
			if (context.getGoalieFlag() == false && context.getFrame() % 5 == 0) {

				// 前のボールの値を保存する
				balld0 = balld;
				ballh0 = ballh;

//				log.debug("balld(after2)=" + balld);
//				log.debug("balld0(after2)=" + balld0);
//				log.debug("ballh(after2)=" + ballh);
//				log.debug("ballh0(after2)=" + ballh0);

				// 値保存のためのフラグ
				context.setGoalieFlag(true);
//				log.debug("GoalieFlag = 1");

			}
			// 連続でキックさせないためのフラグ
			if (context.getGoalieKickFlag() == false
					&& context.getFrame() % 50 == 0) {
				context.setGoalieKickFlag(true);
				log.debug("------------------");
				log.debug("GoalieKickFlag = 1");
				log.debug("------------------");
			}

		} else {
//			balld = 0;
//			ballh = 0;
//			balld0 = 0;
//			ballh0 = 0;

		}
	}

	/**
	 * 前のボールの距離と後のボールの距離を比較する。 腕を振る方向は、前保存した値と後で保存した値を比較する。 正面の時は蹴り返す処理。
	 *
	 */

	@Override
	public void continueTask(StrategyContext context) {
		// TODO 自動生成されたメソッド・スタブ

		log.info("continue");
		tracking.setMode(Mode.Cont);
		WorldObject ball = context.getBall();
		balld = ball.getDistance();
		ballh = ball.getHeading();
		ballc = ball.getConfidence();

		if (context.getGoalieFlag() == true) {

			context.setGoalieFlag(false);
			log.info("GoalieFlag = 0");

			log.info("balld  =" + balld);
			log.info("balld0 =" + balld0);
			log.info("ballh  =" + ballh);
			log.info("ballh0 =" + ballh0);

			if (balld < 820) {
				// ballがこっちに向かっているかを判定
				if (balld0 - balld >= 10) {
					// ballが左側にあるとき
					if (ballh0 > 10) {
						// ballが左側からゴーリーの左側に抜けるとき
						if (ballh0 < ballh) {
							log.info("L^L");
							context.makemotion(Motions.MOTION_L_GORI_ITO);
						} else
						// ballが左側からゴーリーの右側に抜けるとき
						if (ballh0 > ballh) {
							log.info("L^R");
							context.makemotion(Motions.MOTION_R_GORI_ITO);
						}
					} else
					// ballが右側にあるとき
					if (ballh0 < -10) {
						// ballが右側からゴーリーの左側に抜けるとき
						if (ballh0 < ballh) {
							log.info("R^L");
							context.makemotion(Motions.MOTION_L_GORI_ITO);

						} else
						// ballが右側からゴーリーの右側に抜けるとき
						if (ballh0 > ballh) {
							log.info("R^R");
							context.makemotion(Motions.MOTION_R_GORI_ITO);

						}
					} else
					// ballが正面にあるとき
					if (balld < 230 && context.getGoalieKickFlag() == true) {
						// Kickの使い分け
						if (ballh > 0) {
							if (ballh < 35) {
								log.info("C^SL");
								context.makemotion(Motions.MOTION_L_GOKI_ITO);
							} else if (ballh >= 35 && ballh < 50) {
								log.info("C^CL");
								context.makemotion(Motions.MOTION_L_GOKIC_ITO);
							}
						} else if (ballh < 0) {
							if (ballh > -35) {
								log.info("C^SR");
								context.makemotion(Motions.MOTION_R_GOKI_ITO);
							} else if (ballh <= -35 && ballh > -50) {
								log.info("C^CR");
								context.makemotion(Motions.MOTION_R_GOKIC_ITO);
							}
						}

						context.setGoalieKickFlag(false);
						log.info("GoalieKickFlag=0");
					}
				} else

				// ballが止まってる時。
				if (balld < 230 && context.getGoalieKickFlag() == true) {
					// Kickの使い分け
					if (ballh > 0) {
						if (ballh < 35) {
							log.info("S^C^SL");
							context.makemotion(Motions.MOTION_L_GOKI_ITO);
						} else if (ballh >= 35 && ballh < 50) {
							log.info("S^C^CL");
							context.makemotion(Motions.MOTION_L_GOKIC_ITO);
						} else if (ballh >= 50 && ballh < 70) {
							log.info("L---------------------");
						}
					} else if (ballh < 0) {
						if (ballh > -35) {
							log.info("S^C^SR");
							context.makemotion(Motions.MOTION_R_GOKI_ITO);
						} else if (ballh <= -35 && ballh > -50) {
							log.info("S^C^CR");
							context.makemotion(Motions.MOTION_R_GOKIC_ITO);
						} else if (ballh <= -50 && ballh > -70) {
							log.info("R------------------------");
						}
					}

					context.setGoalieKickFlag(false);
					log.info("GoalieKickFlag=0");
				}
			}
			// // 前保存した値を初期化する
			// balld = 0;
			// ballh = 0;
			// balld0 = 0;
			// ballh0 = 0;
			// log.info("balld(zero) =" + balld);
			// log.info("ballh(zero) =" + ballh);
			// log.info("balld0(zero)=" + balld0);
			// log.info("ballh0(zero)=" + ballh0);
		}
	}// continue end

	@Override
	public void leave(StrategyContext context) {
		// TODO 自動生成されたメソッド・スタブ
	}

	@Override
	public void enter(StrategyContext context) {
		// TODO 自動生成されたメソッド・スタブ
		context.getScheduler().setTTL(100);
		context.setGoalieKickFlag(true);
	}

}
