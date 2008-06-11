/*
 * 作成日: 2008/05/05
 */
package jp.ac.fit.asura.nao;

import jp.ac.fit.asura.nao.glue.SchemeGlue;
import jp.ac.fit.asura.nao.localization.Localization;
import jp.ac.fit.asura.nao.motion.MotorCortex;
import jp.ac.fit.asura.nao.strategy.StrategySystem;
import jp.ac.fit.asura.nao.vision.VisualCortex;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class RobotContext extends Context {
	private AsuraCore core;
	private Effector effector;
	private Sensor sensor;
	private VisualCortex vision;
	private MotorCortex motor;
	private SchemeGlue glue;
	private StrategySystem strategy;
	private RoboCupGameControlData gameControlData;
	private Localization localization;

	private int frame;

	/**
	 * 
	 */
	public RobotContext(AsuraCore core, MotorCortex motor, VisualCortex vision,
			Sensor sensor, Effector effector, SchemeGlue glue,
			StrategySystem strategy, RoboCupGameControlData gameControlData,
			Localization localization) {
		this.core = core;
		this.motor = motor;
		this.vision = vision;
		this.sensor = sensor;
		this.effector = effector;
		this.glue = glue;
		this.strategy = strategy;
		this.gameControlData = gameControlData;
		this.localization = localization;
	}

	public int getRobotId() {
		return core.getId();
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

	public StrategySystem getStrategy() {
		return strategy;
	}

	/**
	 * @return the gameControlData
	 */
	public RoboCupGameControlData getGameControlData() {
		return gameControlData;
	}

	/**
	 * @return the localization
	 */
	public Localization getLocalization() {
		return localization;
	}

	public int getFrame() {
		return frame;
	}

	public void setFrame(int frame) {
		this.frame = frame;
	}
}
