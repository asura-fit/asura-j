/*
 * 作成日: 2008/07/02
 */
package jp.ac.fit.asura.nao.localization.self;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.Sensor;

/**
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public class GPSLocalization extends SelfLocalization {
	private Sensor sensor;

	public int getConfidence() {
		return 999;
	}

	public float getHeading() {
		return (float) Math.toDegrees(-sensor.getGpsHeading());
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
