/*
 * 作成日: 2008/05/05
 */
package jp.ac.fit.asura.nao;

import jp.ac.fit.asura.nao.communication.MessageManager;
import jp.ac.fit.asura.nao.glue.SchemeGlue;
import jp.ac.fit.asura.nao.localization.Localization;
import jp.ac.fit.asura.nao.motion.MotorCortex;
import jp.ac.fit.asura.nao.sensation.SomatoSensoryCortex;
import jp.ac.fit.asura.nao.strategy.StrategySystem;
import jp.ac.fit.asura.nao.vision.VisualCortex;

/**
 * @author sey
 *
 * @version $Id: RobotContext.java 713 2008-11-24 06:27:48Z sey $
 *
 */
public class RobotContext extends Context {
	private AsuraCore core;
	private Effector effector;
	private Sensor sensor;
	private Camera camera;
	private VisualCortex vision;
	private MotorCortex motor;
	private SchemeGlue glue;
	private StrategySystem strategy;
	private Localization localization;
	private MessageManager communication;
	private DatagramService datagramService;
	private SomatoSensoryCortex sensoryCortex;

	private int robotId = 1;
	private int teamId;

	/**
	 *
	 */
	public RobotContext(AsuraCore core, Sensor sensor, Effector effector,
			DatagramService ds, Camera camera, MotorCortex motor,
			VisualCortex vision, SchemeGlue glue, StrategySystem strategy,
			Localization localization, MessageManager communication,
			SomatoSensoryCortex sensoryCortex) {
		this.core = core;
		this.sensor = sensor;
		this.motor = motor;
		this.vision = vision;
		this.effector = effector;
		this.camera = camera;
		this.glue = glue;
		this.strategy = strategy;
		this.localization = localization;
		this.communication = communication;
		this.datagramService = ds;
		this.sensoryCortex = sensoryCortex;
	}

	public AsuraCore getCore() {
		return core;
	}

	public int getRobotId() {
		return robotId;
	}

	public int getTeamId() {
		return teamId;
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
	 * @return sensor
	 */
	public Sensor getSensor() {
		return sensor;
	}

	/**
	 * @return camera
	 */
	public Camera getCamera() {
		return camera;
	}

	public StrategySystem getStrategy() {
		return strategy;
	}

	/**
	 * @return the localization
	 */
	public Localization getLocalization() {
		return localization;
	}

	/**
	 * @return the communication
	 */
	public MessageManager getCommunication() {
		return communication;
	}

	/**
	 * @return the glue
	 */
	public SchemeGlue getGlue() {
		return glue;
	}

	/**
	 * @return the datagramService
	 */
	public DatagramService getDatagramService() {
		return datagramService;
	}

	/**
	 * @return the sensoryCortex
	 */
	public SomatoSensoryCortex getSensoryCortex() {
		return sensoryCortex;
	}

	protected void setRobotId(int robotId) {
		this.robotId = robotId;
	}

	protected void setTeamId(int teamId) {
		this.teamId = teamId;
	}
}
