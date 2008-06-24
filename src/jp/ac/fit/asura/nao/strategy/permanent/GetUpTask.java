/*
 * 作成日: 2008/06/15
 */
package jp.ac.fit.asura.nao.strategy.permanent;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class GetUpTask extends Task {
	private int fallDownCount;

	public void init(RobotContext context) {
	}

	public boolean canExecute(StrategyContext context) {
		return true;
	}

	public void after(StrategyContext context) {
		float ax = context.getSuperContext().getSensor().getAccelX();
		float ay = context.getSuperContext().getSensor().getAccelY();
		float az = context.getSuperContext().getSensor().getAccelZ();

		if (ay > -5.0 && (Math.abs(ax) > 8.0 || Math.abs(az) > 5.0)) {
			fallDownCount++;
			if (fallDownCount > 5) {
				System.out.println("Fall down state detected.");
				context.makemotion(1);
			}
		} else {
			fallDownCount = 0;
		}

	}

	public void enter(StrategyContext context) {
	}

	public String getName() {
		return "GetUpTask";
	}

	public void leave(StrategyContext context) {
	}
}
