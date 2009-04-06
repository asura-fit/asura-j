/*
 * 作成日: 2008/04/12
 */
package jp.ac.fit.asura.nao;

import java.util.ArrayList;
import java.util.List;

import jp.ac.fit.asura.nao.communication.MessageManager;
import jp.ac.fit.asura.nao.communication.RoboCupGameControlData;
import jp.ac.fit.asura.nao.glue.SchemeGlue;
import jp.ac.fit.asura.nao.localization.Localization;
import jp.ac.fit.asura.nao.motion.MotorCortex;
import jp.ac.fit.asura.nao.sensation.SomatoSensoryCortex;
import jp.ac.fit.asura.nao.strategy.StrategySystem;
import jp.ac.fit.asura.nao.strategy.Team;
import jp.ac.fit.asura.nao.vision.VisualCortex;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 *
 * @version $Id: AsuraCore.java 713 2008-11-24 06:27:48Z sey $
 *
 */
public class AsuraCore {
	private Logger log = Logger.getLogger(AsuraCore.class);

	private int time;

	private List<RobotLifecycle> lifecycleListeners;

	private Effector effector;
	private Sensor sensor;
	private Camera camera;

	private MotorCortex motor;

	private VisualCortex vision;

	private SchemeGlue glue;

	private StrategySystem strategy;

	private RobotContext robotContext;

	private RoboCupGameControlData gameControlData;

	private Localization localization;

	private MessageManager communication;

	private SomatoSensoryCortex sensoryCortex;

	/**
	 *
	 */
	public AsuraCore(Effector effector, Sensor sensor, DatagramService ds, Camera camera) {
		this.gameControlData = new RoboCupGameControlData();
		this.effector = effector;
		this.sensor = sensor;
		this.camera = camera;
		lifecycleListeners = new ArrayList<RobotLifecycle>();
		glue = new SchemeGlue();
		motor = new MotorCortex();
		vision = new VisualCortex();
		strategy = new StrategySystem();
		localization = new Localization();
		communication = new MessageManager();
		sensoryCortex = new SomatoSensoryCortex();
		lifecycleListeners.add(communication);
		lifecycleListeners.add(sensoryCortex);
		lifecycleListeners.add(vision);
		lifecycleListeners.add(localization);
		lifecycleListeners.add(strategy);
		lifecycleListeners.add(motor);
		lifecycleListeners.add(glue);
		robotContext = new RobotContext(sensor, effector, ds,camera, motor,
				vision, glue, strategy, gameControlData, localization,
				communication, sensoryCortex);
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		log.info("Robot set new id:" + id);
		robotContext.setRobotId(id);
	}

	public void setTeam(Team team) {
		log.info("Robot set new team:" + team);
		strategy.setTeam(team);
	}

	public void init() {
		log.info("Init AsuraCore");
		time = 0;

		effector.init();
		sensor.init();
		camera.init();
		for (RobotLifecycle rl : lifecycleListeners) {
			log.debug("init " + rl.toString());
			try {
				rl.init(robotContext);
			} catch (RuntimeException e) {
				log.error("", e);
				assert false;
			}
		}
	}

	public void start() {
		log.info("Start AsuraCore");
		robotContext.setFrame(0);
		for (RobotLifecycle rl : lifecycleListeners) {
			log.debug("start " + rl.toString());
			try {
				rl.start();
			} catch (RuntimeException e) {
				log.error("", e);
				assert false;
			}
		}
	}

	public void run(int ts) {
		if (log.isTraceEnabled())
			log.trace(String.format("step frame %d at %d ms", robotContext
					.getFrame(), ts));

		time += ts;
		effector.before();
		sensor.before();
		camera.before();
		for (RobotLifecycle rl : lifecycleListeners) {
			if (log.isTraceEnabled())
				log.trace("call step " + rl.toString());

			try {
				rl.step();
			} catch (RuntimeException e) {
				log.error("", e);
				assert false;
			}
		}
		camera.after();
		sensor.after();
		effector.after();
		robotContext.setFrame(robotContext.getFrame() + 1);
	}

	public RobotContext getRobotContext() {
		return robotContext;
	}
}
