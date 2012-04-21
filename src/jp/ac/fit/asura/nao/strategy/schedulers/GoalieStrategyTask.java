/*
 * 作成日: 2008/06/13
 */
package jp.ac.fit.asura.nao.strategy.schedulers;

import org.apache.log4j.Logger;

import jp.ac.fit.asura.nao.strategy.StrategyContext;

/**
 * @author $Author: sey $
 * 
 * @version $Id: GoalieStrategyTask.java 709 2008-11-23 07:40:31Z sey $
 * 
 */
public class GoalieStrategyTask extends StrategyTask {
	private Logger log = Logger.getLogger(GoalieStrategyTask.class);

	public void enter(StrategyContext context) {
		log.info("I'm a Goalie");
	}

	void fillQueue(StrategyContext context) {
		context.pushQueue("GoalieTask");
	}

	public String getName() {
		return GoalieStrategyTask.class.getSimpleName();
	}

}
