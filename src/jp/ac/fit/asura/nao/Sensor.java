/*
 * 作成日: 2008/05/07
 */
package jp.ac.fit.asura.nao;

import javax.vecmath.Matrix3f;

/**
 * @author $Author: sey $
 *
 * @version $Id: Sensor.java 717 2008-12-31 18:16:20Z sey $
 *
 */
public interface Sensor {
	public float getJoint(Joint joint);

	@Deprecated
	public float getJointDegree(Joint joint);

	public float[] getJointAngles();

	public float getAccelX();

	public float getAccelY();

	public float getAccelZ();

	public float getGyroX();

	public float getGyroZ();

	public float getForce(PressureSensor ts);

	public float getForce(Joint joint);

	public float getGpsX();

	public float getGpsY();

	public float getGpsZ();

	@Deprecated
	public void getGpsRotation(Matrix3f rotationMatrix);

	public boolean getSwitch(Switch sw);

	public void init();

	public void before();

	public void after();

}
