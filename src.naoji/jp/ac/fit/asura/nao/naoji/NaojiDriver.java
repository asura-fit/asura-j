/*
 * 作成日: 2009/03/20
 */
package jp.ac.fit.asura.nao.naoji;

import javax.vecmath.Matrix3f;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.Image;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.PressureSensor;
import jp.ac.fit.asura.nao.Sensor;

/**
 * @author $Author: sey $
 *
 * @version $Id: $
 *
 */
public class NaojiDriver {

	public class NaojiSensor implements Sensor {
		public void init() {
		}

		public void after() {
		}

		public void before() {
		}

		public float getAccelX() {
			return 0;
		}

		public float getAccelY() {
			return 0;
		}

		public float getAccelZ() {
			return 0;
		}

		public float getForce(Joint joint) {
			return 0;
		}

		public float getForce(PressureSensor ts) {
			return 0;
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
			return 0;
		}

		public float getGyroZ() {
			return 0;
		}

		public Image getImage() {
			return null;
		}

		public float getJoint(Joint joint) {
			return 0;
		}

		public float getJointDegree(Joint joint) {
			return 0;
		}
	}

	public class NaojiEffector implements Effector {
		public void init() {
		}

		public void after() {
		}

		public void before() {
		}

		public void setForce(Joint joint, float valueTorque) {
			// Not implemented.
		}

		public void setJoint(Joint joint, float valueInRad) {
		}

		public void setJointDegree(Joint joint, float valueInDeg) {
		}

		public void setJointMicro(Joint joint, int valueInMicroRad) {
		}

		public void setPower(boolean sw) {
			// Set stiffness 0 or 1
		}
	}
}
