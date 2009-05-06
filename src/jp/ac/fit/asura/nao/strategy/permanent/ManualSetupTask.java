package jp.ac.fit.asura.nao.strategy.permanent;

import jp.ac.fit.asura.nao.Switch;
import jp.ac.fit.asura.nao.communication.RoboCupGameControlData;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.Team;

public class ManualSetupTask extends Task {
	private boolean chestPushed;
	private boolean lFoodPushed;
	private boolean rFoodPushed;

	@Override
	public String getName() {
		return "ManualSetupTask";
	}

	@Override
	public void before(StrategyContext context) {
		// 胸ボタンによるステート変更
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
	}

}
