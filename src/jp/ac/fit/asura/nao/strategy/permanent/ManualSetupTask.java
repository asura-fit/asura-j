package jp.ac.fit.asura.nao.strategy.permanent;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.SensorContext;
import jp.ac.fit.asura.nao.Switch;
import jp.ac.fit.asura.nao.communication.RoboCupGameControlData;
import jp.ac.fit.asura.nao.communication.RobotInfo;
import jp.ac.fit.asura.nao.communication.TeamInfo;
import jp.ac.fit.asura.nao.event.RoboCupMessageListener;
import jp.ac.fit.asura.nao.misc.AverageFilter;
import jp.ac.fit.asura.nao.misc.Filter.BooleanFilter;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.GameState;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.StrategySystem;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.Team;

import org.apache.log4j.Logger;

public class ManualSetupTask extends Task implements RoboCupMessageListener {
	private static final Logger log = Logger.getLogger(ManualSetupTask.class);

	private StrategySystem ss;
	private RobotContext robotContext;

	private BooleanFilter chestFilter;
	private BooleanFilter lFootFilter;
	private BooleanFilter rFootFilter;

	private Team lastTeam;
	private Team lastKickoff;
	private boolean lastPenalized;
	private GameState lastState;

	/**
	 *
	 */
	public ManualSetupTask() {
		chestFilter = new AverageFilter.Boolean(6);
		lFootFilter = new AverageFilter.Boolean(10);
		rFootFilter = new AverageFilter.Boolean(10);
	}

	@Override
	public void init(RobotContext context) {
		context.getCommunication().addMessageListener(this);
		ss = context.getStrategy();
		robotContext = context;
	}

	@Override
	public String getName() {
		return "ManualSetupTask";
	}

	@Override
	public void update(RoboCupGameControlData gameData) {
		// gameData.debug();
		if (gameData.getTeam((byte) Team.Red.toInt()).getTeamNumber() == robotContext
				.getTeamId())
			ss.setTeam(Team.Red);
		if (gameData.getTeam((byte) Team.Blue.toInt()).getTeamNumber() == robotContext
				.getTeamId())
			ss.setTeam(Team.Blue);

		if (robotContext.getTeamId() == gameData.getTeam(
				(byte) ss.getTeam().toInt()).getTeamNumber()) {

			// log.debug("teamId:"
			// + robotContext.getTeamId()
			// + " "
			// + gameData.getTeam((byte) ss.getTeam().toInt())
			// .getTeamNumber());
			TeamInfo team = gameData.getTeam((byte) ss.getTeam().toInt());
			RobotInfo player = team.getPlayers()[ss.getContext().hasMotion(
					Motions.NAOJI_WALKER) ? (robotContext.getRobotId() - 1)
					: (robotContext.getRobotId())];

			// log.debug("robotId:" + robotContext.getRobotId() + " penalty: "
			// + player.getPenalty());
			boolean isPenalized = player.getPenalty() != 0;

			byte new_gs = gameData.getState();
			GameState gs = ss.getGameState();

			if (new_gs == RoboCupGameControlData.STATE_READY
					&& (gs == GameState.INITIAL || gs == GameState.PLAYING)) {
				ss.setGameState(GameState.READY);

			} else if (new_gs == RoboCupGameControlData.STATE_SET
					&& gs == GameState.READY) {
				ss.setGameState(GameState.SET);

			} else if (new_gs == RoboCupGameControlData.STATE_PLAYING
					&& gs == GameState.SET) {
				ss.setGameState(GameState.PLAYING);

			} else if (new_gs == RoboCupGameControlData.STATE_FINISHED
					&& gs == GameState.PLAYING) {
				ss.setGameState(GameState.FINISHED);
			}

			Effector e = robotContext.getEffector();
			if (ss.getGameState() == GameState.PLAYING
					&& lastPenalized != isPenalized) {
				if (isPenalized) {
					ss.setPenalized(true);
					log.info("I'm penalized by GameControler.");
					e.say("I'm penalized.");
				} else if (robotContext.getStrategy().isPenalized()
						&& player.getSecsTillUnpenalised() == 0) {
					ss.setPenalized(false);
					log.info("I'm unpenalized by GameControler.");
					e.say("I'm unpenalized.");
				}
			}

			// score
			byte score = team.getScore();
			ss.setOwnScore(score);

			// secsRemaining
			int remaining = gameData.getSecsRemaining();
			ss.setSecsRemaining(remaining);
		}

	}

	@Override
	public void before(StrategyContext context) {
		SensorContext sensor = context.getSensorContext();
		boolean chestPushed = chestFilter.eval(sensor.getSwitch(Switch.Chest));
		boolean lFootPushed = lFootFilter.eval(sensor
				.getSwitch(Switch.LFootLeft)
				&& sensor.getSwitch(Switch.LFootRight));
		boolean rFootPushed = rFootFilter.eval(sensor
				.getSwitch(Switch.RFootLeft)
				&& sensor.getSwitch(Switch.RFootRight));

		// TeamNumberによるチームの切り替え
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

		Effector e = context.getSuperContext().getEffector();
		// 胸ボタンによるステート変更
		if (chestPushed) {
			if (context.getGameState() == GameState.INITIAL
					|| context.getGameState() == GameState.PLAYING) {
				if (!context.isPenalized()) {
					context.setPenalized(true);
					log.info("I'm penalized by ChestButton.");
					e.say("I'm penalized.");
				} else {
					context.setPenalized(false);
					log.info("I'm unpenalized by ChestButton.");
					e.say("I'm unpenalized.");
					if (context.getGameState() != GameState.PLAYING) {
						context.setGameState(GameState.PLAYING);
						log.info("State changed by ChestButton PLAYING");
					}
				}
			}
			context.getScheduler().abort();
		}

		// if (chestPushed) {
		// if (context.getGameState() != GameState.PLAYING) {
		// switch (context.getGameState()) {
		// case INITIAL:
		// context.setGameState(GameState.READY);
		// log.info("State changed by ChestButton READY");
		// break;
		// case READY:
		// context.setGameState(GameState.SET);
		// log.info("State changed by ChestButton SET");
		// break;
		// case SET:
		// context.setGameState(GameState.PLAYING);
		// log.info("State changed by ChestButton PLAYING");
		// break;
		// }
		// } else if (!context.isPenalized()) {
		// // ペナライズ（設定値は決めておいた方がいいかも）
		// context.setPenalized(true);
		// log.info("I'm penalized by ChestButton");
		// e.say("I'm penalized.");
		// } else {
		// // アンペナライズ
		// context.setPenalized(false);
		// log.info("I'm unpenalized by ChestButton");
		// e.say("I'm unpenalized.");
		// }
		// context.getScheduler().abort();
		// }

		// チーム、キックオフの変更
		if (context.getGameState() == GameState.INITIAL) {
			if (lFootPushed) {
				// チームと色替え
				if (context.getTeam() == Team.Red)
					ss.setTeam(Team.Blue);
				else
					ss.setTeam(Team.Red);
				log.info("Team color is changed by LeftBumper:"
						+ context.getTeam());
				e.say("We are " + context.getTeam().name() + " team.");
			}

			// if (rFootPushed) {
			// if (context.getKickOffTeam() == Team.Red)
			// context.setKickOffTeam(Team.Blue);
			// else
			// context.setKickOffTeam(Team.Red);
			// log.info("Kickoff is changed by RightBumper:"
			// + context.getKickOffTeam());
			// e.say("We are " + context.getTeam().name() + " team.");
			// }
		}

		// LEDの表示など
		if (context.getTeam() != lastTeam) {
			lastTeam = context.getTeam();
			if (lastTeam == Team.Red) {
				e.setLed("LFoot/Led/Red", 1.0f);
				e.setLed("LFoot/Led/Blue", 0.0f);
				e.setLed("LFoot/Led/Green", 0.0f);
			}
			if (lastTeam == Team.Blue) {
				e.setLed("LFoot/Led/Red", 0.0f);
				e.setLed("LFoot/Led/Blue", 1.0f);
				e.setLed("LFoot/Led/Green", 0.0f);
			}
		}
		if (context.getKickOffTeam() != lastKickoff) {
			lastKickoff = context.getKickOffTeam();
			if (lastKickoff == context.getTeam()) {
				e.setLed("RFoot/Led/Red", 1.0f);
				e.setLed("RFoot/Led/Blue", 1.0f);
				e.setLed("RFoot/Led/Green", 1.0f);
			} else {
				e.setLed("RFoot/Led/Red", 0.0f);
				e.setLed("RFoot/Led/Blue", 0.0f);
				e.setLed("RFoot/Led/Green", 0.0f);
			}
		}

		if (context.getGameState() != lastState) {
			lastState = context.getGameState();
			// LEDなど. 別のところでやるべき.
			float red;
			float blue;
			float green;
			String text = "";
			switch (lastState) {
			case READY:
				red = green = 0.0f;
				blue = 1.0f;
				text = "Get ready.";
				break;
			case SET:
				// ちょっと赤がきつすぎる.
				red = 0.75f;
				green = 1.0f;
				blue = 0.0f;
				text = "Set.";
				break;
			case PLAYING:
				if (lastPenalized) {
					red = 1.0f;
					green = blue = 0.0f;
				} else {
					green = 1.0f;
					red = blue = 0.0f;
				}
				text = "Play!";
				break;
			case FINISHED:
				text = "Finished.";
			case INITIAL:
			default:
				red = blue = green = 0.0f;
				break;
			}
			e.setLed("ChestBoard/Led/Red", red);
			e.setLed("ChestBoard/Led/Blue", blue);
			e.setLed("ChestBoard/Led/Green", green);
			e.say(text);
		}

		if (context.isPenalized() != lastPenalized) {
			lastPenalized = context.isPenalized();
			float red;
			float blue;
			float green;
			if (lastPenalized) {
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
	}
}
