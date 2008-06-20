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
			context.makemotion_head_rel((float) (-0.4 * angle.getX()),
					(float) (0.4 * angle.getY()));
		} else {
			// 楕円を描くように動かす
			float yaw = (float) (Math.sin(step * 0.1) * 45.0);
			float pitch = (float) (Math.cos(step * 0.1) * 20.0 + 25.0);
			context.makemotion_head(yaw, pitch);
			step++;
		}
	}

	public void enter(StrategyContext context) {
		step = 0;
	}

	public String getName() {
		return "BallTracking";
	}

	public void leave(StrategyContext context) {
	}
}
