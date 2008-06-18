/*
 * 作成日: 2008/06/13
 */
package jp.ac.fit.asura.nao.strategy.tactics;

import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class FindBallTask extends Task {
	
	private int onSiteTime;
	
	public String getName() {
		return "FindBallTask";
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(300);
		onSiteTime = 100;
	}

	public void continueTask(StrategyContext context) {
		if (context.getBall().getVision().cf > 0) {
			context.makemotion(0);
			context.getScheduler().abort();
			return;
		}
		
		if (onSiteTime > 0) {
			onSiteTime--;
			context.makemotion(3);
		} else {
			context.makemotion(11);
		}
	}
}
