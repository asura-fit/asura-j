/*
 * 作成日: 2008/10/08
 */
package jp.ac.fit.asura.nao.misc;

import static jp.ac.fit.asura.nao.misc.MathUtils.EPSf;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.Body;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.HeadPitch;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.HeadYaw;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.LAnkleRoll;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.NaoCam;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.RAnklePitch;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.RAnkleRoll;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.RFsrFL;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.RHipPitch;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.RHipRoll;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.RHipYawPitch;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.RKneePitch;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.RSole;

import java.util.SortedMap;
import java.util.TreeMap;

import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.motion.MotionUtils;
import jp.ac.fit.asura.nao.physical.Robot;
import jp.ac.fit.asura.nao.physical.RobotTest;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.FrameState;
import jp.ac.fit.asura.nao.sensation.SomaticContext;
import jp.ac.fit.asura.vecmathx.GVector;
import junit.framework.TestCase;

/**
 * @author $Author: sey $
 *
 * @version $Id: KinematicsTest.java 717 2008-12-31 18:16:20Z sey $
 *
 */
public class KinematicsTest extends TestCase {
	/**
	 * よさそうなSCALEとLANGLEのパラメータを探索する
	 */
	public void testInverseKinematicsStability2() throws Exception {
		SomaticContext sc = new SomaticContext(RobotTest.createRobot());

		SortedMap<Double, String> m = new TreeMap<Double, String>();
		// for (double s = 0.0625; s <= 1.25; s += 0.0625) {
		for (double s = 1.0; s <= 1.0; s += 0.0625) {
			// for (double a = Math.PI / 72; a < 1.0; a += Math.PI / 72)
			for (double a = 0.75; a < 1.25; a += Math.PI / 72)
				try {
					Kinematics.LANGLE = a;
					Kinematics.SCALE = s;
					long l = System.currentTimeMillis();
					int n = 0;
					int FACTOR = 10000;
					for (int i = 0; i < FACTOR; i++) {
						if (i % 1000 == 0)
							System.out.println(i);
						setAngleRandom(sc);

						Kinematics.calculateForward(sc);
						FrameState fs = sc.get(Frames.RAnkleRoll).clone();

						setAngleRandom(sc);

						n += Kinematics.calculateInverse(sc, fs);
						assertTrue(fs.getBodyPosition().toString() + "\n"
								+ sc.get(RAnkleRoll).getBodyPosition(), fs
								.getBodyPosition().epsilonEquals(
										sc.get(RAnkleRoll).getBodyPosition(),
										1e-1f));

						assertTrue(fs.getBodyRotation().toString() + "\n"
								+ sc.get(RAnkleRoll).getBodyRotation(), fs
								.getBodyRotation().epsilonEquals(
										sc.get(RAnkleRoll).getBodyRotation(),
										1e-1f));

						for (FrameState f : sc.getFrames()) {
							if (f.getId().isJoint()) {
								assertTrue(f.getId() + " out of range:"
										+ f.getAngle(), MotionUtils.isInRange(f
										.getFrame(), f.getAngle()));
							}
						}
					}
					long l2 = System.currentTimeMillis();
					double tries = n / (double) FACTOR;
					System.out.println((l2 - l) / (double) FACTOR);
					System.out.println(tries);
					m.put(tries, "n = " + tries + " sec" + (l2 - l)
							/ (double) FACTOR + " s:" + s + " a:" + a);
				} catch (RuntimeException re) {
					System.out.println("error at s:" + s + " a:" + a);
					// re.printStackTrace();
				} catch (Error re) {
					System.out.println("error at s:" + s + " a:" + a);
					// re.printStackTrace();
				}
		}
		for (String s : m.values())
			System.out.println(s);
	}

	/**
	 * 関節の可動域内でランダムに計算して、順運動学と逆運動学の結果が一致するかをテストする.
	 *
	 */
	public void testInverseKinematics() throws Exception {
		SomaticContext sc = new SomaticContext(RobotTest.createRobot());

		Kinematics.SCALE = 0.125;
		Kinematics.LANGLE = Math.PI / 16;

		long l = System.currentTimeMillis();
		long n = 0;
		int worst = 0;
		// 何度も計算して安定しているかをテストする
		int FACTOR = 100000;
		for (int i = 0; i < FACTOR; i++) {
			if (i % 1000 == 0)
				System.out.println(i);

			// 関節を可動範囲内でランダムにセット
			setAngleRandom(sc);

			// RAnkleRollの現在位置を取得
			Kinematics.calculateForward(sc);
			FrameState fs = sc.get(Frames.RAnkleRoll).clone();
			float[] b = new float[] {
					sc.get(Frames.RHipYawPitch).getAxisAngle().angle,
					sc.get(Frames.RHipPitch).getAxisAngle().angle,
					sc.get(Frames.RHipRoll).getAxisAngle().angle,
					sc.get(Frames.RKneePitch).getAxisAngle().angle,
					sc.get(Frames.RAnklePitch).getAxisAngle().angle,
					sc.get(Frames.RAnkleRoll).getAxisAngle().angle };

			// 関節を再びランダムにセット
			setAngleRandom(sc);

			// 最初に取得した値を目標に逆運動学計算
			try {
				int m = Kinematics.calculateInverse(sc, fs);
				if (m > worst) {
					worst = m;
					System.out.println("worst:" + worst);
				}
				n += m;
			} catch (AssertionError error) {
				for (float a : b)
					System.out.print(Math.toDegrees(a) + ",");
				throw error;
			}
			// for (FrameState fs2 : sc.getFrames()) {
			// System.out.println(fs2.getId());
			// System.out.println(Math.toDegrees(fs2.getAngle()));
			// }

			// 関節位置と姿勢は最初の値に一致しているか?
			assertTrue(fs.getBodyPosition().toString() + "\n"
					+ sc.get(RAnkleRoll).getBodyPosition(), fs
					.getBodyPosition().epsilonEquals(
							sc.get(RAnkleRoll).getBodyPosition(), 1e-1f));

			assertTrue(fs.getBodyRotation().toString() + "\n"
					+ sc.get(RAnkleRoll).getBodyRotation(), fs
					.getBodyRotation().epsilonEquals(
							sc.get(RAnkleRoll).getBodyRotation(), 1e-1f));

			for (FrameState f : sc.getFrames()) {
				if (f.getId().isJoint()) {
					assertTrue(f.getId() + " out of range:" + f.getAngle(),
							MotionUtils.isInRange(f.getFrame(), f.getAngle()));
				}
			}
		}
		long l2 = System.currentTimeMillis();
		double tries = n / (double) FACTOR;
		System.out.println("average ms:" + (l2 - l) / (double) FACTOR);
		System.out.println(tries);
		System.out.println("worst" + worst);
	}

	/**
	 * 左足バージョン
	 */
	public void testInverseKinematicsL() throws Exception {
		SomaticContext sc = new SomaticContext(RobotTest.createRobot());

		// Kinematics.SCALE = 0.5;
		// Kinematics.LANGLE = Math.PI/10;

		long l = System.currentTimeMillis();
		long n = 0;
		int worst = 0;
		int FACTOR = 100000;
		for (int i = 0; i < FACTOR; i++) {
			if (i % 1000 == 0)
				System.out.println(i);

			// 関節を可動範囲内でランダムにセット
			setAngleRandom(sc);

			// RAnkleRollの現在位置を取得
			Kinematics.calculateForward(sc);
			FrameState fs = sc.get(Frames.LAnkleRoll).clone();
			float[] b = new float[] {
					sc.get(Frames.LHipYawPitch).getAxisAngle().angle,
					sc.get(Frames.LHipPitch).getAxisAngle().angle,
					sc.get(Frames.LHipRoll).getAxisAngle().angle,
					sc.get(Frames.LKneePitch).getAxisAngle().angle,
					sc.get(Frames.LAnklePitch).getAxisAngle().angle,
					sc.get(Frames.LAnkleRoll).getAxisAngle().angle };

			// 関節を再びランダムにセット
			setAngleRandom(sc);

			// 最初に取得した値を目標に逆運動学計算
			try {
				int m = Kinematics.calculateInverse(sc, fs);
				if (m > worst) {
					worst = m;
					System.out.println("worst:" + worst);
				}
				n += m;
			} catch (AssertionError error) {
				for (float a : b)
					System.out.print(Math.toDegrees(a) + ",");
				throw error;
			}
			// for (FrameState fs2 : sc.getFrames()) {
			// System.out.println(fs2.getId());
			// System.out.println(Math.toDegrees(fs2.getAngle()));
			// }

			// 関節位置と姿勢は最初の値に一致しているか?
			assertTrue(fs.getBodyPosition().toString() + "\n"
					+ sc.get(LAnkleRoll).getBodyPosition(), fs
					.getBodyPosition().epsilonEquals(
							sc.get(LAnkleRoll).getBodyPosition(), 1e-1f));

			assertTrue(fs.getBodyRotation().toString() + "\n"
					+ sc.get(LAnkleRoll).getBodyRotation(), fs
					.getBodyRotation().epsilonEquals(
							sc.get(LAnkleRoll).getBodyRotation(), 1e-1f));

			for (FrameState f : sc.getFrames()) {
				if (f.getId().isJoint()) {
					assertTrue(f.getId() + " out of range:" + f.getAngle(),
							MotionUtils.isInRange(f.getFrame(), f.getAngle()));
				}
			}
		}
		long l2 = System.currentTimeMillis();
		double tries = n / (double) FACTOR;
		System.out.println((l2 - l) / (double) FACTOR);
		System.out.println(tries);
		System.out.println("worst" + worst);
	}

	public void testInverseKinematicsSpaces() throws Exception {
		SomaticContext sc = new SomaticContext(RobotTest.createRobot());
		for (FrameState fs : sc.getFrames()) {
			if (fs.getId().isJoint()) {
				fs.getAxisAngle().angle = 0;
			}
		}
		Kinematics.SCALE = 0.75;
		Kinematics.LANGLE = Math.PI / 4;

		// 関節を可動範囲内でランダムにセット
		// setAngleRandom(sc);

		// RAnkleRollの現在位置を取得
		Kinematics.calculateForward(sc);
		FrameState fs = sc.get(Frames.LAnkleRoll).clone();
		// fs.getBodyPosition().set(45.0f, -260.0f, 9.402309f);
		fs.getBodyPosition().y = -260;

		fs.getBodyRotation().setIdentity();

		// 最初に取得した値を目標に逆運動学計算
		int tries = Kinematics.calculateInverse(sc, fs);
		System.out.println("tries:" + tries);
		for (FrameState fs2 : sc.getFrames()) {
			System.out.println(fs2.getId());
			System.out.println(Math.toDegrees(fs2.getAngle()));
		}

		// 関節位置と姿勢は最初の値に一致しているか?

		for (FrameState f : sc.getFrames()) {
			if (f.getId().isJoint()) {
				assertTrue(f.getId() + " out of range:" + f.getAngle(),
						MotionUtils.isInRange(f.getFrame(), f.getAngle()));
			}
		}
	}

	public void testForwardKinematics() throws Exception {
		// 順運動学のテスト
		// 予め計算した値と一致するかをテストする
		SomaticContext sc = new SomaticContext(RobotTest.createRobot());
		Kinematics.calculateForward(sc);
		for (FrameState fs : sc.getFrames()) {
			System.out.println(fs.getId());
			System.out.println(fs.getBodyPosition());
		}
		assertEquals(new Vector3f(0, 0, 0), sc.get(Body).getBodyPosition(),
				1e-1f);
		assertEquals(new Vector3f(0, 126.5f, 0), sc.get(HeadYaw)
				.getBodyPosition(), 1e-1f);
		assertEquals(new Vector3f(0, 126.5f, 0), sc.get(HeadPitch)
				.getBodyPosition(), 1e-1f);
		assertEquals(new Vector3f(0, 126.5f + 67.90f, 53.90f), sc.get(NaoCam)
				.getBodyPosition(), 1e-1f);
		assertEquals(new Vector3f(-50, -85, 0), sc.get(RHipYawPitch)
				.getBodyPosition(), 1e-1f);
		assertEquals(new Vector3f(-50, -85, 0), sc.get(RHipRoll)
				.getBodyPosition(), 1e-1f);
		assertEquals(new Vector3f(-50, -85, 0), sc.get(RHipPitch)
				.getBodyPosition(), 1e-1f);
		assertEquals(new Vector3f(-50, -85 - 100, 0), sc.get(RKneePitch)
				.getBodyPosition(), 1e-1f);
		assertEquals(new Vector3f(-50, -85 - 100 - 100, 0), sc.get(RAnklePitch)
				.getBodyPosition(), 1e-1f);
		assertEquals(new Vector3f(-50, -85 - 100 - 100, 0), sc.get(RAnkleRoll)
				.getBodyPosition(), 1e-1f);
		assertEquals(new Vector3f(-50, -85 - 100 - 100 - 46, 0), sc.get(RSole)
				.getBodyPosition(), 1e-1f);
		// Red Doc - HardwareのRFsrFL - RFsrBRはFLとFR,BLとBRが逆になっている
		assertEquals(new Vector3f(-50 + 23, -85 - 100 - 100 - 45, 70.1f), sc
				.get(RFsrFL).getBodyPosition(), 1e-1f);

		sc.get(RHipYawPitch).updateValue((float) Math.PI / 2);
		Kinematics.calculateForward(sc);
		// assertEquals(new Vector3f(-55, -45, -30), sc.get(RHipYawPitch)
		// .getBodyPosition(), 1e-1f);
		// assertFalse(new Vector3f(-55, -45 - 120, -30 + 5).equals(sc.get(
		// RKneePitch).getBodyPosition()));
		for (FrameState fs : sc.getFrames()) {
			System.out.println(fs.getId());
			System.out.println(fs.getBodyPosition());
			System.out.println(fs.getBodyRotation());
		}
	}

	public void testForwardKinematicsHead() throws Exception {
		SomaticContext context = new SomaticContext(RobotTest.createRobot());
		context.get(Frames.HeadPitch).updateValue(MathUtils.PIf / 2);

		// context.get(Frames.NaoCam).updateValue(0);
		// context.get(Frames.NaoCam).updateValue(MathUtils.PIf / 2);
		Kinematics.calculateForward(context);
		assertEquals(new Vector3f(0, 126.5f, 0), context.get(Frames.HeadYaw)
				.getBodyPosition(), EPSf);
		assertEquals(new Vector3f(0, 126.5f, 67.9f), context.get(
				Frames.CameraSelect).getBodyPosition(), EPSf);
		assertEquals(new Vector3f(0, 126.5f - 53.9f, 67.9f), context.get(
				Frames.NaoCam).getBodyPosition(), EPSf);

		Vector3f rpy = new Vector3f();
		System.out.println(context.get(Frames.NaoCam).getAxisAngle());
		MatrixUtils.rot2pyr(context.get(Frames.NaoCam).getRotation(), rpy);
		System.out.println(rpy);
		System.out.println(context.get(Frames.NaoCam).getRotation());
		//
		// Matrix3f mat = new Matrix3f();
		// MatrixUtils.rpy2rot(rpy, mat);
		// System.out.println(mat);

		context.get(Frames.HeadPitch).updateValue(-MathUtils.PIf / 2);
		Kinematics.calculateForward(context);
		assertEquals(new Vector3f(0, 126.5f + 53.9f, -67.9f), context.get(
				Frames.NaoCam).getBodyPosition(), EPSf);
	}

	public void testForwardKinematicsHead2() throws Exception {
		SomaticContext context = new SomaticContext(RobotTest.createRobot());

		//
		context.get(Frames.NaoCam).updateValue(MathUtils.PIf);
		Kinematics.calculateForward(context);
		Vector3f cameraVec = new Vector3f(0, 0, 100);
		Coordinates.toBodyCoord(context, Frames.NaoCam, cameraVec, cameraVec);
		assertEquals(new Vector3f(0.0f, 194.4f, -46.1f), cameraVec, 1e-3f);

		//
		context.get(Frames.NaoCam).updateValue(0);
		Kinematics.calculateForward(context);
		Vector3f cameraVec2 = new Vector3f(0, 0, 100);
		Coordinates.toBodyCoord(context, Frames.NaoCam, cameraVec2, cameraVec2);
		assertEquals(new Vector3f(0.0f, 194.4f, 153.9f), cameraVec2, EPSf);

		//
		context.get(Frames.NaoCam).updateValue(MathUtils.PIf);
		context.get(Frames.HeadYaw).updateValue(MathUtils.PIf / 6);
		Kinematics.calculateForward(context);
		Vector3f cameraVec3 = new Vector3f(0, 0, -100);
		Coordinates.toBodyCoord(context, Frames.NaoCam, cameraVec3, cameraVec3);
		assertEquals(new Vector3f((53.9f + 100)
				* (float) Math.sin(MathUtils.PIf / 6), 194.4f, (53.9f + 100)
				* (float) Math.cos(MathUtils.PIf / 6)), cameraVec3, 1e-3f);

		context.get(Frames.NaoCam).updateValue(MathUtils.PIf);
		context.get(Frames.HeadYaw).updateValue(MathUtils.PIf / 6);
		context.get(Frames.HeadPitch).updateValue(MathUtils.PIf / 4);
		Kinematics.calculateForward(context);
		Vector3f cameraVec4 = new Vector3f(0, 0, -100);
		Coordinates.toBodyCoord(context, Frames.NaoCam, cameraVec4, cameraVec4);
		System.out.println(cameraVec4);
	}

	/**
	 * @param vector3f
	 * @param bodyPosition
	 * @param delta
	 */
	private void assertEquals(Vector3f expected, Vector3f actual, float delta) {
		assertTrue("Expected " + expected.toString() + " but actual "
				+ actual.toString(), expected.epsilonEquals(actual, delta));
	}

	public void testCalcError() throws Exception {
		Robot robot = RobotTest.createRobot();
		// 誤差計算のテスト?
		// あまり意味なし
		GVector err = new GVector(6);
		// target
		FrameState fs1 = new FrameState(robot.get(Body));
		fs1.getBodyRotation().set(
				new float[] { 0.5f, 0.50000006f, 0.7071068f, 0.50000006f, 0.5f,
						-0.7071068f, -0.7071068f, 0.7071068f, -4.371139E-8f });
		assertEquals(1f, fs1.getBodyRotation().determinant(), 1e-3);

		// current
		FrameState fs2 = new FrameState(robot.get(Body));
		fs2.getBodyRotation().set(
				new float[] { -0.18696824f, -0.5456833f, -0.81686765f,
						0.67815393f, -0.6733057f, 0.29456228f, -0.7107393f,
						-0.49888813f, 0.49594402f });
		assertEquals(1f, fs2.getBodyRotation().determinant(), 1e-3);
		// Kinematics.calcError(fs1, fs2, err);
		assertTrue(err.toString(), err.normSquared() < 1e10);
	}

	// 関節角度を可動範囲内でランダムにセット
	private void setAngleRandom(SomaticContext sc) {
		for (FrameState fs : sc.getFrames()) {
			if (fs.getId().isJoint()) {
				float max = fs.getFrame().getMaxAngle();
				float min = fs.getFrame().getMinAngle();
				fs.getAxisAngle().angle = (float) MathUtils.rand(min, max);
			}
		}
	}

	// 関節角度を-PI <= angle < PIでランダムにセット
	private void setAngleRandomPI(SomaticContext sc) {
		for (FrameState fs : sc.getFrames()) {
			if (fs.getId().isJoint())
				fs.getAxisAngle().angle = (float) ((2 * Math.random() - 1) * Math.PI);
		}
	}
}
