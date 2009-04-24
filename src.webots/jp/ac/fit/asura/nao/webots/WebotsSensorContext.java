/*
 * 作成日: 2009/04/22
 */
package jp.ac.fit.asura.nao.webots;

import javax.vecmath.Matrix3f;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.PressureSensor;
import jp.ac.fit.asura.nao.SensorContext;
import jp.ac.fit.asura.nao.Switch;
import jp.ac.fit.asura.nao.misc.MathUtils;

/**
 * @author sey
 *
 * @version $Id: $
 *
 */
public class WebotsSensorContext extends SensorContext {
	float[] jointValues;
	float[] jointForces;
	float[] accels;
	float[] gyros;
	float[] forces;
	float[] gps;
	long time;

	public WebotsSensorContext() {
		jointValues = new float[Joint.values().length];
		jointForces = new float[Joint.values().length];
		accels = new float[3];
		gyros = new float[2];
		forces = new float[PressureSensor.values().length];
		gps = new float[3];
	}

	@Override
	public float[] getJointAngles() {
		return jointValues;
	}

	/**
	 * 指定された関節の角度のセンサー値をラジアンで返します.
	 */
	@Override
	public float getJoint(Joint joint) {
		return jointValues[joint.ordinal()];
	}

	@Override
	public float getJointDegree(Joint joint) {
		return MathUtils.toDegrees(getJoint(joint));
	}

	/**
	 * x軸の加速度を返します.
	 *
	 * @return x軸の加速度(m/s^2)
	 */
	@Override
	public float getAccelX() {
		return accels[0];
	}

	@Override
	public float getAccelY() {
		return accels[1];
	}

	@Override
	public float getAccelZ() {
		return accels[2];
	}

	@Override
	public float getGyroX() {
		return gyros[0];
	}

	@Override
	public float getGyroZ() {
		return gyros[1];
	}

	@Override
	public float getForce(PressureSensor ps) {
		return forces[ps.ordinal()];
	}

	@Override
	public float getForce(Joint joint) {
		return jointForces[joint.ordinal()];
	}

	/**
	 * Gpsセンサ値を取得（調整用、本戦では使わないように）
	 *
	 * @return 現在位置のx座標
	 */
	@Override
	public float getGpsX() {
		return gps[0];
	}

	/**
	 * Gpsセンサ値を取得（調整用、本戦では使わないように）
	 *
	 * @return 現在位置のy座標
	 */
	@Override
	public float getGpsY() {
		return gps[1];
	}

	/**
	 * Gpsセンサ値を取得（調整用、本戦では使わないように）
	 *
	 * @return 現在位置のz座標
	 */
	@Override
	public float getGpsZ() {
		return gps[2];
	}

	@Override
	public void getGpsRotation(Matrix3f mat) {
		// disabled in webots6
		mat.setIdentity();
	}

	@Override
	public boolean getSwitch(Switch sw) {
		return false;
	}

	@Override
	public long getTime() {
		return time;
	}
}
