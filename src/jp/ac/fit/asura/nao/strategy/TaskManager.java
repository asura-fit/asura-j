/*
 * 作成日: 2008/05/18
 */
package jp.ac.fit.asura.nao.strategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.strategy.actions.InitialTask;
import jp.ac.fit.asura.nao.strategy.actions.LookAroundTask;
import jp.ac.fit.asura.nao.strategy.actions.ShootTask;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;
import jp.ac.fit.asura.nao.strategy.permanent.GetUpTask;
import jp.ac.fit.asura.nao.strategy.schedulers.ExperimentalScheduler;
import jp.ac.fit.asura.nao.strategy.schedulers.GoalieStrategyTask;
import jp.ac.fit.asura.nao.strategy.schedulers.StrategySchedulerTask;
import jp.ac.fit.asura.nao.strategy.schedulers.StrikerStrategyTask;
import jp.ac.fit.asura.nao.strategy.tactics.FindBallTask;
import jp.ac.fit.asura.nao.strategy.tactics.ApproachBallTask;
import jp.ac.fit.asura.nao.strategy.tactics.GoalieKeepTask;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class TaskManager {
	private Logger log = Logger.getLogger(TaskManager.class);

	private Map<String, Task> tasks;

	private boolean initialized;

	public TaskManager() {
		tasks = new HashMap<String, Task>(64);
		initialized = false;
	}

	public void init(RobotContext context) {
		registerTasks();

		initialized = true;

		for (Task task : tasks.values())
			task.init(context);
	}

	private void registerTasks() {
		add(new BallTrackingTask());
		add(new GetUpTask());

		add(new FindBallTask());
		add(new ApproachBallTask());
		add(new GoalieKeepTask());
		add(new LookAroundTask());
		add(new InitialTask());
		add(new ShootTask());

		add(new StrategySchedulerTask());
		add(new GoalieStrategyTask());
		add(new StrikerStrategyTask());

		add(new ExperimentalScheduler());
	}

	public void add(Task task) {
		assert !tasks.containsKey(task.getName());
		assert !initialized;
		tasks.put(task.getName(), task);
	}

	public Task find(String name) {
		if (!tasks.containsKey(name)) {
			log.error("TaskManager: task not found " + name);
			return null;
		}
		return tasks.get(name);
	}

	public Collection<Task> values() {
		return tasks.values();
	}
}
