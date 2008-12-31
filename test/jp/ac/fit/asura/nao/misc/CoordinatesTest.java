/*
 * 作成日: 2008/07/02
 */
package jp.ac.fit.asura.nao.misc;

import static jp.ac.fit.asura.nao.misc.MatrixUtils.transform;

import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.physical.Robot;
import jp.ac.fit.asura.nao.physical.RobotTest;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.FrameState;
import jp.ac.fit.asura.nao.sensation.SomaticContext;
import junit.framework.TestCase;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class CoordinatesTest extends TestCase {
	/**
	 * {@link jp.ac.fit.asura.nao.misc.Coordinates#camera2bodyCoord(javax.vecmath.Vector3f, float, float)}
	 * のためのテスト・メソッド。
	 */
	public void testCamera2bodyCoord() throws Exception {
		SomaticContext map = new SomaticContext(RobotTest.createRobot());
		Kinematics.calculateForward(map);

		Vector3f v = new Vector3f();
		Coordinates.camera2bodyCoord(map, v);
		System.out.println("camera to body:" + v);
		assertEquals(new Vector3f(0, 250, 38), v);

		Vector3f l = new Vector3f(v);
		Vector3f r = new Vector3f(v);
		Coordinates.body2lSoleCoord(map, l);
		Coordinates.body2rSoleCoord(map, r);
		System.out.println("body to left sole:" + l);
		System.out.println("body to right sole:" + r);
		l.add(r);
		l.scale(0.5f);
		System.out.println(l);

		v = new Vector3f();
		Coordinates.camera2bodyCoord(map, v);
		l.set(v);
		r.set(v);

		Coordinates.body2lSoleCoord(map, l);
		Coordinates.body2rSoleCoord(map, r);
		System.out.println(l);
		System.out.println(r);
		l.add(r);
		l.scale(0.5f);
		System.out.println(l);
		// assertTrue(new Vector3f().epsilonEquals(v, 0.0001f));
	}

	public void testCamera2bodyCoord2() throws Exception {
		Robot robot = RobotTest.createRobot();
		Vector3f v = new Vector3f((float) Math.toRadians(5), (float) Math
				.toRadians(-45), 806);
		Coordinates.polar2carthesian(v, v);
		System.out.println(v);
		assertTrue(v.epsilonEquals(new Vector3f(50, -570, 570), 10));

		Coordinates.image2cameraCoord(v, v);
		assertTrue(v.epsilonEquals(new Vector3f(50, -570, -570), 10));

		FrameState cam = new FrameState(robot.get(Frames.Camera));
		transform(v, cam);
		System.out.println(v);
		assertTrue(v.epsilonEquals(new Vector3f(-49.6725f, -539.92804f,
				625.7593f), 5));

		FrameState hp = new FrameState(robot.get(Frames.HeadPitch));
		hp.updateValue(0);
		transform(v, hp);
		System.out.println(v);
		assertTrue(v.epsilonEquals(new Vector3f(-49.6725f, -479.92804f,
				625.7593f), 5));

		FrameState hy = new FrameState(robot.get(Frames.HeadYaw));
		hy.updateValue(0);
		transform(v, hy);
		System.out.println(v);
		assertTrue(v.epsilonEquals(new Vector3f(-49.6725f, -319.92804f,
				605.7593f), 5));

		SomaticContext map = new SomaticContext(robot);
		Kinematics.calculateForward(map);

		v = new Vector3f((float) Math.toRadians(5),
				(float) Math.toRadians(-45), 806);
		Coordinates.polar2carthesian(v, v);
		Coordinates.image2cameraCoord(v, v);
		Coordinates.camera2bodyCoord(map, v);
		System.out.println(v);
		assertTrue(v.epsilonEquals(new Vector3f(-49.6725f, -319.92804f,
				605.7593f), 5));

		// 
		Vector3f l = new Vector3f(v);
		Vector3f r = new Vector3f(v);
		Coordinates.body2lSoleCoord(map, l);
		Coordinates.body2rSoleCoord(map, r);
		System.out.println(l);
		System.out.println(r);
		l.add(r);
		l.scale(0.5f);
		System.out.println(l);
	}

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

	/**
	 * {@link jp.ac.fit.asura.nao.misc.Coordinates#body2rSoleCoord(javax.vecmath.Vector3f, float, float, float, float, float, float)}
	 * のためのテスト・メソッド。
	 */
	public void testBody2lSoleCoord() throws Exception {
		Vector3f l = new Vector3f();
		SomaticContext map = new SomaticContext(RobotTest.createRobot());

		Coordinates.body2lSoleCoord(map, l);
		System.out.println(l);
		assertEquals(new Vector3f(-55, 320, 25), l);
	}

	/**
	 * {@link jp.ac.fit.asura.nao.misc.Coordinates#body2lSoleCoord(javax.vecmath.Vector3f, float, float, float, float, float, float)}
	 * のためのテスト・メソッド。
	 */
	public void testBody2rSoleCoord() throws Exception {
		Vector3f r = new Vector3f();
		SomaticContext map = new SomaticContext(RobotTest.createRobot());

		Coordinates.body2rSoleCoord(map, r);
		System.out.println(r);
		assertEquals(new Vector3f(55, 320, 25), r);
	}
}
