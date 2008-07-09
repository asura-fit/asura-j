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
	
	public String getName() {
		return "GoalieKeepTask";
	}
	
	public void init(RobotContext context) {
	}
	
	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(400);
		step = 0;
		super.enter(context);
	}

	public void continueTask(StrategyContext context) {
		WorldObject ball = context.getBall();
		WorldObject self = context.getSelf();
		
		int balld = ball.getDistance();
		float ballh = ball.getHeading();
		
		if (balld > 800) {
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
			// ボールが近い
			if (balld > 600) {
				context.makemotion(Motions.MOTION_STOP);
			} else if (ballh < 0) {
				context.makemotion(Motions.MOTION_SIDEKEEP_LEFT);
			} else {
				context.makemotion(Motions.MOTION_SIDEKEEP_RIGHT);
			}
		}
		
		step++;
		super.continueTask(context);
	}

}
