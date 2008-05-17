/*
 * 作成日: 2008/05/07
 */
package jp.ac.fit.asura.nao;

import java.util.EnumMap;

import com.cyberbotics.webots.Controller;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class WebotsSensor implements Sensor {
	private EnumMap<Joint, Integer> joints;
	private int camera;

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
