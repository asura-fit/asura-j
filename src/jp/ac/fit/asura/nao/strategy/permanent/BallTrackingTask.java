/*
 * 作成日: 2008/05/18
 */
package jp.ac.fit.asura.nao.strategy.permanent;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.vision.VisualObject;

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
		if(context.isHeadSet())
			return;
		
		VisualObject vo = context.getBall().getVision();
		if (vo.cf > 0) {
			context.makemotion_head_rel((float) (-0.4 * vo.angle.getX()),
					(float) (0.4 * vo.angle.getY()));
		} else {
			float yaw = (float) (Math.sin(step * Math.PI / 100.0 * 60.0));
			float pitch = (float) (Math.sin(step * Math.PI / 50.0 * 20.0) + 40.0);
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
