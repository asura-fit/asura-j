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

	public void setJoint(Joint joint, float valueInRad);

	public void setJointMicro(Joint joint, int valueInMicroRad);

	public void setJointDegree(Joint joint, float valueInDeg);

	public void setForce(Joint joint, float valueTorque);
	
	public void setPower(boolean sw);
	
	public void before();

	public void after();
}
