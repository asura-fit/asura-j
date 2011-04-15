/*
 * 作成日: 2008/06/12
 */
package jp.ac.fit.asura.nao.strategy.schedulers;

import java.util.HashMap;
import java.util.Map;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.strategy.GameState;
import jp.ac.fit.asura.nao.strategy.Role;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.TaskManager;

/**
 *
 * RoboCup用のストラテジを実装しているスケジューラ.
 *
 * このスケジューラはStrategyTaskによって、Roleを切り替えています(Strategyパターン).
 *
 * ペナライズ時の処理、状態切り替え時の処理なども実装.
 *
 * @author $Author: sey $
 *
 * @version $Id: StrategySchedulerTask.java 709 2008-11-23 07:40:31Z sey $
 *
 */
public class StrategySchedulerTask extends BasicSchedulerTask {
	private Map<Role, StrategyTask> strategyTasks;
	private StrategyTask currentStrategy;
	private GameState currentState;
	private boolean lastPenalized;

	public String getName() {
		return StrategySchedulerTask.class.getSimpleName();
	}

	/**
	 *
	 */
	public StrategySchedulerTask() {
		strategyTasks = new HashMap<Role, StrategyTask>();
		currentState = null;
		lastPenalized = false;
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
				.find("DefenderStrategyTask"));
	}

	public void continueTask(StrategyContext context) {
		// ストラテジを切り替える
		if (currentStrategy != strategyTasks.get(context.getRole())) {
			if (currentStrategy != null)
				currentStrategy.leave(context);
			currentStrategy = strategyTasks.get(context.getRole());
			currentStrategy.enter(context);
		}

		if (currentState != context.getGameState()) {
			// ゲームステートが変更された場合はタスクをすべてキャンセルして再スケジューリング
			clearQueue();
			setTTL(0);
			currentState = context.getGameState();
		}

		// ペナライズ状態が変更された場合はキャンセルして再スケジューリング
		if (lastPenalized != context.isPenalized())
			context.getScheduler().abort();
		lastPenalized = context.isPenalized();
		super.continueTask(context);
	}

	protected void fillQueue(StrategyContext context) {
		// RoboCup用のスケジュール動作
		switch (currentState) {
		case PLAYING: {
			// PLAY中はStrategyTaskに処理を委譲
			if (lastPenalized)
				pushQueue(context.findTask("InitialTask"));
			else
				currentStrategy.fillQueue(context);
			assert !queue.isEmpty();
			break;
		}
		case SET:
			// LookAroundがびみょーなのでオフに.
			// pushQueue(context.findTask("LookAroundTask"));
			pushQueue(context.findTask("InitialTask"));
			break;
		case READY:
			// pushQueue(context.findTask("GotoReadyPositionTask"));
			pushQueue(context.findTask("InitialTask"));
			break;
		default: {
			pushQueue(context.findTask("InitialTask"));
			break;
		}
		}
	}
}
