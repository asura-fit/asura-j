/*
 * 作成日: 2009/04/21
 */
package jp.ac.fit.asura.nao;

import javax.vecmath.Matrix3f;

import jp.ac.fit.asura.nao.misc.MathUtils;

/**
 * @author sey
 *
 * @version $Id: $
 *
 */
public abstract class SensorContext extends Context {

	public abstract float getJoint(Joint joint);

	@Deprecated
	public float getJointDegree(Joint joint) {
		return MathUtils.toDegrees(getJoint(joint));
	}

	public abstract float[] getJointAngles();

	public abstract float getAccelX();

	public abstract float getAccelY();

	public abstract float getAccelZ();

	public abstract float getGyroX();

	public abstract float getGyroZ();

	public abstract float getForce(PressureSensor ts);

	@Deprecated
	public abstract float getForce(Joint joint);

	@Deprecated
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

	public boolean getSwitch(Switch sw) {
		return false;
	}

	public abstract long getTime();
}
