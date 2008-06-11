/*
 * 作成日: 2008/05/07
 */
package jp.ac.fit.asura.nao;

import java.util.EnumMap;

import com.cyberbotics.webots.Controller;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class WebotsSensor implements Sensor {
	private EnumMap<Joint, Integer> joints;
	private int camera;
	private int left_ultrasound_sensor;
	private int right_ultrasound_sensor;
	private int accelerometer;
	private int right_fsr[];
	private int left_fsr[];

	/**
	 * 
	 */
	public WebotsSensor() {
		joints = new EnumMap<Joint, Integer>(Joint.class);
		for (Joint joint : Joint.values()) {
			int device = Controller.robot_get_device(joint.toString());
			Controller.servo_enable_position(device,
					WebotsPlayer.SIMULATION_STEP);
			joints.put(joint, device);
		}

		camera = Controller.robot_get_device("camera");
		Controller.camera_enable(camera, 4 * 40);

		accelerometer = Controller.robot_get_device("accelerometer");
		Controller.accelerometer_enable(accelerometer,
				WebotsPlayer.SIMULATION_STEP);
		left_ultrasound_sensor = Controller
				.robot_get_device("left ultrasound sensor");
		Controller.distance_sensor_enable(left_ultrasound_sensor,
				WebotsPlayer.SIMULATION_STEP);
		right_ultrasound_sensor = Controller
				.robot_get_device("right ultrasound sensor");
		Controller.distance_sensor_enable(right_ultrasound_sensor,
				WebotsPlayer.SIMULATION_STEP);
		
		right_fsr = new int[4];
		right_fsr[0] = Controller.robot_get_device("RFsrFL");
		right_fsr[1] = Controller.robot_get_device("RFsrFR");
		right_fsr[2] = Controller.robot_get_device("RFsrBR");
		right_fsr[3] = Controller.robot_get_device("RFsrBL");

		left_fsr = new int[4];
		left_fsr[0] = Controller.robot_get_device("LFsrFL");
		left_fsr[1] = Controller.robot_get_device("LFsrFR");
		left_fsr[2] = Controller.robot_get_device("LFsrBR");
		left_fsr[3] = Controller.robot_get_device("LFsrBL");
		

		for (int i = 0; i < 4; i++) {
			Controller.touch_sensor_enable(right_fsr[i], WebotsPlayer.SIMULATION_STEP);
			Controller.touch_sensor_enable(left_fsr[i], WebotsPlayer.SIMULATION_STEP);
		}
	}

	public float getJoint(Joint joint) {
		assert joints.containsKey(joint);
		return Controller.servo_get_position(joints.get(joint));
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see jp.ac.fit.asura.nao.Sensor#getImage()
	 */
	public Image getImage() {
		int[] data = Controller.camera_get_image(camera);
		int width = Controller.camera_get_width(camera);
		int height = Controller.camera_get_height(camera);
		return new Image(data, width, height);
	}

}
