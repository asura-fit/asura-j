/*
 * 作成日: 2008/04/10
 */
package jp.ac.fit.asura.nao.webots;

import jp.ac.fit.asura.nao.AsuraCore;
import jp.ac.fit.asura.nao.strategy.Team;

import com.cyberbotics.webots.Controller;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class WebotsPlayer extends Controller {
	private static AsuraCore core;

	public static final int SIMULATION_STEP = 40;

	public static void die() {
		robot_console_print("die method called\n");
	}

	public static void reset() {
		WebotsDriver driver = new WebotsDriver();
		core = new AsuraCore(driver.new WebotsEffector(),
				driver.new WebotsSensor(), new WebotsDatagramService());

		String name = robot_get_name();

		if (name.equals("red goal keeper")) {
			core.setId(0);
			core.setTeam(Team.Red);
		} else if (name.equals("red player 1")) {
			core.setId(1);
			core.setTeam(Team.Red);
		} else if (name.equals("red player 2")) {
			core.setId(2);
			core.setTeam(Team.Red);
		} else if (name.equals("red player 3")) {
			core.setId(3);
			core.setTeam(Team.Red);
		} else if (name.equals("blue goal keeper")) {
			core.setId(0);
			core.setTeam(Team.Blue);
		} else if (name.equals("blue player 1")) {
			core.setId(1);
			core.setTeam(Team.Blue);
		} else if (name.equals("blue player 2")) {
			core.setId(2);
			core.setTeam(Team.Blue);
		} else if (name.equals("blue player 3")) {
			core.setId(3);
			core.setTeam(Team.Blue);
		} else
			robot_console_print("unable to recognize player position: " + name
					+ "\n");

		core.init();
		core.start();
	}

	public static int run(int step) {
		try {
			core.run(SIMULATION_STEP);
		} catch (Throwable ex) {
			ex.printStackTrace();
			assert false : ex;
		}
		return SIMULATION_STEP;
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
