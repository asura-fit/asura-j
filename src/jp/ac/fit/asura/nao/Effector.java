/*
 * 作成日: 2008/04/12
 */
package jp.ac.fit.asura.nao;

/**
 * @author $Author: sey $
 *
 * @version $Id: Effector.java 709 2008-11-23 07:40:31Z sey $
 *
 */
public interface Effector {

	public float[] getJointBuffer();

	public void setJoint(Joint joint, float valueInRad);

	public void setJointMicro(Joint joint, int valueInMicroRad);

	@Deprecated
	public void setJointDegree(Joint joint, float valueInDeg);

	@Deprecated
	public void setForce(Joint joint, float valueTorque);

	public void setBodyJoints(float[] angleMatrix, int[] durationInMills);

	public void setPower(float power);

	public void setPower(Joint joint, float power);

	public void init();

	public void before();

	public void after();
}
