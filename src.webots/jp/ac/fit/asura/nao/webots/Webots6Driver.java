/*
 * 作成日: 2008/12/30
 */
package jp.ac.fit.asura.nao.webots;

import java.util.EnumMap;

import javax.vecmath.Matrix3f;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.PressureSensor;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.misc.MathUtils;

import com.cyberbotics.webots.controller.Accelerometer;
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
class Webots6Driver {
	private WebotsSensor sensor;
	private WebotsEffector effector;

	private EnumMap<Joint, Servo> joints;
	private EnumMap<PressureSensor, TouchSensor> fsr;
	private DistanceSensor[] usSensors;
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
			device.enableMotorForceFeedback(Webots6Player.SIMULATION_STEP);
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

		accelerometer = robot.getAccelerometer("accelerometer");
		accelerometer.enable(Webots6Player.SIMULATION_STEP);

		gyro = robot.getGyro("gyro");
		gyro.enable(Webots6Player.SIMULATION_STEP);

		usSensors = new DistanceSensor[4];
		usSensors[0] = robot.getDistanceSensor("US/TopRight");
		usSensors[0].enable(Webots6Player.SIMULATION_STEP);
		usSensors[1] = robot.getDistanceSensor("US/BottomRight");
		usSensors[1].enable(Webots6Player.SIMULATION_STEP);
		usSensors[2] = robot.getDistanceSensor("US/TopLeft");
		usSensors[2].enable(Webots6Player.SIMULATION_STEP);
		usSensors[3] = robot.getDistanceSensor("US/BottomLeft");
		usSensors[3].enable(Webots6Player.SIMULATION_STEP);

		// GPSセンサ
		gps = robot.getGPS("gps");
		gps.enable(Webots6Player.SIMULATION_STEP);

		sensor = new WebotsSensor();
		effector = new WebotsEffector();

		power = true;
	}

	Sensor getSensor() {
		return sensor;
	}

	Effector getEffector() {
		return effector;
	}

	private class WebotsSensor implements Sensor {
		public void init() {
		}

		public void before() {
			for (Joint joint : Joint.values()) {
				jointValues[joint.ordinal()] = (float) joints.get(joint)
						.getPosition();
				jointForces[joint.ordinal()] = (float) joints.get(joint)
						.getMotorForceFeedback();
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
			return MathUtils.toDegrees(getJoint(joint));
		}

		/**
		 * x軸の加速度を返します.
		 *
		 * @return x軸の加速度(m/s^2)
		 */
		public float getAccelX() {
			return (float) ((accelerometer.getValues())[1]);
		}

		public float getAccelY() {
			return (float) ((accelerometer.getValues())[2]);
		}

		public float getAccelZ() {
			return (float) ((accelerometer.getValues())[0]);
		}

		public float getGyroX() {
			return (float) ((gyro.getValues())[1]);
		}

		public float getGyroZ() {
			return (float) ((gyro.getValues())[0]);
		}

		public float getForce(PressureSensor ts) {
			return (float) fsr.get(ts).getValue();
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

	private class WebotsEffector implements Effector {
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

		public void init() {
		}

		public void before() {
		}

		public void after() {
		}
	}
}
