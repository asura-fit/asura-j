/*
 * 作成日: 2008/07/01
 */
package jp.ac.fit.asura.nao.misc;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.physical.Robot;
import jp.ac.fit.asura.nao.physical.RobotFrame;
import jp.ac.fit.asura.nao.physical.RobotTest;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.FrameState;
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

}
