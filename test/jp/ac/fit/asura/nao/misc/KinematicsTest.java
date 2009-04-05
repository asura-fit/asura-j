/*
 * 作成日: 2008/10/08
 */
package jp.ac.fit.asura.nao.misc;

import static jp.ac.fit.asura.nao.physical.Robot.Frames.Body;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.HeadPitch;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.HeadYaw;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.LAnkleRoll;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.LSole;
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

import javax.vecmath.GVector;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.motion.MotionUtils;
import jp.ac.fit.asura.nao.physical.Robot;
import jp.ac.fit.asura.nao.physical.RobotTest;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.FrameState;
import jp.ac.fit.asura.nao.sensation.SomaticContext;
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

		// Kinematics.SCALE = 0.5;
		// Kinematics.LANGLE = Math.PI/10;

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

	public void testInverseKinematics3() throws Exception {
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
			FrameState fs = sc.get(Frames.LSole).clone();
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
					+ sc.get(LSole).getBodyPosition(), fs.getBodyPosition()
					.epsilonEquals(sc.get(LSole).getBodyPosition(), 1e-1f));

			assertTrue(fs.getBodyRotation().toString() + "\n"
					+ sc.get(LSole).getBodyRotation(), fs.getBodyRotation()
					.epsilonEquals(sc.get(LSole).getBodyRotation(), 1e-1f));

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

	/**
	 * 左足バージョン
	 */
	public void testInverseKinematics4() throws Exception {
		SomaticContext sc = new SomaticContext(RobotTest.createRobot());

		long l = System.currentTimeMillis();
		long n = 0;
		int worst = 0;
		setAngleRandom(sc);

		FrameState lar = new FrameState(sc.getRobot().get(LAnkleRoll));
		FrameState rar = new FrameState(sc.getRobot().get(RAnkleRoll));
		lar.getBodyRotation().setIdentity();
		lar.getBodyPosition().set(50, (float) (-250 + Math.cos(0)),
				(float) (0 + 50 * Math.sin(0)));
		rar.getBodyRotation().setIdentity();
		rar.getBodyPosition().set(-50, (float) (-250 + Math.cos(0 - Math.PI)),
				(float) (0 + 50 * Math.sin(0 - Math.PI)));

		for (double t = 0; t < 2 * Math.PI; t += Math.PI / 180.0) {
			System.out.println(t);

			Kinematics.calculateForward(sc);

			lar.getBodyPosition().set(50, (float) (-200 + 50 * Math.cos(t)),
					(float) (t + 50 * Math.sin(t)));
			rar.getBodyPosition().set(-50,
					(float) (-200 + 50 * Math.cos(t - Math.PI)),
					(float) (t + 50 * Math.sin(t - Math.PI)));

			// 最初に取得した値を目標に逆運動学計算
			int m = Kinematics.calculateInverse(sc, lar);
			if (m > worst) {
				worst = m;
				System.out.println("worst:" + worst);
			}
			n += m;
			m = Kinematics.calculateInverse(sc, rar);
			if (m > worst) {
				worst = m;
				System.out.println("worst:" + worst);
			}
			n += m;
			// for (FrameState fs2 : sc.getFrames()) {
			// System.out.println(fs2.getId());
			// System.out.println(Math.toDegrees(fs2.getAngle()));
			// }

			// 関節位置と姿勢は最初の値に一致しているか?
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
		assertTrue(new Vector3f(0, 0, 0).epsilonEquals(sc.get(Body)
				.getBodyPosition(), 1e-1f));
		assertTrue(new Vector3f(0, 160, -20).epsilonEquals(sc.get(HeadYaw)
				.getBodyPosition(), 1e-1f));
		assertTrue(new Vector3f(0, 160 + 60, -20).epsilonEquals(sc.get(
				HeadPitch).getBodyPosition(), 1e-1f));
		assertTrue(new Vector3f(0, 160 + 60 + 30, -20 + 58).epsilonEquals(sc
				.get(NaoCam).getBodyPosition(), 1e-1f));
		assertTrue(new Vector3f(-55, -45, -30).epsilonEquals(sc.get(
				RHipYawPitch).getBodyPosition(), 1e-1f));
		assertTrue(new Vector3f(-55, -45, -30).epsilonEquals(sc.get(RHipRoll)
				.getBodyPosition(), 1e-1f));
		assertTrue(new Vector3f(-55, -45, -30).epsilonEquals(sc.get(RHipPitch)
				.getBodyPosition(), 1e-1f));
		assertTrue(new Vector3f(-55, -45 - 120, -30 + 5).epsilonEquals(sc.get(
				RKneePitch).getBodyPosition(), 1e-1f));
		assertTrue(new Vector3f(-55, -45 - 120 - 100, -30 + 5).epsilonEquals(sc
				.get(RAnklePitch).getBodyPosition(), 1e-1f));
		assertTrue(new Vector3f(-55, -45 - 120 - 100, -30 + 5).epsilonEquals(sc
				.get(RAnkleRoll).getBodyPosition(), 1e-1f));
		assertTrue(new Vector3f(-55, -45 - 120 - 100 - 55, -30 + 5)
				.epsilonEquals(sc.get(RSole).getBodyPosition(), 1e-1f));
		assertTrue(new Vector3f(-55 + 23.17f, -45 - 120 - 100 - 55,
				-30 + 5 + 69.909996f).epsilonEquals(sc.get(RFsrFL)
				.getBodyPosition(), 1e-1f));

		sc.get(RHipYawPitch).updateValue((float) Math.PI / 2);
		Kinematics.calculateForward(sc);
		assertTrue(new Vector3f(-55, -45, -30).epsilonEquals(sc.get(
				RHipYawPitch).getBodyPosition(), 1e-1f));
		assertFalse(new Vector3f(-55, -45 - 120, -30 + 5).equals(sc.get(
				RKneePitch).getBodyPosition()));
		for (FrameState fs : sc.getFrames()) {
			System.out.println(fs.getId());
			System.out.println(fs.getBodyPosition());
			System.out.println(fs.getBodyRotation());
		}
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
		Kinematics.calcError(fs1, fs2, err);
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
