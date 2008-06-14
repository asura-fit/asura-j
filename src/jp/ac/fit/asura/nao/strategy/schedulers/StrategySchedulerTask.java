/*
 * 作成日: 2008/06/12
 */
package jp.ac.fit.asura.nao.strategy.schedulers;

import java.util.HashMap;
import java.util.Map;

import jp.ac.fit.asura.nao.RoboCupGameControlData;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.strategy.Role;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.TaskManager;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class StrategySchedulerTask extends BasicSchedulerTask {
	private Map<Role, StrategyTask> strategyTasks;
	private StrategyTask currentStrategy;
	private int prevState;

	public String getName() {
		return StrategySchedulerTask.class.getSimpleName();
	}

	/**
	 * 
	 */
	public StrategySchedulerTask() {
		strategyTasks = new HashMap<Role, StrategyTask>();
	}

	public void init(RobotContext context) {
		super.init(context);
		TaskManager taskManager = context.getStrategy().getTaskManager();
		strategyTasks.put(Role.Goalie, (StrategyTask) taskManager
				.find("GoalieStrategyTask"));
		strategyTasks.put(Role.Striker, (StrategyTask) taskManager
				.find("StrikerStrategyTask"));
		// strategyTasks.put(Role.Libero, (StrategyTask) taskManager
		// .find("LiberoStrategyTask"));
		strategyTasks.put(Role.Libero, (StrategyTask) taskManager
				.find("StrikerStrategyTask"));
		// strategyTasks.put(Role.Defender, (StrategyTask) taskManager
		// .find("DefenderStrategyTask"));
		strategyTasks.put(Role.Defender, (StrategyTask) taskManager
				.find("StrikerStrategyTask"));
		context.getGameControlData().getState();
	}

	public void continueTask(StrategyContext context) {
		// ストラテジを切り替える
		if (currentStrategy != strategyTasks.get(context.getRole())) {
			if (currentStrategy != null)
				currentStrategy.leave(context);
			currentStrategy = strategyTasks.get(context.getRole());
			currentStrategy.enter(context);
		}
		
		if(prevState != context.getGameState().getState()){
			clearQueue();
			setTTL(0);
			prevState = context.getGameState().getState();
		}

		super.continueTask(context);
	}

	protected void fillQueue(StrategyContext context) {
		switch (context.getGameState().getState()) {
		case RoboCupGameControlData.STATE_PLAYING: {
			currentStrategy.fillQueue(context);
			assert !queue.isEmpty();
			break;
		}
		case RoboCupGameControlData.STATE_SET:
		case RoboCupGameControlData.STATE_READY: {
			pushQueue(context.findTask("LookAroundTask"));
			break;
		}
		default: {
			pushQueue(context.findTask("InitialTask"));
			break;
		}
		}
	}
}
