/*
 * 作成日: 2008/06/13
 */
package jp.ac.fit.asura.nao.strategy.schedulers;

import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.vision.VisualObjects;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class StrikerStrategyTask extends StrategyTask {
	public void enter(StrategyContext context) {
		System.out.println("I'm a Striker");
	}

	public void fillQueue(StrategyContext context) {
		if (context.getBall().getVision().getInt(
				VisualObjects.Properties.Confidence) > 0) {
			context.pushQueue("ApproachBallTask");
		} else {
			context.pushQueue("FindBallTask");
		}
	}

	public String getName() {
		return StrikerStrategyTask.class.getSimpleName();
	}
}
