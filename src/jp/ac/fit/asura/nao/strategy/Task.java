/*
 * 作成日: 2008/05/18
 */
package jp.ac.fit.asura.nao.strategy;

import jp.ac.fit.asura.nao.RobotContext;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public interface Task {
	public String getName();
	
	public void init(RobotContext context);

	public void enter(StrategyContext context);

	public void leave(StrategyContext context);

	public void step(StrategyContext context);

}
