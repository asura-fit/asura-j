/*
 * 作成日: 2008/05/18
 */
package jp.ac.fit.asura.nao.strategy.permanent;

import java.awt.geom.Point2D;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.vision.VisualObjects;
import jp.ac.fit.asura.nao.vision.objects.VisualObject;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class BallTrackingTask extends Task {
	private int step;

	public void init(RobotContext context) {
	}

	public boolean canExecute(StrategyContext context) {
		return true;
	}

	public void after(StrategyContext context) {
		// 頭が動かされていたら実行しない
		if (context.isHeadSet())
			return;

		VisualObject vo = context.getBall().getVision();
		if (vo.getInt(VisualObjects.Properties.Confidence) > 0) {
			Point2D angle = vo.get(Point2D.class,
					VisualObjects.Properties.Angle);
			context.makemotion_head_rel((float) (-0.4 * Math.toDegrees(angle
					.getX())), (float) (0.4 * Math.toDegrees(angle.getY())));
			return;
		} else if (context.getBall().getConfidence() > 800) {
			return;
		} else {
			// 8の字
			float yaw = (float) (Math.sin(step * 0.15) * 60.0);
			float pitch = (float) (Math.cos(step * 0.15) * 20.0 + 30.0);
			context.makemotion_head(yaw, pitch);
			step++;
		}
	}

	public String getName() {
		return "BallTracking";
	}
}
