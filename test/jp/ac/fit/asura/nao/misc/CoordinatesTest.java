/*
 * 作成日: 2008/07/02
 */
package jp.ac.fit.asura.nao.misc;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.physical.Robot;
import jp.ac.fit.asura.nao.physical.RobotTest;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.SomaticContext;
import junit.framework.TestCase;

/**
 * @author $Author: sey $
 *
 * @version $Id: CoordinatesTest.java 717 2008-12-31 18:16:20Z sey $
 *
 */
public class CoordinatesTest extends TestCase {
	private Robot robot;

	public void testPolar2carthesian() {
		float angleX = (float) Math.toRadians(25);
		float angleY = (float) Math.toRadians(80);
		float d = 10.0f;

		Vector3f v = new Vector3f();
		Coordinates.polar2carthesian(new Vector3f(angleX, angleY, d), v);
		assertEquals(d, v.length(), 0.001);

		Coordinates.carthesian2polar(v, v);
		assertEquals(new Vector3f(angleX, angleY, d), v);

		v = new Vector3f((float) Math.toRadians(5),
				(float) Math.toRadians(-45), 806);
		Coordinates.polar2carthesian(v, v);
		System.out.println(v);
		assertEquals(50.0f, v.x, 10);
		assertEquals(-570, v.y, 10);
		assertEquals(570, v.z, 10);
	}

	public void testCalculateBodyRotation() {
		SomaticContext context = new SomaticContext(robot);
		Matrix3f mat = new Matrix3f();
		mat.set(context.get(Frames.LSole).getBodyRotation());
		mat.set(context.get(Frames.RSole).getBodyRotation());
		Vector3f v = new Vector3f(0, 0, 1);
		mat.transform(v);
		System.out.println(v);
	}

	protected void setUp() throws Exception {
		robot = RobotTest.createRobot();
	}
}
