/*
 * 作成日: 2008/05/05
 */
package jp.ac.fit.asura.nao;

import jp.ac.fit.asura.nao.glue.SchemeGlue;
import jp.ac.fit.asura.nao.motion.MotorCortex;
import jp.ac.fit.asura.nao.vision.VisualCortex;

/**
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public class RobotContext extends Context {
	private AsuraCore core;
	private Effector effector;
	private Sensor sensor;
	private VisualCortex vision;
	private MotorCortex motor;
	private SchemeGlue glue;

	/**
	 * 
	 */
	public RobotContext(AsuraCore core, MotorCortex motor, VisualCortex vision,
			Sensor sensor, Effector effector, SchemeGlue glue) {
		this.core = core;
		this.motor = motor;
		this.vision = vision;
		this.sensor = sensor;
		this.effector = effector;
		this.glue = glue;
	}

	public AsuraCore getCore() {
		return core;
	}

	public MotorCortex getMotor() {
		return motor;
	}

	public VisualCortex getVision() {
		return vision;
	}

	/**
	 * @return the effector
	 */
	public Effector getEffector() {
		return effector;
	}

	/**
	 * @return the sensor
	 */
	public Sensor getSensor() {
		return sensor;
	}
}
