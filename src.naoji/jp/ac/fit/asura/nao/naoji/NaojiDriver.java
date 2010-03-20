/*
 * 作成日: 2009/03/20
 */
package jp.ac.fit.asura.nao.naoji;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.SensorContext;
import jp.ac.fit.asura.nao.Switch;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.misc.AverageFilter;
import jp.ac.fit.asura.nao.misc.Filter.BooleanFilter;
import jp.ac.fit.asura.naoji.NaojiContext;
import jp.ac.fit.asura.naoji.jal.JALBroker;
import jp.ac.fit.asura.naoji.jal.JALMemory;
import jp.ac.fit.asura.naoji.jal.JALMotion;
import jp.ac.fit.asura.naoji.jal.JALTextToSpeech;
import jp.ac.fit.asura.naoji.jal.JDCM;
import jp.ac.fit.asura.naoji.jal.JALMemory.FloatQuery;
import jp.ac.fit.asura.naoji.jal.JDCM.MergeType;
import jp.ac.fit.asura.naoji.robots.NaoV3R;
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
	protected JALMemory memory;
	protected JALMotion motion;
	protected JDCM dcm;
	protected JALTextToSpeech tts;
	private int bodyAliasId;
	private int headAliasId;

	private boolean eHasHeadCommand;
	private float[] eHeadAngles;
	private int[] eHeadDurations;

	private boolean eHasBodyCommand;
	private float[] eBodyAngles;
	private int[] eBodyDurations;

	public NaojiDriver(NaojiContext context) {
		JALBroker broker = context.getParentBroker();
		memory = broker.createJALMemory();
		motion = broker.createJALMotion();
		dcm = broker.createJDCM();
		tts = broker.createJALTextToSpeech();
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
		}

		bodyAliasId = dcm.createAlias(bodyJoints.toArray(new String[0]));
		headAliasId = dcm.createAlias(new String[] {
				"HeadYaw/Position/Actuator/Value",
				"HeadPitch/Position/Actuator/Value" });
		// timeKey = memory.defineKey("DCM/Time");
	}

	public class NaojiSensor implements Sensor {
		FloatQuery floatQuery;
		BooleanFilter resetFilterR;
		BooleanFilter resetFilterL;
		BooleanFilter resetFilterC;

		public NaojiSensor() {
			resetFilterR = new AverageFilter.Boolean(200);
			resetFilterL = new AverageFilter.Boolean(200);
			resetFilterC = new AverageFilter.Boolean(200);
		}

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
			fKeys.add("Device/SubDeviceList/LFoot/Bumper/Left/Sensor/Value");
			fKeys.add("Device/SubDeviceList/LFoot/Bumper/Right/Sensor/Value");
			fKeys.add("Device/SubDeviceList/RFoot/Bumper/Left/Sensor/Value");
			fKeys.add("Device/SubDeviceList/RFoot/Bumper/Right/Sensor/Value");

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

			context.time = System.currentTimeMillis();

			boolean leftPressed = context.getSwitch(Switch.LFootLeft)
					&& context.getSwitch(Switch.LFootRight);
			boolean rightPressed = context.getSwitch(Switch.RFootLeft)
					&& context.getSwitch(Switch.RFootRight);
			boolean chestPressed = context.getSwitch(Switch.Chest);
			boolean doReset = (resetFilterL.eval(leftPressed) || resetFilterR
					.eval(rightPressed))
					&& resetFilterC.eval(chestPressed);
			if (doReset && resetFilterL.isFilled() && resetFilterR.isFilled()
					&& resetFilterC.isFilled()) {
				log.warn("Manual reset called. restart naoqi.");
				try {
					Runtime.getRuntime().exec("/etc/init.d/naoqi restart");
					// Runtime.getRuntime().exec("sh /etc/init.d/naoqi restart");
				} catch (IOException e) {
					log.error("", e);
				}
				resetFilterL.clear();
				resetFilterR.clear();
				resetFilterC.clear();
			}
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
	}

	public class NaojiEffector implements Effector {
		private int headYawTaskId;
		private int headPitchTaskId;

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
			} else {
				if (eHasHeadCommand)
					doHeadCommandDCM();
				if (eHasBodyCommand) {
					motion.setJointStiffness(NaoV3R.Joint.RHipPitch.getId(),
							1.0f);
					motion.setJointStiffness(NaoV3R.Joint.LHipPitch.getId(),
							1.0f);
					motion.setJointStiffness(NaoV3R.Joint.RHipYawPitch.getId(),
							1.0f);
					motion.setJointStiffness(NaoV3R.Joint.LHipYawPitch.getId(),
							1.0f);
					motion.setJointStiffness(NaoV3R.Joint.RHipRoll.getId(),
							1.0f);
					motion.setJointStiffness(NaoV3R.Joint.LHipRoll.getId(),
							1.0f);
					motion.setJointStiffness(NaoV3R.Joint.RAnkleRoll.getId(),
							1.0f);
					motion.setJointStiffness(NaoV3R.Joint.LAnkleRoll.getId(),
							1.0f);
					motion.setJointStiffness(NaoV3R.Joint.RKneePitch.getId(),
							1.0f);
					motion.setJointStiffness(NaoV3R.Joint.LKneePitch.getId(),
							1.0f);
					motion.setJointStiffness(NaoV3R.Joint.RAnklePitch.getId(),
							1.0f);
					motion.setJointStiffness(NaoV3R.Joint.LAnklePitch.getId(),
							1.0f);
					// FIXME setTimeMixedにして個別に実行時間を指定する.
					dcm.setTimeSeparate(bodyAliasId, MergeType.ClearAll,
							eBodyAngles, new int[] { 200 });
				}
			}
			eHasHeadCommand = false;
			eHasBodyCommand = false;
		}

		private void doHeadCommandDCM() {
			// FIXME setTimeMixedにして個別に実行時間を指定する.
			int duration = eHeadDurations[Joint.HeadPitch.ordinal()];
			dcm.setTimeSeparate(headAliasId, MergeType.ClearAll, eHeadAngles,
					new int[] { duration });
		}

		private void doHeadCommandALMotion() {
			float ms1 = eHeadDurations[Joint.HeadPitch.ordinal()] / 1e3f;
			motion.stop(headYawTaskId);
			motion.stop(headPitchTaskId);
			headPitchTaskId = motion.gotoAngle(Joint.HeadPitch.ordinal(),
					eHeadAngles[Joint.HeadPitch.ordinal()], ms1,
					InterpolationType.LINEAR.getId());

			float ms2 = eHeadDurations[Joint.HeadYaw.ordinal()] / 1e3f;
			headYawTaskId = motion.gotoAngle(Joint.HeadYaw.ordinal(),
					eHeadAngles[Joint.HeadYaw.ordinal()], ms2,
					InterpolationType.LINEAR.getId());
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
				eHasBodyCommand = true;
				return;
			}
		}

		@Override
		public void setJoint(Joint joint, float[] angleValues,
				int[] durationInMills) {
			dcm.set(joint.name() + "/Position/Actuator/Value",
					MergeType.ClearAll, angleValues, durationInMills);
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
			motion.setJointStiffness(NaoV3R.Joint.RHipPitch.getId(), 1.0f);
			motion.setJointStiffness(NaoV3R.Joint.LHipPitch.getId(), 1.0f);
			motion.setJointStiffness(NaoV3R.Joint.RHipYawPitch.getId(), 1.0f);
			motion.setJointStiffness(NaoV3R.Joint.LHipYawPitch.getId(), 1.0f);
			motion.setJointStiffness(NaoV3R.Joint.RHipRoll.getId(), 1.0f);
			motion.setJointStiffness(NaoV3R.Joint.LHipRoll.getId(), 1.0f);
			motion.setJointStiffness(NaoV3R.Joint.RAnkleRoll.getId(), 1.0f);
			motion.setJointStiffness(NaoV3R.Joint.LAnkleRoll.getId(), 1.0f);
			motion.setJointStiffness(NaoV3R.Joint.RKneePitch.getId(), 1.0f);
			motion.setJointStiffness(NaoV3R.Joint.LKneePitch.getId(), 1.0f);
			motion.setJointStiffness(NaoV3R.Joint.RAnklePitch.getId(), 1.0f);
			motion.setJointStiffness(NaoV3R.Joint.LAnklePitch.getId(), 1.0f);
			motion.setTimeSeparate(angleMatrix, durationInMills,
					InterpolationType.LINEAR.getId());
			// dcm.setTimeSeparate(bodyAliasId, MergeType.ClearAll, angleMatrix,
			// durationInMills);
		}

		@Override
		public void setLed(String ledName, float luminance) {
			assert luminance >= 0 && luminance <= 1 : luminance;
			dcm.set(ledName + "/Actuator/Value", MergeType.ClearAll,
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

		@Override
		public void say(String text) {
			tts.say(text);
		}
	}
}
