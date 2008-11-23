/*
 * 作成日: 2008/07/02
 */
package jp.ac.fit.asura.nao.localization.self;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.misc.MatrixUtils;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class GPSLocalization extends SelfLocalization {
	private Sensor sensor;

	public int getConfidence() {
		return 999;
	}

	public float getHeading() {
		Matrix3f mat = new Matrix3f();
		sensor.getGpsRotation(mat);
		Vector3f rpy = new Vector3f();
		MatrixUtils.rot2rpy(mat, rpy);
		return MathUtils.normalizeAngle180((float) Math.toDegrees(-rpy.z
				+ Math.PI));
	}

	public int getX() {
		return (int) (-sensor.getGpsZ() * 1000);
	}

	public int getY() {
		return (int) (-sensor.getGpsX() * 1000);
	}

	public void reset() {
	}

	public void start() {
	}

	public void step() {
	}

	public void stop() {
	}

	public void init(RobotContext rctx) {
		sensor = rctx.getSensor();
	}
}
