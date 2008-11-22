/*
 * 作成日: 2008/09/27
 */
package jp.ac.fit.asura.nao.webots;

import java.util.EnumMap;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.Image;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.PressureSensor;

import com.cyberbotics.webots.Controller;

/**
 * @author $Author: sey $
 *
 * @version $Id: $
 *
 */
public class WebotsDriver {
	private EnumMap<Joint, Integer> joints;
	private EnumMap<PressureSensor, Integer> fsr;
	private int camera;
	private int left_ultrasound_sensor;
	private int right_ultrasound_sensor;
	private int accelerometer;

	private int gps;

	private float jointValues[];

	private float jointForces[];

	private boolean power;

	/**
	 *
	 */
	public WebotsDriver() {
		joints = new EnumMap<Joint, Integer>(Joint.class);
		for (Joint joint : Joint.values()) {
			int device = Controller.robot_get_device(joint.toString());
			Controller.servo_enable_position(device,
					WebotsPlayer.SIMULATION_STEP);
			joints.put(joint, device);
		}
		jointValues = new float[Joint.values().length];
		jointForces = new float[Joint.values().length];

		fsr = new EnumMap<PressureSensor, Integer>(PressureSensor.class);
		for (PressureSensor ts : PressureSensor.values()) {
			int device = Controller.robot_get_device(ts.getDeviceTag());
			Controller
					.touch_sensor_enable(device, WebotsPlayer.SIMULATION_STEP);
			fsr.put(ts, device);
		}

		camera = Controller.robot_get_device("camera");
		Controller.camera_enable(camera, WebotsPlayer.SIMULATION_STEP);

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

		// GPSセンサ
		gps = Controller.robot_get_device("gps");
		Controller.gps_enable(gps, WebotsPlayer.SIMULATION_STEP);

		for (PressureSensor ts : PressureSensor.values()) {
			int device = Controller.robot_get_device(ts.getDeviceTag());
			fsr.put(ts, device);
			Controller
					.touch_sensor_enable(device, WebotsPlayer.SIMULATION_STEP);
		}

		power = true;
	}

	protected class WebotsSensor implements Sensor {
		public void before() {
			for (Joint joint : Joint.values()) {
				jointValues[joint.ordinal()] = Controller
						.servo_get_position(joints.get(joint));
			}
		}

		public void after() {
		}

		/**
		 * 指定された関節の角度のセンサー値をラジアンで返します.
		 */
		public float getJoint(Joint joint) {
			assert joints.containsKey(joint);
			return jointValues[joint.ordinal()];
		}

		public float getJointDegree(Joint joint) {
			return (float) Math.toDegrees(getJoint(joint));
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
			float hFov = Controller.camera_get_fov(camera);
			float vFov = hFov * height / width;
			return new Image(data, width, height, hFov, vFov);
		}

		/**
		 * x軸の加速度を返します.
		 *
		 * @return x軸の加速度(m/s^2)
		 */
		public float getAccelX() {
			return Controller.accelerometer_get_values(accelerometer)[0];
		}

		public float getAccelY() {
			return Controller.accelerometer_get_values(accelerometer)[1];
		}

		public float getAccelZ() {
			return Controller.accelerometer_get_values(accelerometer)[2];
		}

		public int getForce(PressureSensor ts) {
			return Controller.touch_sensor_get_value(fsr.get(ts));
		}

		public float getForce(Joint joint) {
			return jointForces[joint.ordinal()];
		}

		/**
		 * Gpsセンサ値を取得（調整用、本戦では使わないように）
		 *
		 * @return 現在位置のx座標
		 */
		public float getGpsX() {
			float[] gps_matrix = Controller.gps_get_matrix(gps);
			return Controller.gps_position_x(gps_matrix);
		}

		/**
		 * Gpsセンサ値を取得（調整用、本戦では使わないように）
		 *
		 * @return 現在位置のy座標
		 */
		public float getGpsY() {
			float[] gps_matrix = Controller.gps_get_matrix(gps);
			return Controller.gps_position_y(gps_matrix);
		}

		/**
		 * Gpsセンサ値を取得（調整用、本戦では使わないように）
		 *
		 * @return 現在位置のz座標
		 */
		public float getGpsZ() {
			float[] gps_matrix = Controller.gps_get_matrix(gps);
			return Controller.gps_position_z(gps_matrix);
		}

		public float getGpsHeading() {
			float[] gps_matrix = Controller.gps_get_matrix(gps);
			float[] eulers = Controller.gps_euler(gps_matrix);
			return eulers[1];
		}
	}

	protected class WebotsEffector implements Effector {
		public void setJoint(Joint joint, float valueInRad) {
			assert joints.containsKey(joint);
			if (power)
				Controller.servo_set_position(joints.get(joint), valueInRad);
		}

		public void setJointDegree(Joint joint, float valueInDeg) {
			setJoint(joint, (float) (valueInDeg * Math.PI / 180.0));
		}

		public void setJointMicro(Joint joint, int valueInMicroRad) {
			setJoint(joint, valueInMicroRad / 1000000.0F);
		}

		public void setForce(Joint joint, float value) {
			if (power) {
				Controller.servo_set_force(joints.get(joint), value);
				jointForces[joint.ordinal()] = value;
			}
		}

		public void setPower(boolean sw) {
			power = sw;
		}

		public void before() {
		}

		public void after() {
		}
	}
}
