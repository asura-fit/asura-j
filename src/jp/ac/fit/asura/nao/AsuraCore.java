/*
 * 作成日: 2008/04/12
 */
package jp.ac.fit.asura.nao;

import jp.ac.fit.asura.nao.Camera.CameraType;
import jp.ac.fit.asura.nao.communication.MessageManager;
import jp.ac.fit.asura.nao.glue.SchemeGlue;
import jp.ac.fit.asura.nao.localization.Localization;
import jp.ac.fit.asura.nao.motion.MotorCortex;
import jp.ac.fit.asura.nao.sensation.SomatoSensoryCortex;
import jp.ac.fit.asura.nao.strategy.StrategySystem;
import jp.ac.fit.asura.nao.vision.VisualCortex;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 *
 * @version $Id: AsuraCore.java 713 2008-11-24 06:27:48Z sey $
 *
 */
public class AsuraCore {
	private static final Logger log = Logger.getLogger(AsuraCore.class);

	public abstract static class Controller {
		public abstract void init() throws Exception;

		public abstract void start() throws Exception;

		public abstract void stop() throws Exception;

		public abstract long getTargetVisualCycleTime();

		public abstract void setTargetVisualCycleTime(long targetVisualCycleTime);
	}

	private Effector effector;
	private Sensor sensor;
	private Camera camera;

	private MotorCortex motor;

	private VisualCortex vision;

	private SchemeGlue glue;

	private StrategySystem strategy;

	private RobotContext robotContext;

	private Localization localization;

	private MessageManager communication;

	private SomatoSensoryCortex sensoryCortex;

	private Controller controller;

	public AsuraCore(Effector effector, Sensor sensor, DatagramService ds,
			Camera camera) {
		this.effector = effector;
		this.sensor = sensor;
		this.camera = camera;
		glue = new SchemeGlue();
		motor = new MotorCortex();
		vision = new VisualCortex();
		strategy = new StrategySystem();
		localization = new Localization();
		communication = new MessageManager();
		sensoryCortex = new SomatoSensoryCortex();

		robotContext = new RobotContext(this, sensor, effector, ds, camera,
				motor, vision, glue, strategy, localization, communication,
				sensoryCortex);

		// FIXME
		if (camera.getType() == CameraType.WEBOTS6) {
			controller = new SingleThreadController(robotContext);
			controller.setTargetVisualCycleTime(0);
		} else {
			controller = new MultiThreadController(robotContext);
			controller.setTargetVisualCycleTime(100);
		}
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		log.info("Robot set new id:" + id);
		robotContext.setRobotId(id);
	}

	public void setTeamId(int id) {
		log.info("Team set new id:" + id);
		robotContext.setTeamId(id);
	}

	public void init() throws Exception {
		log.info("Init AsuraCore");

		effector.init();
		sensor.init();
		camera.init();
		controller.init();
	}

	public void start() throws Exception {
		log.info("Start AsuraCore");

		controller.start();
		effector.say("Started.");
	}

	public void stop() throws Exception {
		controller.stop();
	}

	public RobotContext getRobotContext() {
		return robotContext;
	}

	public long getTargetVisualCycleTime() {
		return controller.getTargetVisualCycleTime();
	}

	public void setTargetVisualCycleTime(long targetVisualCycleTime) {
		controller.setTargetVisualCycleTime(targetVisualCycleTime);
	}
}
