/*
 * 作成日: 2008/04/12
 */
package jp.ac.fit.asura.nao;

import java.util.ArrayList;
import java.util.List;

import jp.ac.fit.asura.nao.glue.SchemeGlue;
import jp.ac.fit.asura.nao.localization.Localization;
import jp.ac.fit.asura.nao.motion.MotorCortex;
import jp.ac.fit.asura.nao.strategy.StrategySystem;
import jp.ac.fit.asura.nao.strategy.Team;
import jp.ac.fit.asura.nao.vision.VisualCortex;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class AsuraCore {
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

	/**
	 * 
	 */
	public AsuraCore(RoboCupGameControlData gameControlData, Effector effector,
			Sensor sensor) {
		this.gameControlData = gameControlData;
		this.effector = effector;
		this.sensor = sensor;
		lifecycleListeners = new ArrayList<RobotLifecycle>();
		glue = new SchemeGlue();
		motor = new MotorCortex();
		vision = new VisualCortex();
		strategy = new StrategySystem();
		localization = new Localization();
		lifecycleListeners.add(vision);
		lifecycleListeners.add(localization);
		lifecycleListeners.add(strategy);
		lifecycleListeners.add(motor);
		lifecycleListeners.add(glue);
		robotContext = new RobotContext(this, motor, vision, sensor, effector,
				glue, strategy, gameControlData, localization);
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public void setTeam(Team team) {
		strategy.setTeam(team);
	}

	public void init() {
		time = 0;

		for (RobotLifecycle rl : lifecycleListeners) {
			rl.init(robotContext);
		}
	}

	public void start() {
		robotContext.setFrame(0);
		for (RobotLifecycle rl : lifecycleListeners) {
			rl.start();
		}
	}

	public void run(int ts) {
		time += ts;
		for (RobotLifecycle rl : lifecycleListeners) {
			rl.step();
		}
		robotContext.setFrame(robotContext.getFrame() + 1);
	}
}
