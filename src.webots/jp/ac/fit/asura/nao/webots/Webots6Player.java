/*
 * 作成日: 2008/12/30
 */
package jp.ac.fit.asura.nao.webots;

import jp.ac.fit.asura.nao.AsuraCore;
import jp.ac.fit.asura.nao.strategy.GameState;
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
		Webots6Camera camera = new Webots6Camera(player);
		core = new AsuraCore(driver.getEffector(), driver.getSensor(),
				new Webots6DatagramService(player), camera);

		String id = args[0];
		String team = args[1];

		try {
			core.init();

			if (team.equals("1")) {
				core.setTeamId(1);
				core.getRobotContext().getStrategy().setTeam(Team.Red);
				if (id.equals("0")) {
					// red goal keeper
					core.setId(0);
				} else if (id.equals("1")) {
					// red player 1
					core.setId(1);
				} else if (id.equals("2")) {
					// red player 2
					core.setId(2);
				} else if (id.equals("3")) {
					// red player 3
					core.setId(3);
				} else {
					System.err.println("unable to recognize player. args[0]: " + id + "\n");
				}
			} else if (team.equals("0")) {
				core.setTeamId(0);
				core.getRobotContext().getStrategy().setTeam(Team.Blue);
				if (id.equals("0")) {
					// blue goal keeper
					core.setId(0);
				} else if (id.equals("1")) {
					// blue player 1
					core.setId(1);
				} else if (id.equals("2")) {
					// blue player 2
					core.setId(2);
				} else if (id.equals("3")) {
					// blue player 3
					core.setId(3);
				} else {
					System.err.println("unable to recognize player. args[0]: " + id + "\n");
				}
			} else {
				System.err.println("unable to recognize team. args[1]: " + team + "\n");
			}

			core.start();
			core.getRobotContext().getStrategy()
					.setGameState(GameState.PLAYING);
		} catch (Exception e) {
			e.printStackTrace();
			assert false;
			return;
		}

		Object lock = new Object();
		synchronized (lock) {
			try {
				lock.wait();
			} catch (Exception e) {
			}
		}
	}
}
