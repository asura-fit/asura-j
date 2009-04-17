/*
 * 作成日: 2009/03/20
 */
package jp.ac.fit.asura.nao.naoji;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Matrix3f;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.PressureSensor;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.Switch;
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

	private float[] sAngles;
	private float[] eHeadAngles;
	private float[] eBodyAngles;
	private float[] accels;
	private float[] gyros;
	private float[] inertialAngles;
	private float[] forces;
	private float[] cofPositions;
	private float[] witches;

	public NaojiDriver(NaojiContext context) {
		JALBroker broker = context.getParentBroker();
		memory = broker.createJALMemory();
		motion = broker.createJALMotion();
		dcm = broker.createJDCM();
		String[] names = motion.getBodyJointNames();
		Joint[] joints = Joint.values();
		sAngles = new float[joints.length];
		eHeadAngles = new float[2];
		eBodyAngles = new float[joints.length - 2];
		accels = new float[3];
		gyros = new float[2];
		inertialAngles = new float[2];
		forces = new float[8];
		cofPositions = new float[4];
		witches = new float[5];

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
	}

	public class NaojiSensor implements Sensor {
		FloatQuery floatQuery;

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

		public void after() {
			log.trace("after NaojiSensor.");
		}

		public void before() {
			log.trace("before NaojiSensor.");
			floatQuery.update();
			FloatBuffer fb = floatQuery.getBuffer();
			fb.get(accels);
			fb.get(gyros);
			fb.get(inertialAngles);
			fb.get(forces);
			fb.get(cofPositions);
			fb.get(witches);
			fb.get(sAngles);
			assert !fb.hasRemaining() : fb;
			fb.position(0);

			if (log.isTraceEnabled()) {
				log.trace("Accels data:" + Arrays.toString(accels));
				log.trace("Gyros data:" + Arrays.toString(gyros));
				log.trace("InertialAngles data:"
						+ Arrays.toString(inertialAngles));
				log.trace("Forces data:" + Arrays.toString(forces));
				log.trace("CofPositions data:" + Arrays.toString(cofPositions));
				log.trace("Switches data:" + Arrays.toString(witches));
				log.trace("Joints data:" + Arrays.toString(sAngles));
			}
		}

		// TODO マッピングがおかしい.
		public float getAccelX() {
			return accels[1];
		}

		public float getAccelY() {
			return -accels[2];
		}

		public float getAccelZ() {
			return accels[0];
		}

		public float getForce(Joint joint) {
			return 0;
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

		public float getForce(PressureSensor ts) {
			return fsrFilter(forces[ts.ordinal()]);
		}

		public void getGpsRotation(Matrix3f rotationMatrix) {
		}

		public float getGpsX() {
			return 0;
		};

		public float getGpsY() {
			return 0;
		}

		public float getGpsZ() {
			return 0;
		}

		public float getGyroX() {
			return gyros[0];
		}

		public float getGyroZ() {
			return gyros[1];
		}

		public float[] getJointAngles() {
			return sAngles;
		}

		public float getJoint(Joint joint) {
			return sAngles[joint.ordinal()];
		}

		public float getJointDegree(Joint joint) {
			return MathUtils.toDegrees(getJoint(joint));
		}

		@Override
		public boolean getSwitch(Switch sw) {
			// according to the RedBook DCM, value = 0.0 if button is pressed.
			return witches[sw.ordinal()] == 0;
		}
	}

	public class NaojiEffector implements Effector {
		@Override
		public void init() {
			log.trace("init NaojiEffector.");
		}

		@Override
		public void after() {
			log.trace("after NaojiEffector.");

			if (motion.walkIsActive()) {
				int id1 = motion.gotoAngle(Joint.HeadPitch.ordinal(),
						eHeadAngles[Joint.HeadPitch.ordinal()], 0.0625f,
						InterpolationType.INTERPOLATION_SMOOTH.getId());
				int id2 = motion.gotoAngle(Joint.HeadYaw.ordinal(),
						eHeadAngles[Joint.HeadYaw.ordinal()], 0.0625f,
						InterpolationType.INTERPOLATION_SMOOTH.getId());
				motion.wait(id1, 30);
				motion.wait(id2, 30);
			} else {
				dcm.setTimeSeparate(headAliasId, MergeType.ClearAfter,
						eHeadAngles, new int[] { 60 });
				dcm.setTimeSeparate(bodyAliasId, MergeType.ClearAfter,
						eBodyAngles, new int[] { 60 });
			}
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
			switch (joint) {
			case HeadYaw:
			case HeadPitch:
				eHeadAngles[joint.ordinal()] = valueInRad;
				return;
			default:
				eBodyAngles[joint.ordinal() - 2] = valueInRad;
			}
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
		}

		@Override
		public void setJoint(Joint joint, float[] angleValues,
				int[] durationInMills) {
			dcm.set(joint.name() + "/Position/Actuator/Value",
					MergeType.ClearAfter, angleValues, durationInMills);
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
