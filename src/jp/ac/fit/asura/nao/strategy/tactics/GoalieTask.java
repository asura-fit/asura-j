package jp.ac.fit.asura.nao.strategy.tactics;

/**
 * ボールの加速度を   ボールの加速度＝前のボール位置（balld0）- 後のボールの位置（balld）   という風にする。
 * もし加速度が300以上だったら対応した方向の腕を動かすモーションを使う。
 * headingで対応する腕を決める。
 * @author mizuta
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
		balld = 0;
		balld0 = 0;
		ballh = 0;
		ballh0 = 0;

	}

	public void after(StrategyContext context) {
		

		WorldObject ball = context.getBall();
		double balld = ball.getDistance();
		float ballh = ball.getHeading();
		


		// 前のボールの位置の値を保存する。
		if (context.getGoalieFlag() == false && context.getFrame() %20==0) {
			
			//前保存した値を初期化する
			

			balld0 = 0;
			ballh0 = 0;
			log.info("balld0(zero)=" + balld0);
			log.info("ballh0(zero)=" + ballh0);
			
			//前のボールの値を保存する
			balld0 = balld; 
			ballh0 = ballh;
			
			
			//値保存のためのフラグ
			context.setGoalieFlag(true);
			log.info("GoalieFlag = 1");
			log.info("balld(after) =" + balld);
			log.info("balld0(after)=" + balld0);
			log.info("ballh(after) =" + ballh);
			log.info("ballh0(after)=" + ballh0);

		}

	}
	
	/**
	 * ◯前のボールの距離と後のボールの距離を比較する。
	 * ◯腕を振る方向は、前保存した値と後で保存した値を比較する。 
	 * ◯正面の時はなるべくゴールを狙って蹴り返す処理
	 */

	@Override
	public void continueTask(StrategyContext context) {
		// TODO 自動生成されたメソッド・スタブ
		tracking.setMode(Mode.Cont);

		WorldObject ball = context.getBall();
		double balld = ball.getDistance();
		float ballh = ball.getHeading();

		if (context.getGoalieFlag() == true) {

				context.setGoalieFlag(false);
				log.info("GoalieFlag = 0");

				log.info("balld  =" + balld);
				log.info("balld0 =" + balld0);
				log.info("ballh  =" + ballh);
				log.info("ballh0 =" + ballh0);
				

				//ボールがこっちに向かっているかを判定
				if (balld0 - balld >= 10) {
					//ball0が左側にあるとき
					if(ballh0>5){
						//ballo0が左側からゴーリーの左側に抜けるとき
						if(ballh0 < ballh){
							log.info("L→L");
							context.makemotion(Motions.MOTION_L_GORI_ITO);
						
						}else 
						//ball0が左側からゴーリーの右側に抜けるとき	
						if(ballh0 > ballh){
							log.info("L→R");
							context.makemotion(Motions.MOTION_R_GORI_ITO);
							
						}
					}else
					//ball0が右側にあるとき
					if(ballh0<-5){
						//ball0が右側からゴーリーの左側に抜けるとき
						if(ballh0 > ballh){
							log.info("R→L");
							context.makemotion(Motions.MOTION_L_GORI_ITO);

							
						}else
						//ball0が右側からゴーリーの右側に抜けるとき
						if(ballh0 < ballh){
							log.info("R→R");
							context.makemotion(Motions.MOTION_R_GORI_ITO);

						}
					}
				}
		}
	}//continue end
				

	


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
