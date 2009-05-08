/*
 * 作成日: 2009/04/21
 */
package jp.ac.fit.asura.nao.naoji;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.PressureSensor;
import jp.ac.fit.asura.nao.SensorContext;
import jp.ac.fit.asura.nao.Switch;
import jp.ac.fit.asura.nao.misc.MathUtils;

/**
 * @author sey
 *
 * @version $Id: $
 *
 */
public class NaojiSensorContext extends SensorContext {
	float[] angles;
	float[] accels;
	float[] gyros;
	float[] inertialAngles;
	float[] forces;
	float[] cofPositions;
	float[] witches;
	long time;

	protected NaojiSensorContext() {
		Joint[] joints = Joint.values();
		angles = new float[joints.length];
		accels = new float[3];
		gyros = new float[2];
		inertialAngles = new float[2];
		forces = new float[8];
		cofPositions = new float[4];
		witches = new float[5];
	}

	// TODO マッピングがおかしい.
	public float getAccelX() {
		return accels[1];
	}

	public float getAccelY() {
		return -accels[2];
	}

	public float getAccelZ() {
		return -accels[0];
	}

	public float getForce(Joint joint) {
		return 0;
	}

	public float getForce(PressureSensor ts) {
		return forces[ts.ordinal()];
	}

	public float getGyroX() {
		return gyros[1];
	}

	public float getGyroZ() {
		return gyros[0];
	}

	@Override
	public float getInertialX() {
		return inertialAngles[1];
	}

	@Override
	public float getInertialZ() {
		return inertialAngles[0];
	}

	public float[] getJointAngles() {
		return angles;
	}

	public float getJoint(Joint joint) {
		return angles[joint.ordinal()];
	}

	public float getJointDegree(Joint joint) {
		return MathUtils.toDegrees(getJoint(joint));
	}

	@Override
	public boolean getSwitch(Switch sw) {
		// according to the RedBook DCM, value = 0.0 if button is pressed.
		return witches[sw.ordinal()] == 1.0f;
	}

	@Override
	public long getTime() {
		return time;
	}
}
