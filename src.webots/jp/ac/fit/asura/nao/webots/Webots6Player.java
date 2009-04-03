/*
 * 作成日: 2008/12/30
 */
package jp.ac.fit.asura.nao.webots;

import jp.ac.fit.asura.nao.AsuraCore;
import jp.ac.fit.asura.nao.strategy.Team;

import com.cyberbotics.webots.controller.Robot;

/**
 * @author sey
 *
 * @version $Id: $
 *
 */
public class Webots6Player extends Robot {
	private static AsuraCore core;

	public static final int SIMULATION_STEP = 40;

	public static void main(String args[]) {
		Webots6Player player = new Webots6Player();
		Webots6Driver driver = new Webots6Driver(player);
		core = new AsuraCore(driver.getEffector(), driver.getSensor(),
				new Webots6DatagramService(player), new Webots6Camera(player));

		String name = player.getName();

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
		} else {
			Robot.javaPrintStderr("unable to recognize player position: "
					+ name + "\n");
		}

		core.init();
		core.start();

		int step = 0;
		while (true) {
			step++;
			player.step(SIMULATION_STEP);
			try {
				core.run(SIMULATION_STEP);
			} catch (Throwable ex) {
				ex.printStackTrace();
				assert false : ex;
			}
		}
	}
}
