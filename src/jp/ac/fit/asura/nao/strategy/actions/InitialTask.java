/*
 * 作成日: 2008/06/13
 */
package jp.ac.fit.asura.nao.strategy.actions;

import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class InitialTask extends Task {
	public String getName() {
		return "InitialTask";
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(50);
	}
}
