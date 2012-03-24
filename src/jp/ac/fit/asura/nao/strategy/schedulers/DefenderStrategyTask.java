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
	if (context.getBall().getConfidence() > 0) {
			//if (!context.getWalkFlag()) {
				// 最初はとりあえず距離を詰めて蹴るだけ
			//	context.pushQueue("DefenceAttackTask");
			//} else {
				// Ballが見えていたらアプローチする
		      //context.pushQueue("GetBallTask");
				context.pushQueue("DefenceTask");
			//}
		} else {
			context.pushQueue("FindBallTask");
	}
	}

	@Override
	public String getName() {
		return "DefenderStrategyTask";
	}

}
