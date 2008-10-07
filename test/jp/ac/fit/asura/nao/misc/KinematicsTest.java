/*
 * 作成日: 2008/10/08
 */
package jp.ac.fit.asura.nao.misc;

import static jp.ac.fit.asura.nao.physical.Nao.Frames.Body;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.Camera;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.HeadPitch;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.HeadYaw;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RAnklePitch;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RAnkleRoll;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RHipPitch;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RHipRoll;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RHipYawPitch;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RKneePitch;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RSole;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RSoleFL;

import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.sensation.FrameState;
import jp.ac.fit.asura.nao.sensation.SomaticContext;
import junit.framework.TestCase;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class KinematicsTest extends TestCase {
	public void testForwardKinematics() {
		SomaticContext sc = new SomaticContext();
		Kinematics.calculateForward(sc);
		for (FrameState fs : sc.getFrames()) {
			System.out.println(fs.getId());
			System.out.println(fs.getRobotPosition());
		}
		assertEquals(new Vector3f(0, 0, 0), sc.get(Body).getRobotPosition());
		assertEquals(new Vector3f(0, 160, -20), sc.get(HeadYaw)
				.getRobotPosition());
		assertEquals(new Vector3f(0, 160 + 60, -20), sc.get(HeadPitch)
				.getRobotPosition());
		assertEquals(new Vector3f(0, 160 + 60 + 30, -20 + 58), sc.get(Camera)
				.getRobotPosition());
		assertEquals(new Vector3f(-55, -45, -30), sc.get(RHipYawPitch)
				.getRobotPosition());
		assertEquals(new Vector3f(-55, -45, -30), sc.get(RHipRoll)
				.getRobotPosition());
		assertEquals(new Vector3f(-55, -45, -30), sc.get(RHipPitch)
				.getRobotPosition());
		assertEquals(new Vector3f(-55, -45 - 120, -30 + 5), sc.get(RKneePitch)
				.getRobotPosition());
		assertEquals(new Vector3f(-55, -45 - 120 - 100, -30 + 5), sc.get(
				RAnklePitch).getRobotPosition());
		assertEquals(new Vector3f(-55, -45 - 120 - 100, -30 + 5), sc.get(
				RAnkleRoll).getRobotPosition());
		assertEquals(new Vector3f(-55, -45 - 120 - 100 - 55, -30 + 5), sc.get(
				RSole).getRobotPosition());
		assertEquals(new Vector3f(-55 + 23.17f, -45 - 120 - 100 - 55,
				-30 + 5 + 69.909996f), sc.get(RSoleFL).getRobotPosition());

		sc.get(RHipYawPitch).updateValue((float) Math.PI / 2);
		Kinematics.calculateForward(sc);
		assertEquals(new Vector3f(-55, -45, -30), sc.get(RHipYawPitch)
				.getRobotPosition());
		assertFalse(new Vector3f(-55, -45 - 120, -30 + 5).equals(sc.get(
				RKneePitch).getRobotPosition()));
	}
}
