/*
 * 作成日: 2008/06/13
 */
package jp.ac.fit.asura.nao.strategy.schedulers;

import org.apache.log4j.Logger;

import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.vision.VisualObjects;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class StrikerStrategyTask extends StrategyTask {
	private Logger log = Logger.getLogger(StrikerStrategyTask.class);

	public void enter(StrategyContext context) {
		log.info("I'm a Striker");
	}

	public void fillQueue(StrategyContext context) {
		if (context.getBall().getConfidence() > 0) {
			context.pushQueue("ApproachBallTask");
		} else {
			context.pushQueue("FindBallTask");
		}
	}

	public String getName() {
		return StrikerStrategyTask.class.getSimpleName();
	}
}
