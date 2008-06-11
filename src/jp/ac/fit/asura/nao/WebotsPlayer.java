/*
 * 作成日: 2008/04/10
 */
package jp.ac.fit.asura.nao;

import java.awt.image.BufferedImage;

import javax.swing.JLabel;

import jp.ac.fit.asura.nao.strategy.StrategySystem;

import com.cyberbotics.webots.Controller;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class WebotsPlayer extends Controller {
	private static AsuraCore core;

	private static RoboCupGameControlData gameControlData;

	public static final int SIMULATION_STEP = 40;

	static int emitter, receiver, logo_led;

	static BufferedImage bufferedImage; // image (for GUI)

	static JLabel imageLabel; // swing widget for the image (GUI)

	static JLabel batteryLabel;

	public static void die() {
		robot_console_print("die method called\n");
	}

	public static void reset() {
		gameControlData = new RoboCupGameControlData();
		core = new AsuraCore(gameControlData, new WebotsEffector(),
				new WebotsSensor());

		String name = robot_get_name();

		if (name.equals("red goal keeper")) {
			core.setId(0);
			core.setTeam(StrategySystem.Team.Red);
		} else if (name.equals("red player 1")) {
			core.setId(1);
			core.setTeam(StrategySystem.Team.Red);
		} else if (name.equals("red player 2")) {
			core.setId(2);
			core.setTeam(StrategySystem.Team.Red);
		} else if (name.equals("red player 3")) {
			core.setId(3);
			core.setTeam(StrategySystem.Team.Red);
		} else if (name.equals("blue goal keeper")) {
			core.setId(0);
			core.setTeam(StrategySystem.Team.Blue);
		} else if (name.equals("blue player 1")) {
			core.setId(1);
			core.setTeam(StrategySystem.Team.Blue);
		} else if (name.equals("blue player 2")) {
			core.setId(2);
			core.setTeam(StrategySystem.Team.Blue);
		} else if (name.equals("blue player 3")) {
			core.setId(3);
			core.setTeam(StrategySystem.Team.Blue);
		} else
			robot_console_print("unable to recognize player position: " + name
					+ "\n");

		logo_led = robot_get_device("logo led");
		emitter = robot_get_device("emitter");
		receiver = robot_get_device("receiver");
		receiver_enable(receiver, SIMULATION_STEP);

		core.init();
		core.start();
	}

	public static int run(int step) {
		updateGameControl();
		core.run(SIMULATION_STEP);
		return SIMULATION_STEP;
	}

	protected static void updateGameControl() {
		while (Controller.receiver_get_queue_length(receiver) > 0) {
			byte[] data = Controller.receiver_get_data(receiver);
			if (RoboCupGameControlData.hasValidHeader(data)) {
				gameControlData.update(data);
				// Controller.robot_console_println("readIncomingMessages():
				// received: " + gameControlData);
			} else
				Controller
						.robot_console_println("readIncomingMessages(): received unexpected message of "
								+ data.length + " bytes");

			Controller.receiver_next_packet(receiver);
		}
	}

	public static void main(String args[]) {
		robot_console_print("main method called\n");

		int step = 0;
		while (true) {
			step++;
			robot_step(run(step));
		}
	}
}
