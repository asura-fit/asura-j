/*
 * 作成日: 2008/06/13
 */
package jp.ac.fit.asura.nao.strategy.tactics;

import java.awt.geom.Point2D;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.vision.VisualObjects;
import jp.ac.fit.asura.nao.vision.objects.VisualObject;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class ApproachBallTask extends Task {
	public String getName() {
		return "ApproachBallTask";
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(300);
	}

	public void continueTask(StrategyContext context) {
		VisualObject ball = context.getBall().getVision();
		if (ball.getInt(VisualObjects.Properties.Confidence) == 0) {
			context.getScheduler().abort();
			return;
		}

		double ballAngle = ball.get(Point2D.class,
				VisualObjects.Properties.Angle).getX()
				+ Math.toDegrees(context.getSuperContext().getSensor()
						.getJoint(Joint.HeadYaw));

		if (ballAngle > 20) {
			context.makemotion(10);
		} else if (ballAngle < -20) {
			context.makemotion(11);
		} else {
			context.makemotion(15);
		}
	}
}
