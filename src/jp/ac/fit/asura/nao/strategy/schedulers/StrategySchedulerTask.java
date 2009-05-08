/*
 * 作成日: 2008/06/12
 */
package jp.ac.fit.asura.nao.strategy.schedulers;

import java.util.HashMap;
import java.util.Map;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.communication.RoboCupGameControlData;
import jp.ac.fit.asura.nao.strategy.Role;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.TaskManager;

/**
 * @author $Author: sey $
 *
 * @version $Id: StrategySchedulerTask.java 709 2008-11-23 07:40:31Z sey $
 *
 */
public class StrategySchedulerTask extends BasicSchedulerTask {
	private Map<Role, StrategyTask> strategyTasks;
	private StrategyTask currentStrategy;
	private int prevState;
	private boolean lastPenalized;

	public String getName() {
		return StrategySchedulerTask.class.getSimpleName();
	}

	/**
	 *
	 */
	public StrategySchedulerTask() {
		strategyTasks = new HashMap<Role, StrategyTask>();
		prevState = -1;
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

		boolean isPenalized = context.getGameState().getTeam(
				(byte) context.getTeam().toInt()).getPlayers()[context
				.getSuperContext().getRobotId()].getPenalty() == 1;

		if (prevState != context.getGameState().getState()) {
			clearQueue();
			setTTL(0);
			prevState = context.getGameState().getState();

			// LEDなど. 別のところでやるべき.
			float red;
			float blue;
			float green;
			switch (prevState) {
			case RoboCupGameControlData.STATE_READY:
				red = green = 0.0f;
				blue = 1.0f;
				break;
			case RoboCupGameControlData.STATE_SET:
				// ちょっと赤がきつすぎる.
				red = 0.75f;
				green = 1.0f;
				blue = 0.0f;
				break;
			case RoboCupGameControlData.STATE_PLAYING:
				if (isPenalized) {
					red = 1.0f;
					green = blue = 0.0f;
				} else {
					green = 1.0f;
					red = blue = 0.0f;
				}
				break;
			case RoboCupGameControlData.STATE_INITIAL:
			case RoboCupGameControlData.STATE_FINISHED:
			default:
				red = blue = green = 0.0f;
				break;
			}
			Effector e = context.getSuperContext().getEffector();
			e.setLed("ChestBoard/Led/Red", red);
			e.setLed("ChestBoard/Led/Blue", blue);
			e.setLed("ChestBoard/Led/Green", green);
		}
		if (prevState == RoboCupGameControlData.STATE_PLAYING
				&& lastPenalized != isPenalized) {
			float red;
			float blue;
			float green;
			Effector e = context.getSuperContext().getEffector();
			if (isPenalized) {
				red = 1.0f;
				green = blue = 0.0f;
			} else {
				green = 1.0f;
				red = blue = 0.0f;
			}
			e.setLed("ChestBoard/Led/Red", red);
			e.setLed("ChestBoard/Led/Blue", blue);
			e.setLed("ChestBoard/Led/Green", green);
		}

		lastPenalized = isPenalized;
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
			// LookAroundがびみょーなのでオフに.
			// pushQueue(context.findTask("LookAroundTask"));
			pushQueue(context.findTask("InitialTask"));
			break;
		case RoboCupGameControlData.STATE_READY:
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
