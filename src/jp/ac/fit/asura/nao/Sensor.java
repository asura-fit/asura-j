/*
 * 作成日: 2008/05/07
 */
package jp.ac.fit.asura.nao;

import javax.vecmath.Matrix3f;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public interface Sensor {
	public Image getImage();

	public float getJoint(Joint joint);

	public float getJointDegree(Joint joint);

	public float getAccelX();

	public float getAccelY();

	public float getAccelZ();

	public int getForce(PressureSensor ts);

	public float getForce(Joint joint);

	public float getGpsX();

	public float getGpsY();

	public float getGpsZ();

	public void getGpsRotation(Matrix3f rotationMatrix);

	public void before();

	public void after();

}
