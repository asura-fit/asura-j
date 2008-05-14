/**
 * 
 */
package jp.ac.fit.asura.nao;

import junit.framework.TestCase;

/**
 * @author s06c2026
 * 
 */
public class AsuraCoreTest extends TestCase {
	public void testCore() {
		Effector ef = new Effector() {
			public void setJoint(Joint joint, float valueInRad) {
			}

			public void setJointDegree(Joint joint, float valueInDeg) {
			}

			public void setJointMicro(Joint joint, int valueInMicroRad) {
			}
		};
		Sensor ss = new Sensor() {
			public Image getImage() {
				return new Image(new int[9], 3, 3);
			}

			public float getJoint(Joint joint) {
				return 0.0F;
			}
		};
		AsuraCore core = new AsuraCore(ef, ss);
		core.init();

		// while (true) {
		core.run(40);
		// try {
		// Thread.sleep(40);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
	}
}
