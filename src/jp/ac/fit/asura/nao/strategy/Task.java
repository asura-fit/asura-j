/*
 * 作成日: 2008/05/18
 */
package jp.ac.fit.asura.nao.strategy;

import jp.ac.fit.asura.nao.RobotContext;

/**
 * @author $Author: sey $
 * 
 * @version $Id$
 * 
 */
public abstract class Task {
	abstract public String getName();

	public void init(RobotContext context) {
	}

	public void before(StrategyContext context) {
	}
	
	public void after(StrategyContext context) {
	}
	
	public void enter(StrategyContext context) {
	}

	public void leave(StrategyContext context) {
	}

	public void continueTask(StrategyContext context) {
	}

	public boolean canExecute(StrategyContext context) {
		return true;
	}

}
