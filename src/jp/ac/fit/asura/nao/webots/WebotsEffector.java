/*
 * 作成日: 2008/04/12
 */
package jp.ac.fit.asura.nao.webots;

import java.util.EnumMap;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.Joint;

import com.cyberbotics.webots.Controller;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class WebotsEffector implements Effector {
	private EnumMap<Joint, Integer> joints;

	/**
	 * 
	 */
	public WebotsEffector() {
		joints = new EnumMap<Joint, Integer>(Joint.class);
		for (Joint joint : Joint.values()) {
			joints.put(joint, Controller.robot_get_device(joint.toString()));
		}
	}

	public void setJoint(Joint joint, float valueInRad) {
		assert joints.containsKey(joint);
		Controller.servo_set_position(joints.get(joint), valueInRad);
	}

	public void setJointDegree(Joint joint, float valueInDeg) {
		setJoint(joint, (float) (valueInDeg * Math.PI / 180.0));
	}

	public void setJointMicro(Joint joint, int valueInMicroRad) {
		setJoint(joint, valueInMicroRad / 1000000.0F);
	}

	public void after() {
	}

	public void before() {
	}
}
