/*
 * 作成日: 2008/04/12
 */
package jp.ac.fit.asura.nao;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public interface Effector {

	public void setJoint(Joint joint, float valueInRad);

	public void setJointMicro(Joint joint, int valueInMicroRad);

	public void setJointDegree(Joint joint, float valueInDeg);

	public void before();

	public void after();
}
