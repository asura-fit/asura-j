/*
 * 作成日: 2008/07/01
 */
package jp.ac.fit.asura.nao.misc;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.physical.Nao;
import jp.ac.fit.asura.nao.physical.RobotFrame;
import jp.ac.fit.asura.nao.physical.Nao.Frames;
import junit.framework.TestCase;

/**
 * @author sey
 *
 * @version $Id$
 *
 */
public class MatrixUtilsTest extends TestCase {

	/**
	 * {@link jp.ac.fit.asura.nao.misc.MatrixUtils#transform(javax.vecmath.Vector3f, javax.vecmath.AxisAngle4f, float, javax.vecmath.Vector3f)}
	 * のためのテスト・メソッド。
	 */
	public void testTransform() {
		Vector3f v = new Vector3f();
		RobotFrame fr = new RobotFrame(null);
		fr.getTranslation().set(new Vector3f());
		fr.getAxis().set(new AxisAngle4f());
		MatrixUtils.transform(v, fr, 0.0f);
		assertTrue(new Vector3f().epsilonEquals(v, 0.0001f));

		v = new Vector3f();
		MatrixUtils.transform(v, Nao.get(Frames.HeadYaw), 1.0f);
		assertTrue(new Vector3f(0, 160, -20).epsilonEquals(v, 0.0001f));
	}

	/**
	 * {@link jp.ac.fit.asura.nao.misc.MatrixUtils#inverseTransform(javax.vecmath.Vector3f, javax.vecmath.AxisAngle4f, float, javax.vecmath.Vector3f)}
	 * のためのテスト・メソッド。
	 */
	public void testInverseTransform() {
		Vector3f v = new Vector3f();

		MatrixUtils.inverseTransform(v, Nao.get(Frames.HeadYaw), 0.0f);
		assertTrue(new Vector3f(0, -160, 20).epsilonEquals(v, 0.0001f));

		v = new Vector3f(10, 10, 10);
		MatrixUtils.transform(v, Nao.get(Frames.HeadYaw), 1.0f);
		MatrixUtils.inverseTransform(v, Nao.get(Frames.HeadYaw), 1.0f);

		assertTrue(new Vector3f(10, 10, 10).epsilonEquals(v, 0.0001f));
	}

}
