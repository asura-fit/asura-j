/**
 * 
 */
package jp.ac.fit.asura.nao;

import java.nio.ByteBuffer;

import javax.vecmath.Matrix3f;

import jp.ac.fit.asura.nao.misc.MathUtils;
import junit.framework.TestCase;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class AsuraCoreTest extends TestCase {
	public static DatagramService createDatagramServiceStub() {
		return new DatagramService() {
			public byte[] receive() {
				return null;
			}

			public void receive(ByteBuffer buf) {
			}

			public void send(ByteBuffer buf) {
			}
		};
	}

	public static Effector createEffectorStub() {
		return new Effector() {
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
	}

	public static Sensor createSensorStub(final float[] values) {
		return new Sensor() {
			public Image getImage() {
				return new Image(new int[9], 3, 3, 0.8f, 0.8f);
			}

			public float getJoint(Joint joint) {
				return values[joint.ordinal()];
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
			
			public float getGyroX() {
				return 0;
			}
			
			public float getGyroZ() {
				return 0;
			}

			public float getJointDegree(Joint joint) {
				return MathUtils.toDegrees(getJoint(joint));
			}

			public int getForce(PressureSensor ts) {
				return 40;
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

			public void getGpsRotation(Matrix3f rotationMatrix) {
			}

			public void after() {
			}

			public void before() {
			}
		};
	}
	
	public static AsuraCore createCore(){
		AsuraCore core = new AsuraCore(createEffectorStub(),
				createSensorStub(new float[Joint.values().length]),
				createDatagramServiceStub());
		return core;
	}

	public void testCore() {
		AsuraCore core = createCore();
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
