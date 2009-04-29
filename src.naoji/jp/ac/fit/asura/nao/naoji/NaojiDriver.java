/*
 * 作成日: 2009/03/20
 */
package jp.ac.fit.asura.nao.naoji;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.SensorContext;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.naoji.NaojiContext;
import jp.ac.fit.asura.naoji.jal.JALBroker;
import jp.ac.fit.asura.naoji.jal.JALMemory;
import jp.ac.fit.asura.naoji.jal.JALMotion;
import jp.ac.fit.asura.naoji.jal.JDCM;
import jp.ac.fit.asura.naoji.jal.JALMemory.FloatQuery;
import jp.ac.fit.asura.naoji.jal.JDCM.MergeType;
import jp.ac.fit.asura.naoji.robots.NaoV3R.InterpolationType;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 *
 * @version $Id: $
 *
 */
public class NaojiDriver {
	private static final Logger log = Logger.getLogger(NaojiDriver.class);
	private NaojiContext context;
	protected JALMemory memory;
	protected JALMotion motion;
	protected JDCM dcm;
	private int bodyAliasId;
	private int headAliasId;

	private boolean eHasHeadCommand;
	private float[] eHeadAngles;
	private int[] eHeadDurations;

	private float[] eBodyAngles;
	private int[] eBodyDurations;

	public NaojiDriver(NaojiContext context) {
		this.context = context;
		JALBroker broker = context.getParentBroker();
		memory = broker.createJALMemory();
		motion = broker.createJALMotion();
		dcm = broker.createJDCM();
		String[] names = motion.getBodyJointNames();
		Joint[] joints = Joint.values();
		eHeadAngles = new float[2];
		eHeadDurations = new int[2];
		eBodyAngles = new float[joints.length - 2];
		eBodyDurations = new int[joints.length - 2];

		List<String> bodyJoints = new ArrayList<String>();

		log.debug("Joint names:" + Arrays.toString(names));
		for (int i = 0; i < joints.length; i++) {
			Joint j = joints[i];
			if (j != Joint.HeadPitch && j != Joint.HeadYaw)
				bodyJoints.add(j.name() + "/Position/Actuator/Value");

			assert j != null;
			assert j.name().equals(names[i]) : "Invalid order of joint:" + j;
			;
		}

		bodyAliasId = dcm.createAlias(bodyJoints.toArray(new String[0]));
		headAliasId = dcm.createAlias(new String[] {
				"HeadYaw/Position/Actuator/Value",
				"HeadPitch/Position/Actuator/Value" });
		// timeKey = memory.defineKey("DCM/Time");
	}

	public class NaojiSensor implements Sensor {
		FloatQuery floatQuery;

		@Override
		public void init() {
			log.info("init NaojiSensor.");
			List<String> fKeys = new ArrayList<String>();
			fKeys.add("Device/SubDeviceList/InertialSensor/AccX/Sensor/Value");
			fKeys.add("Device/SubDeviceList/InertialSensor/AccY/Sensor/Value");
			fKeys.add("Device/SubDeviceList/InertialSensor/AccZ/Sensor/Value");
			fKeys.add("Device/SubDeviceList/InertialSensor/GyrX/Sensor/Value");
			fKeys.add("Device/SubDeviceList/InertialSensor/GyrY/Sensor/Value");
			fKeys
					.add("Device/SubDeviceList/InertialSensor/AngleX/Sensor/Value");
			fKeys
					.add("Device/SubDeviceList/InertialSensor/AngleY/Sensor/Value");
			// FSRもfloatらしい.
			fKeys.add("Device/SubDeviceList/LFoot/FSR/FrontLeft/Sensor/Value");
			fKeys.add("Device/SubDeviceList/LFoot/FSR/FrontRight/Sensor/Value");
			fKeys.add("Device/SubDeviceList/LFoot/FSR/RearLeft/Sensor/Value");
			fKeys.add("Device/SubDeviceList/LFoot/FSR/RearRight/Sensor/Value");
			fKeys.add("Device/SubDeviceList/RFoot/FSR/FrontLeft/Sensor/Value");
			fKeys.add("Device/SubDeviceList/RFoot/FSR/FrontRight/Sensor/Value");
			fKeys.add("Device/SubDeviceList/RFoot/FSR/RearLeft/Sensor/Value");
			fKeys.add("Device/SubDeviceList/RFoot/FSR/RearRight/Sensor/Value");

			fKeys.add("Device/SubDeviceList/LFoot/CenterOfForceX/Sensor/Value");
			fKeys.add("Device/SubDeviceList/LFoot/CenterOfForceY/Sensor/Value");
			fKeys.add("Device/SubDeviceList/RFoot/CenterOfForceX/Sensor/Value");
			fKeys.add("Device/SubDeviceList/RFoot/CenterOfForceY/Sensor/Value");

			fKeys.add("Device/SubDeviceList/ChestBoard/Button/Sensor/Value");
			fKeys.add("Device/SubDeviceList/LFoot/Bumber/Left/Sensor/Value");
			fKeys.add("Device/SubDeviceList/LFoot/Bumber/Right/Sensor/Value");
			fKeys.add("Device/SubDeviceList/RFoot/Bumber/Left/Sensor/Value");
			fKeys.add("Device/SubDeviceList/RFoot/Bumber/Right/Sensor/Value");

			for (Joint j : Joint.values()) {
				fKeys.add("Device/SubDeviceList/" + j.name()
						+ "/Position/Sensor/Value");
			}

			floatQuery = memory.createFloatQuery(fKeys);
		}

		@Override
		public SensorContext create() {
			return new NaojiSensorContext();
		}

		@Override
		public void update(SensorContext sensorContext) {
			NaojiSensorContext context = (NaojiSensorContext) sensorContext;
			floatQuery.update();
			FloatBuffer fb = floatQuery.getBuffer();
			fb.get(context.accels);
			fb.get(context.gyros);
			fb.get(context.inertialAngles);
			fb.get(context.forces);
			fb.get(context.cofPositions);
			fb.get(context.witches);
			fb.get(context.angles);
			assert !fb.hasRemaining() : fb;
			fb.position(0);

			if (log.isTraceEnabled()) {
				log.trace("Accels data:" + Arrays.toString(context.accels));
				log.trace("Gyros data:" + Arrays.toString(context.gyros));
				log.trace("InertialAngles data:"
						+ Arrays.toString(context.inertialAngles));
				log.trace("Forces data:" + Arrays.toString(context.forces));
				log.trace("CofPositions data:"
						+ Arrays.toString(context.cofPositions));
				log.trace("Switches data:" + Arrays.toString(context.witches));
				log.trace("Joints data:" + Arrays.toString(context.angles));
			}

			for (int i = 0; i < context.forces.length; i++)
				context.forces[i] = fsrFilter(context.forces[i]);
			context.time = System.currentTimeMillis();
		}

		@Override
		public void poll() {
			memory.waitNextCycle();
		}

		@Override
		public boolean isSupported(Function func) {
			switch (func) {
			case ACCEL:
			case GYRO:
			case FORCE:
			case INERTIAL:
			case JOINT_ANGLE:
			case SWITCH:
				return true;
			default:
				return false;
			}
		}

		@Override
		public void after() {
			log.trace("after NaojiSensor.");
		}

		@Override
		public void before() {
			log.trace("before NaojiSensor.");
		}

		/**
		 * FSRの値を圧力値(ニュートン)に変換する.
		 *
		 * 手抜き実装.
		 *
		 * @param a
		 * @return
		 */
		private float fsrFilter(float a) {
			if (a <= 0.0) {
				log.error("Invalid FSR value:" + a);
				return 0;
			}
			float r = 1 / a - 8.3682e-05f;
			if (r < 0.0) {
				if (r < -1e-3f)
					log.error("Invalid FSR value:" + r);
				return 0;
			}
			return r * (2510270.415f / 1000.0f);
		}
	}

	public class NaojiEffector implements Effector {
		private boolean hasTimedCommand;
		private long commandTime;

		@Override
		public void init() {
			log.trace("init NaojiEffector.");
		}

		@Override
		public void after() {
			log.trace("after NaojiEffector.");

			if (motion.walkIsActive()) {
				if (eHasHeadCommand)
					doHeadCommandALMotion();
			} else if (hasTimedCommand) {
				if (commandTime - System.currentTimeMillis() < 0)
					hasTimedCommand = false;
				if (eHasHeadCommand)
					doHeadCommandDCM();
			} else {
				if (eHasHeadCommand)
					doHeadCommandDCM();
				dcm.setTimeSeparate(bodyAliasId, MergeType.ClearAfter,
						eBodyAngles, new int[] { 200 });
			}
			eHasHeadCommand = false;
		}

		private void doHeadCommandDCM() {
			// FIXME setTimeMixedにして個別に実行時間を指定する.
			int duration = eHeadDurations[Joint.HeadPitch.ordinal()];
			dcm.setTimeSeparate(headAliasId, MergeType.ClearAfter, eHeadAngles,
					new int[] { duration });
		}

		private void doHeadCommandALMotion() {
			float ms1 = eHeadDurations[Joint.HeadPitch.ordinal()] / 1e3f;
			int id1 = motion.gotoAngle(Joint.HeadPitch.ordinal(),
					eHeadAngles[Joint.HeadPitch.ordinal()], ms1,
					InterpolationType.LINEAR.getId());

			float ms2 = eHeadDurations[Joint.HeadYaw.ordinal()] / 1e3f;
			int id2 = motion.gotoAngle(Joint.HeadYaw.ordinal(),
					eHeadAngles[Joint.HeadYaw.ordinal()], ms2,
					InterpolationType.LINEAR.getId());
			motion.wait(id1, 30);
			motion.wait(id2, 30);
		}

		@Override
		public void before() {
			log.trace("before NaojiEffector.");
		}

		@Override
		public void setForce(Joint joint, float valueTorque) {
			// Not implemented.
		}

		@Override
		public void setJoint(Joint joint, float valueInRad) {
			setJoint(joint, valueInRad, 250);
		}

		@Override
		public void setJoint(Joint joint, float valueInRad, int durationInMills) {
			if (log.isTraceEnabled())
				log.trace("setJoint " + joint + " to "
						+ MathUtils.toDegrees(valueInRad) + "[deg] in "
						+ durationInMills + "[ms]");
			switch (joint) {
			case HeadYaw:
			case HeadPitch:
				eHeadAngles[joint.ordinal()] = valueInRad;
				eHeadDurations[joint.ordinal()] = durationInMills;
				eHasHeadCommand = true;
				return;
			default:
				eBodyAngles[joint.ordinal() - 2] = valueInRad;
				eBodyDurations[joint.ordinal() - 2] = durationInMills;
				return;
			}
		}

		@Override
		public void setJoint(Joint joint, float[] angleValues,
				int[] durationInMills) {
			dcm.set(joint.name() + "/Position/Actuator/Value",
					MergeType.ClearAfter, angleValues, durationInMills);
		}

		@Override
		public void setJointDegree(Joint joint, float valueInDeg) {
			setJoint(joint, (float) Math.toRadians(valueInDeg));
		}

		@Override
		public void setJointMicro(Joint joint, int valueInMicroRad) {
			setJoint(joint, valueInMicroRad / 1e6f);
		}

		@Override
		public void setBodyJoints(float[] angleMatrix, int[] durationInMills) {
			dcm.setTimeSeparate(bodyAliasId, MergeType.ClearAfter, angleMatrix,
					durationInMills);
			hasTimedCommand = true;
			commandTime = durationInMills[durationInMills.length - 1];
		}

		@Override
		public void setLed(String ledName, float luminance) {
			assert luminance >= 0 && luminance <= 1 : luminance;
			dcm.set(ledName + "/Actuator/Value", MergeType.ClearAfter,
					new float[] { luminance }, new int[] { 0 });
		}

		@Override
		public void setPower(float power) {
			assert power >= 0 && power <= 1 : power;
			// Set stiffness
			int taskId = motion.gotoBodyStiffness(power, 0.5f,
					InterpolationType.LINEAR.getId());
			motion.wait(taskId, 0);
		}

		@Override
		public void setPower(Joint joint, float power) {
			assert power >= 0 && power <= 1 : power;
			int taskId = motion.gotoJointStiffness(joint.ordinal(), power,
					0.125f, InterpolationType.LINEAR.getId());
			motion.wait(taskId, 0);
		}
	}
}
