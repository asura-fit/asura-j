/*
 * 作成日: 2008/07/02
 */
package jp.ac.fit.asura.nao.localization.self;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.SensorContext;
import jp.ac.fit.asura.nao.VisualFrameContext;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.misc.MatrixUtils;

/**
 * @author sey
 *
 * @version $Id: GPSLocalization.java 717 2008-12-31 18:16:20Z sey $
 *
 */
public class GPSLocalization extends SelfLocalization {
	private int x;
	private int y;
	private int z;
	private float heading;

	public int getConfidence() {
		return 999;
	}

	@Override
	public float getHeading() {
		return heading;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}

	@Override
	public void reset() {
	}

	@Override
	public void start() {
	}

	@Override
	public void step(VisualFrameContext context) {
		SensorContext sensor = context.getMotionFrame().getSensorContext();
		Matrix3f mat = new Matrix3f();
		sensor.getGpsRotation(mat);
		Vector3f pyr = new Vector3f();
		MatrixUtils.rot2pyr(mat, pyr);
		heading = MathUtils.normalizeAngle180(MathUtils.toDegrees(-pyr.y
				+ MathUtils.PIf));
		x = (int) (-sensor.getGpsX() * 1000);
		y = (int) (-sensor.getGpsY() * 1000);
		z = (int) (-sensor.getGpsZ() * 1000);
	}

	@Override
	public void stop() {
	}

	@Override
	public void init(RobotContext rctx) {
	}
}
