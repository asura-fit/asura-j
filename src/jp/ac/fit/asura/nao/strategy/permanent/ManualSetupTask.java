package jp.ac.fit.asura.nao.strategy.permanent;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.SensorContext;
import jp.ac.fit.asura.nao.Switch;
import jp.ac.fit.asura.nao.communication.RoboCupGameControlData;
import jp.ac.fit.asura.nao.misc.MedianFilter;
import jp.ac.fit.asura.nao.misc.Filter.BooleanFilter;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.Team;

import org.apache.log4j.Logger;

public class ManualSetupTask extends Task {
	private static final Logger log = Logger.getLogger(ManualSetupTask.class);

	private BooleanFilter chestFilter;
	private BooleanFilter lFootFilter;
	private BooleanFilter rFootFilter;

	/**
	 *
	 */
	public ManualSetupTask() {
		chestFilter = new MedianFilter.Boolean(5);
		lFootFilter = new MedianFilter.Boolean(5);
		rFootFilter = new MedianFilter.Boolean(5);
	}

	// assigned team number
	final static int teamNum = 4;

	@Override
	public String getName() {
		return "ManualSetupTask";
	}

	@Override
	public void before(StrategyContext context) {
		SensorContext sensor = context.getSensorContext();
		boolean chestPushed = chestFilter.eval(sensor.getSwitch(Switch.Chest));
		boolean lFootPushed = lFootFilter.eval(sensor
				.getSwitch(Switch.LFootLeft)
				|| sensor.getSwitch(Switch.LFootRight));
		boolean rFootPushed = rFootFilter.eval(sensor
				.getSwitch(Switch.RFootLeft)
				|| sensor.getSwitch(Switch.RFootRight));

		// TeamNumberによるチームの切り替え
		if (context.getGameState().getTeam((byte) Team.Red.toInt())
				.getTeamNumber() == teamNum
				&& context.getTeam() != Team.Red)
			context.setTeam(Team.Red);
		if (context.getGameState().getTeam((byte) Team.Blue.toInt())
				.getTeamNumber() == teamNum
				&& context.getTeam() != Team.Blue)
			context.setTeam(Team.Blue);

		if (!chestFilter.isFilled())
			chestPushed = false;
		if (!lFootFilter.isFilled())
			lFootPushed = false;
		if (!rFootFilter.isFilled())
			rFootPushed = false;

		if (chestPushed)
			chestFilter.clear();
		if (lFootPushed)
			lFootFilter.clear();
		if (rFootPushed)
			rFootFilter.clear();

		RoboCupGameControlData gc = context.getGameState();
		Effector e = context.getSuperContext().getEffector();
		// 胸ボタンによるステート変更
		if (chestPushed) {
			if (gc.getState() != RoboCupGameControlData.STATE_PLAYING) {
				gc.setState((byte) (gc.getState() + 1));
				log.info("State changed by ChestButton:" + gc.getState());
			} else if (gc.getTeam((byte) context.getTeam().toInt())
					.getPlayers()[context.getSuperContext().getRobotId()]
					.getPenalty() == 0) {
				// ペナライズ（設定値は決めておいた方がいいかも）
				gc.getTeam((byte) context.getTeam().toInt()).getPlayers()[context
						.getSuperContext().getRobotId()].setPenalty((short) 1);
				log.info("I'm penalized by ChestButton");
				e.say("I'm penalized.");
			} else {
				// アンペナライズ
				gc.getTeam((byte) context.getTeam().toInt()).getPlayers()[context
						.getSuperContext().getRobotId()].setPenalty((short) 0);
				log.info("I'm unpenalized by ChestButton");
				e.say("I'm unpenalized.");
			}
			context.getScheduler().abort();
		}

		// チーム、キックオフの変更
		if (gc.getState() == RoboCupGameControlData.STATE_INITIAL) {
			if (lFootPushed) {
				// チームと色替え
				if (context.getTeam() == Team.Red)
					context.setTeam(Team.Blue);
				else
					context.setTeam(Team.Red);
				log.info("Team color is changed by LeftBumper:"
						+ context.getTeam());
				e.say("We are " + context.getTeam().name() + " team.");
			}

			if (rFootPushed) {
				if (gc.getKickOffTeam() == 1)
					gc.setKickOffTeam((byte) 0);
				else
					gc.setKickOffTeam((byte) 1);
				log.info("Kickoff is changed by RightBumper:"
						+ gc.getKickOffTeam());
				if (gc.getKickOffTeam() == 1)
					e.say("We are kick off team.");
			}
		}
	}
}
