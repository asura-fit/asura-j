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

import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.vecmath.GMatrix;
import javax.vecmath.GVector;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.motion.MotionUtils;
import jp.ac.fit.asura.nao.physical.Nao.Frames;
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
	/**
	 * 何度も計算して安定しているかをテストする
	 */
	public void testInverseKinematicsStability() {
		long l = System.currentTimeMillis();
		int FACTOR = 100000;
		for (int i = 0; i < FACTOR; i++) {
			if (i % 1000 == 0)
				System.out.println(i);
			testInverseKinematics();
		}
		long l2 = System.currentTimeMillis();
		System.out.println("average ms:" + (l2 - l) / (double) FACTOR);
	}

	/**
	 * よさそうなSCALEとLANGLEのパラメータを探索する
	 */
	public void testInverseKinematicsStability2() {
		SomaticContext sc = new SomaticContext();

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
						assertTrue(fs.getRobotPosition().toString() + "\n"
								+ sc.get(RAnkleRoll).getRobotPosition(), fs
								.getRobotPosition().epsilonEquals(
										sc.get(RAnkleRoll).getRobotPosition(),
										1e-1f));

						assertTrue(fs.getRobotRotation().toString() + "\n"
								+ sc.get(RAnkleRoll).getRobotRotation(), fs
								.getRobotRotation().epsilonEquals(
										sc.get(RAnkleRoll).getRobotRotation(),
										1e-1f));

						for (FrameState f : sc.getFrames()) {
							if (f.getId().isJoint()) {
								assertTrue(f.getId() + " out of range:"
										+ f.getAngle(), MotionUtils.isInRange(f
										.getId().toJoint(), f.getAngle()));
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
	public void testInverseKinematics() {
		SomaticContext sc = new SomaticContext();

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
			assertTrue(fs.getRobotPosition().toString() + "\n"
					+ sc.get(RAnkleRoll).getRobotPosition(), fs
					.getRobotPosition().epsilonEquals(
							sc.get(RAnkleRoll).getRobotPosition(), 1e-1f));

			assertTrue(fs.getRobotRotation().toString() + "\n"
					+ sc.get(RAnkleRoll).getRobotRotation(), fs
					.getRobotRotation().epsilonEquals(
							sc.get(RAnkleRoll).getRobotRotation(), 1e-1f));

			for (FrameState f : sc.getFrames()) {
				if (f.getId().isJoint()) {
					assertTrue(f.getId() + " out of range:" + f.getAngle(),
							MotionUtils.isInRange(f.getId().toJoint(), f
									.getAngle()));
				}
			}
		}
		long l2 = System.currentTimeMillis();
		double tries = n / (double) FACTOR;
		System.out.println((l2 - l) / (double) FACTOR);
		System.out.println(tries);
		System.out.println("worst" + worst);
	}

	public void testForwardKinematics() {
		// 順運動学のテスト
		// 予め計算した値と一致するかをテストする
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
		for (FrameState fs : sc.getFrames()) {
			System.out.println(fs.getId());
			System.out.println(fs.getRobotPosition());
			System.out.println(fs.getRobotRotation());
		}
	}

	public void testCalcError() {
		// 誤差計算のテスト?
		// あまり意味なし
		GVector err = new GVector(6);
		// target
		FrameState fs1 = new FrameState(Body);
		fs1.getRobotRotation().set(
				new float[] { 0.5f, 0.50000006f, 0.7071068f, 0.50000006f, 0.5f,
						-0.7071068f, -0.7071068f, 0.7071068f, -4.371139E-8f });
		assertEquals(1f, fs1.getRobotRotation().determinant(), 1e-3);

		// current
		FrameState fs2 = new FrameState(Body);
		fs2.getRobotRotation().set(
				new float[] { -0.18696824f, -0.5456833f, -0.81686765f,
						0.67815393f, -0.6733057f, 0.29456228f, -0.7107393f,
						-0.49888813f, 0.49594402f });
		assertEquals(1f, fs2.getRobotRotation().determinant(), 1e-3);
		Kinematics.calcError(fs1, fs2, err);
		assertTrue(err.toString(), err.normSquared() < 1e10);
	}

	// 関節角度を可動範囲内でランダムにセット
	private void setAngleRandom(SomaticContext sc) {
		for (FrameState fs : sc.getFrames()) {
			if (fs.getId().isJoint()) {
				float max = MotionUtils.getMaxAngle(fs.getId().toJoint());
				float min = MotionUtils.getMinAngle(fs.getId().toJoint());
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
