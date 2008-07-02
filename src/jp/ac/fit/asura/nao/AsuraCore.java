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
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class AsuraCore {
	private Logger log = Logger.getLogger(AsuraCore.class);

	private int id;

	private int time;

	private List<RobotLifecycle> lifecycleListeners;

	private Effector effector;
	private Sensor sensor;

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
	public AsuraCore(Effector effector, Sensor sensor, DatagramService ds) {
		this.gameControlData = new RoboCupGameControlData();
		this.effector = effector;
		this.sensor = sensor;
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
		robotContext = new RobotContext(this, sensor, effector, ds, motor,
				vision, glue, strategy, gameControlData, localization,
				communication, sensoryCortex);
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		log.info("Robot set new id:" + id);
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setTeam(Team team) {
		log.info("Robot set new team:" + team);
		strategy.setTeam(team);
	}

	public void init() {
		log.info("Init AsuraCore");
		time = 0;

		for (RobotLifecycle rl : lifecycleListeners) {
			log.debug("init " + rl.toString());
			rl.init(robotContext);
		}
	}

	public void start() {
		log.info("Start AsuraCore");
		robotContext.setFrame(0);
		for (RobotLifecycle rl : lifecycleListeners) {
			log.debug("start " + rl.toString());
			rl.start();
		}
	}

	public void run(int ts) {
		if (log.isTraceEnabled())
			log.trace(String.format("step frame %d at %d ms", robotContext
					.getFrame(), ts));

		time += ts;
		for (RobotLifecycle rl : lifecycleListeners) {
			if (log.isTraceEnabled())
				log.trace("call step " + rl.toString());

			rl.step();
		}
		robotContext.setFrame(robotContext.getFrame() + 1);
	}
}
