/**
 * 
 */
package jp.ac.fit.asura.nao;

import java.nio.ByteBuffer;

import junit.framework.TestCase;

/**
 * @author s06c2026
 * 
 */
public class AsuraCoreTest extends TestCase {
	public void testCore() {
		DatagramService ds = new DatagramService() {
			public byte[] receive() {
				return new byte[0];
			}

			public void receive(ByteBuffer buf) {
			}

			public void send(ByteBuffer buf) {
			}
		};
		Effector ef = new Effector() {
			public void setJoint(Joint joint, float valueInRad) {
			}

			public void setJointDegree(Joint joint, float valueInDeg) {
			}

			public void setJointMicro(Joint joint, int valueInMicroRad) {
			}
			
			public void setForce(Joint joint, float valueTorque) {
			}

			public void after() {
			}

			public void before() {
			}

			public void setPower(boolean sw) {
			}
		};
		Sensor ss = new Sensor() {
			public Image getImage() {
				return new Image(new int[9], 3, 3, 0.8f, 0.8f);
			}

			public float getJoint(Joint joint) {
				return 0.0F;
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

			public float getJointDegree(Joint joint) {
				return 0.0f;
			}

			public int getForce(PressureSensor ts) {
				return 0;
			}

			public float getForce(Joint joint) {
				return 0;
			}

			public float getGpsX() {
				return 0;
			}

			public float getGpsY() {
				return 0;
			}

			public float getGpsZ() {
				return 0;
			}

			public float getGpsHeading() {
				return 0;
			}

			public void after() {
			}

			public void before() {
			}
		};
		AsuraCore core = new AsuraCore(ef, ss, ds);
		core.init();

		while (true) {
			core.run(40);
			try {
				Thread.sleep(40);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
