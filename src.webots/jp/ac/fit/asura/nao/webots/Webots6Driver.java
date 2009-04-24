/*
 * 作成日: 2008/12/30
 */
package jp.ac.fit.asura.nao.webots;

import java.util.EnumMap;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.PressureSensor;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.SensorContext;

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

	private EnumMap<Joint, Boolean> power;

	private Robot robot;

	private TimeBarier motionBarier;
	private TimeBarier visualBarier;

	private long time;

	/**
		 *
		 */
	public Webots6Driver(Robot robot, TimeBarier motionBarier,
			TimeBarier visualBarier) {
		this.robot = robot;
		this.motionBarier = motionBarier;
		this.visualBarier = visualBarier;

		joints = new EnumMap<Joint, Servo>(Joint.class);
		for (Joint joint : Joint.values()) {
			Servo device = robot.getServo(joint.toString());
			device.enablePosition(Webots6Player.SIMULATION_STEP);
			device.enableMotorForceFeedback(Webots6Player.SIMULATION_STEP);
			joints.put(joint, device);
		}

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

		power = new EnumMap<Joint, Boolean>(Joint.class);
		for (Joint j : Joint.values())
			power.put(j, Boolean.TRUE);
	}

	Sensor getSensor() {
		return sensor;
	}

	Effector getEffector() {
		return effector;
	}

	private class WebotsSensor implements Sensor {
		@Override
		public SensorContext create() {
			return new WebotsSensorContext();
		}

		@Override
		public void update(SensorContext sensorContext) {
			WebotsSensorContext sc = (WebotsSensorContext) sensorContext;
			for (Joint joint : Joint.values()) {
				sc.jointValues[joint.ordinal()] = (float) joints.get(joint)
						.getPosition();
				sc.jointForces[joint.ordinal()] = (float) joints.get(joint)
						.getMotorForceFeedback();
			}

			sc.accels[0] = (float) ((accelerometer.getValues())[1]);

			sc.accels[1] = (float) ((accelerometer.getValues())[2]);

			sc.accels[2] = (float) ((accelerometer.getValues())[0]);

			sc.gyros[0] = (float) ((gyro.getValues())[1]);

			sc.gyros[1] = (float) ((gyro.getValues())[0]);

			for (PressureSensor ps : PressureSensor.values())
				sc.forces[ps.ordinal()] = (float) fsr.get(ps).getValue();

			sc.gps[0] = (float) ((gps.getValues())[0]);
			sc.gps[1] = (float) ((gps.getValues())[1]);
			sc.gps[2] = (float) ((gps.getValues())[2]);
			sc.time = time;

			visualBarier.notifyTime(time);
		}

		@Override
		public void poll() {
		}

		@Override
		public void init() {
		}

		@Override
		public void before() {
		}

		@Override
		public void after() {
		}
	}

	private class WebotsEffector implements Effector {
		private float[] eAngles;

		private boolean hasTimedCommand;;
		private float[] angleMatrix;
		private int[] durationInMills;
		private int commandTime;
		private int commandIndex;

		public WebotsEffector() {
			eAngles = new float[Joint.values().length];
		}

		@Override
		public void setJoint(Joint joint, float valueInRad) {
			assert joints.containsKey(joint);
			eAngles[joint.ordinal()] = valueInRad;
		}

		@Override
		public void setJointDegree(Joint joint, float valueInDeg) {
			setJoint(joint, (float) (valueInDeg * Math.PI / 180.0));
		}

		@Override
		public void setJointMicro(Joint joint, int valueInMicroRad) {
			setJoint(joint, valueInMicroRad / 1000000.0F);
		}

		@Override
		public void setJoint(Joint joint, float[] angleValues,
				int[] durationInMills) {
		}

		@Override
		public void setBodyJoints(float[] angleMatrix, int[] durationInMills) {
			if (hasTimedCommand)
				return;
			this.angleMatrix = angleMatrix;
			this.durationInMills = durationInMills;
			hasTimedCommand = true;
			commandTime = 0;
			commandIndex = 0;
		}

		public void setForce(Joint joint, float value) {
			if (power.get(joint).booleanValue()) {
				joints.get(joint).setForce(value);
				// jointForces[joint.ordinal()] = value;
			}
		}

		@Override
		public void setPower(float power) {
			for (Joint j : Joint.values())
				Webots6Driver.this.power.put(j, power != 0);
		}

		@Override
		public void setPower(Joint joint, float power) {
		}

		@Override
		public void setLed(String ledName, float luminance) {
		}

		@Override
		public void init() {
		}

		@Override
		public void before() {
			robot.step(Webots6Player.SIMULATION_STEP);
			time += Webots6Player.SIMULATION_STEP;
		}

		@Override
		public void after() {
			motionBarier.waitTime(time);
			if (hasTimedCommand) {
				do {
					if (commandTime > durationInMills[commandIndex + 1]) {
						commandIndex++;
					}
					if (commandIndex + 1 == durationInMills.length) {
						hasTimedCommand = false;
						return;
					}
				} while (commandTime > durationInMills[commandIndex + 1]);
				commandTime += Webots6Player.SIMULATION_STEP;

				for (Joint joint : Joint.values()) {
					if (!power.get(joint).booleanValue())
						continue;
					if (joint == Joint.HeadYaw || joint == Joint.HeadPitch) {
						joints.get(joint).setPosition(eAngles[joint.ordinal()]);
						continue;
					}

					int jointNum = Joint.values().length - 2;
					float prev = angleMatrix[jointNum * (commandIndex)
							+ joint.ordinal() - 2];
					float next = angleMatrix[jointNum * (commandIndex + 1)
							+ joint.ordinal() - 2];
					int duration = durationInMills[commandIndex + 1]
							- durationInMills[commandIndex];
					int curTime = commandTime - durationInMills[commandIndex];
					float cur = prev + (next - prev)
							* ((float) curTime / duration);
					joints.get(joint).setPosition(cur);
				}
				return;
			}

			for (Joint joint : Joint.values())
				if (power.get(joint).booleanValue())
					joints.get(joint).setPosition(eAngles[joint.ordinal()]);
		}
	}
}
