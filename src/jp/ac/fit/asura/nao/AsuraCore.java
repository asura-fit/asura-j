/*
 * 作成日: 2008/04/12
 */
package jp.ac.fit.asura.nao;

import java.util.ArrayList;
import java.util.List;

import jp.ac.fit.asura.nao.glue.SchemeGlue;
import jp.ac.fit.asura.nao.motion.MotorCortex;
import jp.ac.fit.asura.nao.strategy.TaskManager;
import jp.ac.fit.asura.nao.vision.VisualCortex;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class AsuraCore {
	public enum Team {
		Red, Blue
	};

	private int id;

	private Team team;

	private int time;

	private List<RobotLifecycle> lifecycleListeners;

	private Effector effector;
	private Sensor sensor;

	private MotorCortex motor;

	private VisualCortex vision;

	private SchemeGlue glue;

	private TaskManager taskManager;

	private RobotContext robotContext;

	/**
	 * 
	 */
	public AsuraCore(Effector effector, Sensor sensor) {
		this.effector = effector;
		this.sensor = sensor;
		lifecycleListeners = new ArrayList<RobotLifecycle>();
		glue = new SchemeGlue();
		motor = new MotorCortex();
		vision = new VisualCortex();
		taskManager = new TaskManager();
		lifecycleListeners.add(vision);
		lifecycleListeners.add(taskManager);
		lifecycleListeners.add(motor);
		lifecycleListeners.add(glue);
		robotContext = new RobotContext(this, motor, vision, sensor, effector,
				glue);
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @param team
	 *            the team to set
	 */
	public void setTeam(Team team) {
		this.team = team;
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
