package jp.ac.fit.asura.nao.strategy.tactics;

import org.apache.log4j.Logger;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask.Mode;

/**
 * PK戦専用のゴーリー
 * ベース：既存のゴーリー
 * 既存のゴーリーとの違いは
 *● フレーム数で獲得していた「前のボールの位置」を定数に変更（PK戦ではボールの初期位置が決まっているため）
 *　他にもいろいろ追加する予定
 *
 * @author aqua
 */




public class GoaliePkTask extends Task {

	private static final Logger log = Logger.getLogger(GoaliePkTask.class);
	private double balld;
	private float ballh;
	private BallTrackingTask tracking;
	private int switchFlag;

	@Override
	public String getName() {
		// TODO 自動生成されたメソッド・スタブ
		return "GoaliePkTask";
	}

	@Override
	public void init(RobotContext context) {
		tracking = (BallTrackingTask) context.getStrategy().getTaskManager()
				.find("BallTracking");

		switchFlag = 0;

	}

	public void after(StrategyContext context) {


	}

	@Override
	public void continueTask(StrategyContext context) {
		// TODO 自動生成されたメソッド・スタブ

		tracking.setMode(Mode.Cont);
		WorldObject ball = context.getBall();
		balld = ball.getDistance();
		ballh = ball.getHeading();

			// 状態：キックモーションのフラグ
			if (context.getGoalieKickFlag() == false && balld <= 300) {
				context.setGoalieDefenceFlag(false);
				context.setGoalieKickFlag(true);
				// log.info("------------------");
				// log.info("GoalieKickFlag = 1");
				// log.info("------------------");

			}else{

			// 状態:ディフェンスモーションのフラグ
				context.setGoalieDefenceFlag(true);
				context.setGoalieKickFlag(false);

				// log.info("------------------");
				// log.info("GoalieDefenceFlag=1");
				// log.info("------------------");
			}

			/**
			 * 状態の切り替え 1 ディフェンスモーション 2 キックモーション
			 */
			if (context.getGoalieDefenceFlag() == true
					&& context.getGoalieKickFlag() == false) {
				switchFlag = 1;
			} else if (context.getGoalieDefenceFlag() == false
					&& context.getGoalieKickFlag() == true) {
				switchFlag = 2;
			}

			log.info("switchFlag=" + switchFlag);

			switch (switchFlag) {

			case 1:
				// ディフェンスモーションを出すときのパターン
				//ここの値は後で調整をかける
				if (1500 - balld >= 10) {
					if (ballh > 5) {
						// ballがゴーリーの左側に抜けるとき
							log.info("L^L");
							context.makemotion(Motions.MOTION_L_GORI_ITO);
					} else
					if (ballh <= -5) {
						// ballがゴーリーの右側に抜けるとき
							log.info("R^R");
							context.makemotion(Motions.MOTION_R_GORI_ITO);
					}

					/**
					 * 止めた後を想定してキックモーションのフラグを立て、ディフェンスモーションのフラグを折る。
					 */

					context.setGoalieDefenceFlag(false);
					log.info("GoalieDefenceFlag=0");
					context.setGoalieKickFlag(true);
					log.info("GoalieKickFlag=1");
					switchFlag = 0;
					log.info("switchFlag=0");

					// 前保存した値を初期化する
					balld = 0;
					ballh = 0;
					log.info("balld(zero1) =" + balld);
					log.info("ballh(zero1) =" + ballh);

				}
				break;

			case 2:
				// 近距離モーションを出すときのパターン
				if (ballh > 0) {
					// 左側
					if (ballh <= 40) {
						if (balld <= 180) {
							log.info("S^C^SiL");
							context.makemotion(Motions.MOTION_L_GOKIS_ITO);

							context.setGoalieKickFlag(false);
							log.info("GoalieKickFlag=0");
							context.setGoalieDefenceFlag(false);
							log.info("GoalieDefenceFlag=0");
							switchFlag = 0;
							log.info("switchFlag=0");
							// 前保存した値を初期化する
							balld = 0;
							ballh = 0;
							log.info("balld(zero) =" + balld);
							log.info("ballh(zero) =" + ballh);
						}
					} else if (balld <= 280) {
						log.info("S^C^TL");
						context.makemotion(Motions.MOTION_L_GOTE_ITO);

						context.setGoalieKickFlag(false);
						log.info("GoalieKickFlag=0");
						context.setGoalieDefenceFlag(false);
						log.info("GoalieDefenceFlag=0");
						switchFlag = 0;
						log.info("switchFlag=0");
						// 前保存した値を初期化する
						balld = 0;
						ballh = 0;
						log.info("balld(zero) =" + balld);
						log.info("ballh(zero) =" + ballh);
					} else if (balld <= 500) {

						// ボールへ近づくための処理

					}

				}// 右側
				else if (ballh <= 0) {
					if (ballh >= -50) {
						if (balld <= 180) {
							log.info("S^C^SiR");
							context.makemotion(Motions.MOTION_R_GOKIS_ITO);

							context.setGoalieKickFlag(false);
							log.info("GoalieKickFlag=0");
							context.setGoalieDefenceFlag(false);
							log.info("GoalieDefenceFlag=0");
							switchFlag = 0;
							log.info("switchFlag=0");
							// 前保存した値を初期化する
							balld = 0;
							ballh = 0;
							log.info("balld(zero) =" + balld);
							log.info("ballh(zero) =" + ballh);
						}
					} else if (balld <= 300) {
						log.info("S^C^TR");
						context.makemotion(Motions.MOTION_R_GOTE_ITO);

						context.setGoalieKickFlag(false);
						log.info("GoalieKickFlag=0");
						context.setGoalieDefenceFlag(false);
						log.info("GoalieDefenceFlag=0");
						switchFlag = 0;
						log.info("switchFlag=0");
						// 前保存した値を初期化する
						balld = 0;
						ballh = 0;
						log.info("balld(zero) =" + balld);
						log.info("ballh(zero) =" + ballh);
					} else if (balld <= 500) {

						// ボールへ近づくための処理

					}

				}
				break;

			default:

			}

			// ボールへ近づいた時に元の場所に戻ろうとする処理

	}// continue end

	@Override
	public void leave(StrategyContext context) {
		// TODO 自動生成されたメソッド・スタブ
	}

	@Override
	public void enter(StrategyContext context) {
		// TODO 自動生成されたメソッド・スタブ
		context.getScheduler().setTTL(100);
	}

}