/*
 * 作成日: 2008/07/02
 */
package jp.ac.fit.asura.nao.misc;

import static jp.ac.fit.asura.nao.misc.MatrixUtils.transform;

import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.physical.Nao;
import jp.ac.fit.asura.nao.physical.Nao.Frames;
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
	public void testCamera2bodyCoord() {
		SomaticContext map = new SomaticContext();

		Vector3f v = new Vector3f();
		Coordinates.camera2bodyCoord(v, map);
		System.out.println("camera to body:" + v);
		assertEquals(new Vector3f(0, 250, 38), v);

		Vector3f l = new Vector3f(v);
		Vector3f r = new Vector3f(v);
		Coordinates.body2lSoleCoord(l, map);
		Coordinates.body2rSoleCoord(r, map);
		System.out.println("body to left sole:" + l);
		System.out.println("body to right sole:" + r);
		l.add(r);
		l.scale(0.5f);
		System.out.println(l);

		v = new Vector3f();
		Coordinates.camera2bodyCoord(v, map);
		l.set(v);
		r.set(v);

		Coordinates.body2lSoleCoord(l, map);
		Coordinates.body2rSoleCoord(r, map);
		System.out.println(l);
		System.out.println(r);
		l.add(r);
		l.scale(0.5f);
		System.out.println(l);
		// assertTrue(new Vector3f().epsilonEquals(v, 0.0001f));
	}

	public void testCamera2bodyCoord2() {
		SomaticContext map = new SomaticContext();

		Vector3f v = new Vector3f((float) Math.toRadians(5), (float) Math
				.toRadians(-45), 806);
		Coordinates.polar2carthesian(v, v);
		System.out.println(v);

		Vector3f pitch = new Vector3f(v);
		transform(pitch, Nao.get(Frames.Camera), 0.0f);
		System.out.println(pitch);
		Vector3f yaw = new Vector3f(pitch);
		transform(yaw, Nao.get(Frames.HeadPitch), 0);
		System.out.println(yaw);

		Vector3f body = new Vector3f(yaw);
		transform(body, Nao.get(Frames.HeadYaw), 0);
		System.out.println(body);

		Coordinates.camera2bodyCoord(v, map);
		System.out.println(v);
		Vector3f l = new Vector3f(v);
		Vector3f r = new Vector3f(v);
		Coordinates.body2lSoleCoord(l, map);
		Coordinates.body2rSoleCoord(r, map);
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
	public void testBody2lSoleCoord() {
		Vector3f l = new Vector3f();
		SomaticContext map = new SomaticContext();

		Coordinates.body2lSoleCoord(l, map);
		System.out.println(l);
		assertEquals(new Vector3f(-55, 320, 25), l);
	}

	/**
	 * {@link jp.ac.fit.asura.nao.misc.Coordinates#body2lSoleCoord(javax.vecmath.Vector3f, float, float, float, float, float, float)}
	 * のためのテスト・メソッド。
	 */
	public void testBody2rSoleCoord() {
		Vector3f r = new Vector3f();
		SomaticContext map = new SomaticContext();

		Coordinates.body2rSoleCoord(r, map);
		System.out.println(r);
		assertEquals(new Vector3f(55, 320, 25), r);
	}
}
