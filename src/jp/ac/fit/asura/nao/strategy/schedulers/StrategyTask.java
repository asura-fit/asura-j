/*
 * 作成日: 2008/06/12
 */
package jp.ac.fit.asura.nao.strategy.schedulers;

import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public abstract class StrategyTask extends Task {
	public void continueTask(StrategyContext context) {
		assert false;
	}

	abstract void fillQueue(StrategyContext context);
}
