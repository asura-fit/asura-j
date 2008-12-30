/*
 * 作成日: 2008/12/30
 */
package jp.ac.fit.asura.nao.webots;

import java.util.EnumMap;

import javax.vecmath.Matrix3f;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.Image;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.PressureSensor;
import jp.ac.fit.asura.nao.Sensor;

import com.cyberbotics.webots.controller.Accelerometer;
import com.cyberbotics.webots.controller.Camera;
import com.cyberbotics.webots.controller.DistanceSensor;
import com.cyberbotics.webots.controller.GPS;
import com.cyberbotics.webots.controller.Gyro;
import com.cyberbotics.webots.controller.Robot;
import com.cyberbotics.webots.controller.Servo;
import com.cyberbotics.webots.controller.TouchSensor;

/**
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public class Webots6Driver {
	private EnumMap<Joint, Servo> joints;
	private EnumMap<PressureSensor, TouchSensor> fsr;
	private Camera camera;
	private DistanceSensor left_ultrasound_sensor;
	private DistanceSensor right_ultrasound_sensor;
	private Accelerometer accelerometer;
	private Gyro gyro;

	private GPS gps;

	private float jointValues[];

	private float jointForces[];

	private boolean power;

	/**
		 *
		 */
	public Webots6Driver(Robot robot) {
		joints = new EnumMap<Joint, Servo>(Joint.class);
		for (Joint joint : Joint.values()) {
			Servo device = robot.getServo(joint.toString());
			device.enablePosition(Webots6Player.SIMULATION_STEP);
			joints.put(joint, device);
		}
		jointValues = new float[Joint.values().length];
		jointForces = new float[Joint.values().length];

		fsr = new EnumMap<PressureSensor, TouchSensor>(PressureSensor.class);
		for (PressureSensor ts : PressureSensor.values()) {
			TouchSensor device = robot.getTouchSensor(ts.getDeviceTag());
			device.enable(Webots6Player.SIMULATION_STEP);
			fsr.put(ts, device);
		}

		camera = robot.getCamera("camera");
		camera.enable(Webots6Player.SIMULATION_STEP);

		accelerometer = robot.getAccelerometer("accelerometer");
		accelerometer.enable(Webots6Player.SIMULATION_STEP);

		gyro = robot.getGyro("gyro");
		gyro.enable(Webots6Player.SIMULATION_STEP);

		left_ultrasound_sensor = robot
				.getDistanceSensor("left ultrasound sensor");
		left_ultrasound_sensor.enable(Webots6Player.SIMULATION_STEP);
		right_ultrasound_sensor = robot
				.getDistanceSensor("right ultrasound sensor");
		right_ultrasound_sensor.enable(Webots6Player.SIMULATION_STEP);

		// GPSセンサ
		gps = robot.getGPS("gps");
		gps.enable(Webots6Player.SIMULATION_STEP);

		power = true;
	}

	protected class WebotsSensor implements Sensor {
		public void before() {
			for (Joint joint : Joint.values()) {
				jointValues[joint.ordinal()] = (float) joints.get(joint)
						.getPosition();
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
			int[] data = camera.getImage();
			int width = camera.getWidth();
			int height = camera.getHeight();
			float hFov = (float) camera.getFov();
			float vFov = hFov * height / width;
			return new Image(data, width, height, hFov, vFov);
		}

		/**
		 * x軸の加速度を返します.
		 * 
		 * @return x軸の加速度(m/s^2)
		 */
		public float getAccelX() {
			return (float) ((accelerometer.getValues())[0]);
		}

		public float getAccelY() {
			return (float) ((accelerometer.getValues())[1]);
		}

		public float getAccelZ() {
			return (float) ((accelerometer.getValues())[2]);
		}

		public float getGyroX() {
			return (float) ((gyro.getValues())[0]);
		}

		public float getGyroY() {
			return (float) ((gyro.getValues())[1]);
		}

		public int getForce(PressureSensor ts) {
			return (int) fsr.get(ts).getValue();
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
			return (float) ((gps.getValues())[0]);
		}

		/**
		 * Gpsセンサ値を取得（調整用、本戦では使わないように）
		 * 
		 * @return 現在位置のy座標
		 */
		public float getGpsY() {
			return (float) ((gps.getValues())[1]);
		}

		/**
		 * Gpsセンサ値を取得（調整用、本戦では使わないように）
		 * 
		 * @return 現在位置のz座標
		 */
		public float getGpsZ() {
			return (float) ((gps.getValues())[2]);
		}

		public void getGpsRotation(Matrix3f mat) {
			// disabled in webots6
			mat.setIdentity();
		}
	}

	protected class WebotsEffector implements Effector {
		public void setJoint(Joint joint, float valueInRad) {
			assert joints.containsKey(joint);
			if (power)
				joints.get(joint).setPosition(valueInRad);
		}

		public void setJointDegree(Joint joint, float valueInDeg) {
			setJoint(joint, (float) (valueInDeg * Math.PI / 180.0));
		}

		public void setJointMicro(Joint joint, int valueInMicroRad) {
			setJoint(joint, valueInMicroRad / 1000000.0F);
		}

		public void setForce(Joint joint, float value) {
			if (power) {
				joints.get(joint).setForce(value);
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
