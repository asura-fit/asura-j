package jp.ac.fit.asura.nao.strategy.tactics;

import org.apache.log4j.Logger;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;

public class DefenceTask extends Task {
	private static final Logger log = Logger.getLogger(DefenceTask.class);

	@Override
	public void continueTask(StrategyContext context) {
		log.info("Enter now.");
	}

	@Override
	public void enter(StrategyContext context) {
		// TODO 自動生成されたメソッド・スタブ
		super.enter(context);
	}

	@Override
	public void init(RobotContext context) {
		// TODO 自動生成されたメソッド・スタブ
		super.init(context);
	}

	@Override
	public void leave(StrategyContext context) {
		// TODO 自動生成されたメソッド・スタブ
		super.leave(context);
	}

	@Override
	public String getName() {
		// TODO 自動生成されたメソッド・スタブ
		return "DefenceTask";
	}

}
