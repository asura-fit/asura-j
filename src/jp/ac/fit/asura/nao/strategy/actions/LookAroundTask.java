/*
 * 作成日: 2008/06/13
 */
package jp.ac.fit.asura.nao.strategy.actions;

import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class LookAroundTask extends Task {
	private int step;

	public String getName() {
		return "LookAroundTask";
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(100);
		step = 0;
	}

	public void continueTask(StrategyContext context) {
		float yaw = (float) (Math.sin(step * Math.PI / 100.0 * 60.0));
		float pitch = (float) (Math.sin(step * Math.PI / 50.0 * 20.0) + 40.0);
		context.makemotion_head(yaw, pitch);
		step++;
	}
}
