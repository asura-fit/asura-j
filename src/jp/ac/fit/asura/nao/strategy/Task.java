/*
 * 作成日: 2008/05/18
 */
package jp.ac.fit.asura.nao.strategy;

import jp.ac.fit.asura.nao.RobotContext;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public interface Task {
	public String getName();

	public void enter(RobotContext context);

	public void leave(RobotContext context);

	public void step(RobotContext context);

}
