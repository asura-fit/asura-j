package jp.ac.fit.asura.nao.strategy.schedulers;

import org.apache.log4j.Logger;

import jp.ac.fit.asura.nao.strategy.StrategyContext;

public class DefenderStrategyTask extends StrategyTask {
	private Logger log = Logger.getLogger(DefenderStrategyTask.class);

	@Override
	public void enter(StrategyContext context) {
		log.info("I'm a Defender.");
	}

	@Override
	void fillQueue(StrategyContext context) {
		// TODO 自動生成されたメソッド・スタブ
		if (context.getWalkFlag()) {
			if (context.getBall().getConfidence() > 0) {
				// ここでディフェンダーのタスクを決める。
				 context.pushQueue("DefenceTask");
				//context.pushQueue("GetBallTask");
			} else {
				context.pushQueue("FindBallTask");

			}
		} else {
			context.pushQueue("KickOff03Task");
		}
	}

	@Override
	public String getName() {
		return "DefenderStrategyTask";
	}

}
