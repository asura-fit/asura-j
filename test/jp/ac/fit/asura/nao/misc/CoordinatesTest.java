/*
 * 作成日: 2008/07/02
 */
package jp.ac.fit.asura.nao.misc;

import static jp.ac.fit.asura.nao.misc.MatrixUtils.transform;

import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.misc.PhysicalConstants.Nao;
import junit.framework.TestCase;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class CoordinatesTest extends TestCase {

	/**
	 * {@link jp.ac.fit.asura.nao.misc.Coordinates#camera2bodyCoord(javax.vecmath.Vector3f, float, float)}
	 * のためのテスト・メソッド。
	 */
	public void testCamera2bodyCoord() {
		Vector3f v;
		v = Coordinates.camera2bodyCoord(new Vector3f(), 0, 0);
		System.out.println(v);
		Vector3f l = Coordinates.body2lSoleCoord(v, 0, 0, 0, 0, 0, 0);
		Vector3f r = Coordinates.body2rSoleCoord(v, 0, 0, 0, 0, 0, 0);
		System.out.println(l);
		System.out.println(r);
		l.add(r);
		l.scale(0.5f);
		System.out.println(l);

		v = Coordinates.camera2bodyCoord(v, 0, 0);
		l = Coordinates.body2lSoleCoord(v, 0, 0, 0, 0, 0, 0);
		r = Coordinates.body2rSoleCoord(v, 0, 0, 0, 0, 0, 0);
		System.out.println(l);
		System.out.println(r);
		l.add(r);
		l.scale(0.5f);
		System.out.println(l);
		// assertTrue(new Vector3f().epsilonEquals(v, 0.0001f));
	}

	public void testCamera2bodyCoord2() {
		Vector3f v = new Vector3f((float) Math.toRadians(5), (float) Math
				.toRadians(-45), 806);
		v = Coordinates.polar2carthesian(v);
		System.out.println(v);
		assertEquals(50.0f, v.x, 10);
		assertEquals(-570, v.y, 10);
		assertEquals(570, v.z, 10);

		Vector3f pitch = transform(v, Nao.rCamera2headPitch, 0.0f,
				Nao.tCamera2headPitch);
		System.out.println(pitch);
		Vector3f yaw = transform(pitch, Nao.rHeadPitch2yaw, 0,
				Nao.tHeadPitch2yaw);
		System.out.println(yaw);
		Vector3f body = transform(yaw, Nao.rHeadYaw2body, 0, Nao.tHeadYaw2body);
		System.out.println(body);

		v = Coordinates.camera2bodyCoord(v, 0, 0);
		System.out.println(v);
		Vector3f l = Coordinates.body2lSoleCoord(v, 0, 0, 0, 0, 0, 0);
		Vector3f r = Coordinates.body2rSoleCoord(v, 0, 0, 0, 0, 0, 0);
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

		Vector3f v = Coordinates.polar2carthesian(new Vector3f(angleX, angleY,
				d));
		assertEquals(d, v.length(), 0.001);
		assertEquals(new Vector3f(angleX, angleY, d), Coordinates
				.carthesian2polar(v));
	}

	/**
	 * {@link jp.ac.fit.asura.nao.misc.Coordinates#body2rSoleCoord(javax.vecmath.Vector3f, float, float, float, float, float, float)}
	 * のためのテスト・メソッド。
	 */
	public void testBody2rSoleCoord() {
		fail("まだ実装されていません");
	}

	/**
	 * {@link jp.ac.fit.asura.nao.misc.Coordinates#body2lSoleCoord(javax.vecmath.Vector3f, float, float, float, float, float, float)}
	 * のためのテスト・メソッド。
	 */
	public void testBody2lSoleCoord() {
		fail("まだ実装されていません");
	}

}
