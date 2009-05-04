/*
 * 作成日: 2008/07/01
 */
package jp.ac.fit.asura.nao.misc;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.physical.Robot;
import jp.ac.fit.asura.nao.physical.RobotFrame;
import jp.ac.fit.asura.nao.physical.RobotTest;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.FrameState;
import jp.ac.fit.asura.vecmathx.GMatrix;
import jp.ac.fit.asura.vecmathx.GVector;
import junit.framework.TestCase;

/**
 * @author sey
 *
 * @version $Id: MatrixUtilsTest.java 717 2008-12-31 18:16:20Z sey $
 *
 */
public class MatrixUtilsTest extends TestCase {

	/**
	 * {@link jp.ac.fit.asura.nao.misc.MatrixUtils#transform(javax.vecmath.Vector3f, javax.vecmath.AxisAngle4f, float, javax.vecmath.Vector3f)}
	 * のためのテスト・メソッド。
	 */
	public void testTransform() throws Exception {
		Vector3f v = new Vector3f();
		RobotFrame fr = new RobotFrame(null);
		fr.getTranslation().set(new Vector3f());
		fr.getAxis().set(new AxisAngle4f());
		FrameState fs = new FrameState(fr);
		fs.updateValue(0);
		MatrixUtils.transform(v, fs);
		assertTrue(new Vector3f().epsilonEquals(v, 0.0001f));

		FrameState fs2 = new FrameState(RobotTest.createRobot().get(
				Frames.HeadYaw));
		fs2.updateValue(1.0f);
		v = new Vector3f();
		MatrixUtils.transform(v, fs2);
		assertTrue(new Vector3f(0, 160, -20).epsilonEquals(v, 0.0001f));
	}

	/**
	 * {@link jp.ac.fit.asura.nao.misc.MatrixUtils#inverseTransform(javax.vecmath.Vector3f, javax.vecmath.AxisAngle4f, float, javax.vecmath.Vector3f)}
	 * のためのテスト・メソッド。
	 */
	public void testInverseTransform() throws Exception {
		Robot robot = RobotTest.createRobot();
		Vector3f v = new Vector3f();

		FrameState fs1 = new FrameState(robot.get(Frames.HeadYaw));
		fs1.updateValue(0.0f);
		MatrixUtils.inverseTransform(v, fs1);
		assertTrue(new Vector3f(0, -160, 20).epsilonEquals(v, 0.0001f));

		v = new Vector3f(10, 10, 10);
		fs1.updateValue(1.0f);
		MatrixUtils.transform(v, fs1);
		MatrixUtils.inverseTransform(v, fs1);

		assertTrue(new Vector3f(10, 10, 10).epsilonEquals(v, 0.0001f));
	}

	public void testRpy2rot() throws Exception {
		Vector3f ypr = new Vector3f(MathUtils.toRadians(0), MathUtils
				.toRadians(0), MathUtils.toRadians(10));
		Matrix3f rot = new Matrix3f();
		MatrixUtils.pyr2rot(ypr, rot);
		System.out.println(rot);
		Vector3f ypr2 = new Vector3f();
		MatrixUtils.rot2pyr(rot, ypr2);
		System.out.println(ypr);
		System.out.println(ypr2);
		assertEquals(ypr, ypr2, MathUtils.EPSf);
	}

	public void testRpy2rot2() throws Exception {
		Vector3f rpy = new Vector3f(MathUtils.toRadians(0), MathUtils
				.toRadians(30), MathUtils.toRadians(0));
		Matrix3f rot = new Matrix3f();
		MatrixUtils.pyr2rot(rpy, rot);

		Vector3f v = new Vector3f(5, 10, 0);
		rot.transform(v);
		System.out.println(v);
		assertEquals(new Vector3f(5, 8.660254f, 5.0f), v, MathUtils.EPSf);

		rpy = new Vector3f(MathUtils.toRadians(30), MathUtils.toRadians(0),
				MathUtils.toRadians(0));
		MatrixUtils.pyr2rot(rpy, rot);

		v = new Vector3f(0, 10, 0);
		rot.transform(v);
		System.out.println(v);
		assertEquals(new Vector3f(-5, 8.660254f, 0), v, MathUtils.EPSf);
	}

	public void testRot2omega() throws Exception {
		Vector3f pyr = new Vector3f(MathUtils.toRadians(0), MathUtils
				.toRadians(20), MathUtils.toRadians(20));
		Matrix3f rot = new Matrix3f();
		MatrixUtils.pyr2rot(pyr, rot);
		System.out.println(rot);

		Vector3f omega = new Vector3f();
		MatrixUtils.rot2omega(rot, omega);
		Matrix3f a = new Matrix3f();
		a.set(new AxisAngle4f(omega, omega.length()));
		System.out.println(pyr);
		System.out.println(omega);
		System.out.println(a);

		MatrixUtils.rot2pyr(rot, pyr);
		System.out.println(pyr);
	}

	public void testSolve() throws Exception {
		GMatrix mat = new GMatrix(3, 3, new double[] { -1, 1, 2, 3, -1, 1, -1,
				3, 4 });
		GVector x2 = new GVector(new double[] { 2, 6, 4 });
		GVector b = new GVector(3);
		b.mul(mat, x2);

		GVector x = new GVector(3);
		MatrixUtils.solve(mat, b, x);
		assertEquals(x2, x, 0.0125f);
		//
		// MatrixUtils.solve2(mat, b, x);
		// assertEquals(x2,x, 0.0125f);
	}

	private void assertEquals(GVector expected, GVector actual, float delta) {
		assertTrue("Expected " + expected.toString() + " but actual "
				+ actual.toString(), expected.epsilonEquals(actual, delta));
	}

	private void assertEquals(Vector3f expected, Vector3f actual, float delta) {
		assertTrue("Expected " + expected.toString() + " but actual "
				+ actual.toString(), expected.epsilonEquals(actual, delta));
	}
}
