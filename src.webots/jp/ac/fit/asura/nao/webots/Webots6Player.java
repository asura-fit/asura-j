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
		TimeBarier motionBarier = new TimeBarier();
		TimeBarier visualBarier = new TimeBarier();
		Webots6Driver driver = new Webots6Driver(player, motionBarier,
				visualBarier);
		Webots6Camera camera = new Webots6Camera(player, motionBarier,
				visualBarier);
		core = new AsuraCore(driver.getEffector(), driver.getSensor(),
				new Webots6DatagramService(player), camera);

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
			System.err.println("unable to recognize player position: " + name
					+ "\n");
		}

		try {
			core.init();
			core.start();
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
