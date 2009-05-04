/*
 * 作成日: 2008/06/12
 */
package jp.ac.fit.asura.nao.strategy.schedulers;

import java.util.HashMap;
import java.util.Map;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.Switch;
import jp.ac.fit.asura.nao.communication.RoboCupGameControlData;
import jp.ac.fit.asura.nao.strategy.Role;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.TaskManager;
import jp.ac.fit.asura.nao.strategy.Team;

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
	private boolean chestPushed;
	private boolean lFoodPushed;
	private boolean rFoodPushed;

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
		// 胸ボタンによるステート変更（ここに書いていいのかは知らない）
		if (chestPushed && !context.getSensorContext().getSwitch(Switch.Chest)) {
			if (context.getGameState().getState() < RoboCupGameControlData.STATE_PLAYING) {
				context.getGameState().setState(
						(byte) (context.getGameState().getState() + 1));
			} else if (context.getGameState().getTeam(
					(byte) context.getTeam().toInt()).getPlayers()[context
					.getSuperContext().getRobotId()].getPenalty() == 0) {
				// ペナライズ（設定値は決めておいた方がいいかも）
				context.getGameState()
						.getTeam((byte) context.getTeam().toInt()).getPlayers()[context
						.getSuperContext().getRobotId()].setPenalty((short) 1);
			} else {
				// アンペナライズ
				context.getGameState()
						.getTeam((byte) context.getTeam().toInt()).getPlayers()[context
						.getSuperContext().getRobotId()].setPenalty((short) 0);
			}
		}
		chestPushed = context.getSensorContext().getSwitch(Switch.Chest);

		// チーム、キックオフの変更
		if (context.getGameState().getState() == RoboCupGameControlData.STATE_INITIAL) {
			if (lFoodPushed
					&& !(context.getSensorContext().getSwitch(Switch.LFootLeft) || context
							.getSensorContext().getSwitch(Switch.LFootRight))) {
				// チームと色替え
				if (context.getTeam() == Team.Red) {
					context.setTeam(Team.Blue);
					context.getSuperContext().getEffector().setLed(
							"LFoot/Led/Red", 0.0f);
					context.getSuperContext().getEffector().setLed(
							"LFoot/Led/Blue", 1.0f);
					context.getSuperContext().getEffector().setLed(
							"LFoot/Led/Green", 0.0f);
				} else {
					context.setTeam(Team.Red);
					context.getSuperContext().getEffector().setLed(
							"LFoot/Led/Red", 1.0f);
					context.getSuperContext().getEffector().setLed(
							"LFoot/Led/Blue", 0.0f);
					context.getSuperContext().getEffector().setLed(
							"LFoot/Led/Green", 0.0f);
				}
			}
			lFoodPushed = context.getSensorContext()
					.getSwitch(Switch.LFootLeft)
					|| context.getSensorContext().getSwitch(Switch.LFootRight);

			if (rFoodPushed
					&& !(context.getSensorContext().getSwitch(Switch.RFootLeft) || context
							.getSensorContext().getSwitch(Switch.RFootRight))) {
				if (context.getGameState().getKickOffTeam() == 1)
					context.getGameState().setKickOffTeam((byte) 0);
				else
					context.getGameState().setKickOffTeam((byte) 1);
			}
			rFoodPushed = context.getSensorContext()
					.getSwitch(Switch.RFootLeft)
					|| context.getSensorContext().getSwitch(Switch.RFootRight);

		}

		// ストラテジを切り替える
		if (currentStrategy != strategyTasks.get(context.getRole())) {
			if (currentStrategy != null)
				currentStrategy.leave(context);
			currentStrategy = strategyTasks.get(context.getRole());
			currentStrategy.enter(context);
		}

		if (prevState != context.getGameState().getState()) {
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
			pushQueue(context.findTask("LookAroundTask"));
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
