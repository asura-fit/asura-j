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
		context.pushQueue("DefenceTask");

	}

	@Override
	public String getName() {
		return "DefenderStrategyTask";
	}

}
