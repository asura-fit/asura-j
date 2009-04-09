/*
 * 作成日: 2009/03/20
 */
package jp.ac.fit.asura.nao.naoji;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix3f;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.PressureSensor;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.naoji.NaojiContext;
import jp.ac.fit.asura.naoji.jal.JALBroker;
import jp.ac.fit.asura.naoji.jal.JALMemory;
import jp.ac.fit.asura.naoji.jal.JALMotion;
import jp.ac.fit.asura.naoji.jal.JALMemory.FloatQuery;
import jp.ac.fit.asura.naoji.jal.JALMemory.IntQuery;

/**
 * @author $Author: sey $
 *
 * @version $Id: $
 *
 */
public class NaojiDriver {
	private NaojiContext context;
	private JALMemory memory;
	private JALMotion motion;
	private float[] sAngles;
	private float[] eAngles;
	private float[] accels;
	private float[] gyros;
	private int[] forces;

	public NaojiDriver(NaojiContext context) {
		JALBroker broker = context.getParentBroker();
		memory = broker.createJALMemory();
		motion = broker.createJALMotion();
		String[] names = motion.getBodyJointNames();
		sAngles = new float[names.length];
		eAngles = new float[names.length];
		accels = new float[3];
		gyros = new float[2];
		forces = new int[8];
	}

	public class NaojiSensor implements Sensor {
		FloatQuery floatQuery;
		IntQuery intQuery;

		public void init() {
			List<String> fKeys = new ArrayList<String>();
			fKeys.add("Device/SubDeviceList/InertialSensor/AccX/Sensor/Value");
			fKeys.add("Device/SubDeviceList/InertialSensor/AccY/Sensor/Value");
			fKeys.add("Device/SubDeviceList/InertialSensor/AccZ/Sensor/Value");
			fKeys.add("Device/SubDeviceList/InertialSensor/GyrX/Sensor/Value");
			fKeys.add("Device/SubDeviceList/InertialSensor/GyrY/Sensor/Value");

			List<String> iKeys = new ArrayList<String>();
			iKeys.add("Device/SubDeviceList/LFoot/FSR/FrontLeft/Sensor/Value");
			iKeys.add("Device/SubDeviceList/LFoot/FSR/FrontRight/Sensor/Value");
			iKeys.add("Device/SubDeviceList/LFoot/FSR/RearLeft/Sensor/Value");
			iKeys.add("Device/SubDeviceList/LFoot/FSR/RearRight/Sensor/Value");
			iKeys.add("Device/SubDeviceList/RFoot/FSR/FrontLeft/Sensor/Value");
			iKeys.add("Device/SubDeviceList/RFoot/FSR/FrontRight/Sensor/Value");
			iKeys.add("Device/SubDeviceList/RFoot/FSR/RearLeft/Sensor/Value");
			iKeys.add("Device/SubDeviceList/RFoot/FSR/RearRight/Sensor/Value");

			floatQuery = memory.createFloatQuery(fKeys);
			intQuery = memory.createIntQuery(iKeys);
		}

		public void after() {
		}

		public void before() {
			floatQuery.update();
			intQuery.update();
			FloatBuffer fb = floatQuery.getBuffer();
			fb.get(accels);
			fb.get(gyros);
			fb.position(0);

			IntBuffer ib = intQuery.getBuffer();
			ib.get(forces);
			ib.position(0);

			motion.getBodyAngles(sAngles);
			System.arraycopy(sAngles, 0, eAngles, 0, sAngles.length);
		}

		public float getAccelX() {
			return accels[0];
		}

		public float getAccelY() {
			return accels[2];
		}

		public float getAccelZ() {
			return accels[1];
		}

		public float getForce(Joint joint) {
			return 0;
		}

		private float fsrFilter(float a) {
			float r = 0;
			if (a <= 0.0)
				r = 1 / a;
			r -= 0.000330907f;
			if (r <= 0.0)
				return 0;
			return r * 615078.52707555173569009555449944f;
		}

		public float getForce(PressureSensor ts) {
			return fsrFilter(1 / forces[ts.ordinal()]);
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

		public float getJoint(Joint joint) {
			return sAngles[joint.ordinal()];
		}

		public float getJointDegree(Joint joint) {
			return (float) Math.toDegrees(sAngles[joint.ordinal()]);
		}
	}

	public class NaojiEffector implements Effector {
		public void init() {
		}

		public void after() {
			motion.gotoBodyAngles(eAngles, 0.25f, 1);
		}

		public void before() {
		}

		public void setForce(Joint joint, float valueTorque) {
			// Not implemented.
		}

		public void setJoint(Joint joint, float valueInRad) {
			eAngles[joint.ordinal()] = valueInRad;
		}

		public void setJointDegree(Joint joint, float valueInDeg) {
			setJoint(joint, (float) Math.toRadians(valueInDeg));
		}

		public void setJointMicro(Joint joint, int valueInMicroRad) {
			setJoint(joint, valueInMicroRad / 1e-6f);
		}

		public void setPower(boolean sw) {
			// Set stiffness 0 or 1
			if (sw)
				motion.setBodyStiffness(1.0f);
			else
				motion.setBodyStiffness(0.0f);

		}
	}
}
