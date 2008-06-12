/*
 * 作成日: 2008/06/13
 */
package jp.ac.fit.asura.nao.strategy.tactics;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.vision.VisualObject;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 *
 */
public class GetToBallTask extends Task {
	public String getName() {
		return "GetToBallTask";
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(300);
	}
	
	public void continueTask(StrategyContext context) {
		VisualObject ball = context.getBall().getVision();
		if(ball.cf == 0){
			context.getScheduler().abort();
			return;
		}
		
		double ballAngle = ball.angle.getX() + Math.toDegrees(context.getSuperContext().getSensor().getJoint(Joint.HeadYaw));
		
		if(ballAngle > 20){
			context.makemotion(10);
		}else if(ballAngle < -20){
			context.makemotion(11);
		}else{
			context.makemotion(15);
		}
	}
}
