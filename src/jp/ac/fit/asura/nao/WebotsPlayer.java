/*
 * 作成日: 2008/04/10
 */
package jp.ac.fit.asura.nao;

import java.awt.image.BufferedImage;

import javax.swing.JLabel;

import com.cyberbotics.webots.Controller;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class WebotsPlayer extends Controller {
	private static AsuraCore core;

	public static final int SIMULATION_STEP = 40;

	static int camera, left_ultrasound_sensor, right_ultrasound_sensor,
			accelerometer, right_fsr[], left_fsr[], emitter, receiver,
			logo_led;

	static BufferedImage bufferedImage; // image (for GUI)

	static JLabel imageLabel; // swing widget for the image (GUI)

	static JLabel batteryLabel;

	public static void die() {
		robot_console_print("die method called\n");
	}

	public static void reset() {
		core = new AsuraCore(new WebotsEffector(),new WebotsSensor());

		String name = robot_get_name();
		right_fsr = new int[4];
		left_fsr = new int[4];

		if (name.equals("red goal keeper")) {
			core.setId(0);
			core.setTeam(AsuraCore.Team.Red);
		} else if (name.equals("red player 1")) {
			core.setId(1);
			core.setTeam(AsuraCore.Team.Red);
		} else if (name.equals("red player 2")) {
			core.setId(2);
			core.setTeam(AsuraCore.Team.Red);
		} else if (name.equals("red player 3")) {
			core.setId(3);
			core.setTeam(AsuraCore.Team.Red);
		} else if (name.equals("blue goal keeper")) {
			core.setId(0);
			core.setTeam(AsuraCore.Team.Blue);
		} else if (name.equals("blue player 1")) {
			core.setId(1);
			core.setTeam(AsuraCore.Team.Blue);
		} else if (name.equals("blue player 2")) {
			core.setId(2);
			core.setTeam(AsuraCore.Team.Blue);
		} else if (name.equals("blue player 3")) {
			core.setId(3);
			core.setTeam(AsuraCore.Team.Blue);
		} else
			robot_console_print("unable to recognize player position: " + name
					+ "\n");
		camera = robot_get_device("camera");
		camera_enable(camera, 4 * SIMULATION_STEP);
		accelerometer = robot_get_device("accelerometer");
		accelerometer_enable(accelerometer, SIMULATION_STEP);
		left_ultrasound_sensor = robot_get_device("left ultrasound sensor");
		distance_sensor_enable(left_ultrasound_sensor, SIMULATION_STEP);
		right_ultrasound_sensor = robot_get_device("right ultrasound sensor");
		distance_sensor_enable(right_ultrasound_sensor, SIMULATION_STEP);
		right_fsr[0] = robot_get_device("RFsrFL");
		right_fsr[1] = robot_get_device("RFsrFR");
		right_fsr[2] = robot_get_device("RFsrBR");
		right_fsr[3] = robot_get_device("RFsrBL");
		left_fsr[0] = robot_get_device("LFsrFL");
		left_fsr[1] = robot_get_device("LFsrFR");
		left_fsr[2] = robot_get_device("LFsrBR");
		left_fsr[3] = robot_get_device("LFsrBL");
		for (int i = 0; i < 4; i++) {
			touch_sensor_enable(right_fsr[i], SIMULATION_STEP);
			touch_sensor_enable(left_fsr[i], SIMULATION_STEP);
		}
		logo_led = robot_get_device("logo led");
		emitter = robot_get_device("emitter");
		receiver = robot_get_device("receiver");
		receiver_enable(receiver, SIMULATION_STEP);

		core.init();
		core.start();
	}

	public static int run(int step) {
		core.run(SIMULATION_STEP);
		return SIMULATION_STEP;
	}

	public static void main(String args[]) {
		robot_console_print("main method called\n");
		
		// TODO schemeデータとかの解凍処理追加
		
		int step = 0;
		while (true) {
			step++;
			robot_step(run(step));
		}
	}
}
