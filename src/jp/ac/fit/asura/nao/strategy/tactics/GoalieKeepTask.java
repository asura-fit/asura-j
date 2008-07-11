/**
 * 
 */
package jp.ac.fit.asura.nao.strategy.tactics;

import org.apache.log4j.Logger;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.actions.LookAroundTask;

/**
 * @author kilo
 *
 */
public class GoalieKeepTask extends Task {
	private Logger log = Logger.getLogger(getClass());
	
	private int step;
	private int approachBallIdx;
	
	public String getName() {
		return "GoalieKeepTask";
	}
	
	public void init(RobotContext context) {
	}
	
	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(2400);
		step = 0;
		approachBallIdx = 0;
		super.enter(context);
	}

	public void continueTask(StrategyContext context) {
		WorldObject ball = context.getBall();
		WorldObject self = context.getSelf();
		
		int balld = ball.getDistance();
		float ballh = ball.getHeading();
		
		if (balld > 1000) {
			// ボールが遠い
			if (Math.abs(ballh) > 30) {
				// ボールの方向を向いていない
				if (ballh < 0) {
					context.makemotion(Motions.MOTION_RIGHT_YY_TURN);
				} else {
					context.makemotion(Motions.MOTION_LEFT_YY_TURN);
				}
			} else {
				//　ボールの方向を向いている
				context.makemotion(Motions.MOTION_STOP);
			}
		} else {
			// ボールが近いとき
			// 流れ：キック->横移動->前進->キック->後退->ループ
			switch( approachBallIdx ){
				case 0:
				case 3:
					//まずはガード
					if (balld > 800) {
						context.makemotion(Motions.MOTION_STOP);
					} else if (ballh < 0) {
						context.makemotion(Motions.MOTION_SIDEKEEP_LEFT);
					} else {
						context.makemotion(Motions.MOTION_SIDEKEEP_RIGHT);
					}
					
					if(step % 120 == 0) approachBallIdx++;
					break;
				case 1:	
					//次に横移動で近づく
					//飛ばせそうならキック
					if (balld > 800) {
						context.makemotion(Motions.MOTION_STOP);
					} else if (Math.abs(ballh) < 10.0) {
						if(ballh < 0)
							context.makemotion(Motions.MOTION_KAKICK_LEFT);
						else
							context.makemotion(Motions.MOTION_KAKICK_RIGHT);
					} else if (ballh < 0) {
						context.makemotion(Motions.MOTION_W_RIGHT_SIDESTEP);
					} else {
						context.makemotion(Motions.MOTION_W_LEFT_SIDESTEP);
					}
					
					if(step % 150 == 0) approachBallIdx++;
					break;
				case 2:
					//若干前進
					context.makemotion(Motions.MOTION_YY_FORWARD);
					
					if(step % 140 == 0) approachBallIdx++;
					break;
				case 4:
					//若干後退
					if(balld > 650)
						context.makemotion(Motions.MOTION_W_BACKWARD);
					
					if(step % 120 == 0) approachBallIdx++;
					break;
				case 5:
				default:approachBallIdx = 0;
			}
		}
		
		step++;
		super.continueTask(context);
	}

}
