/*
 * 作成日: 2008/06/13
 */
package jp.ac.fit.asura.nao.strategy.schedulers;

import jp.ac.fit.asura.nao.strategy.StrategyContext;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class StrikerStrategyTask extends StrategyTask {
	public void fillQueue(StrategyContext context) {
		if (context.getBall().getVision().cf > 0) {
			context.pushQueue("ApproachBallTask");
		} else {
			context.pushQueue("FindBallTask");
		}
	}

	public String getName() {
		return StrikerStrategyTask.class.getSimpleName();
	}
}
