/*
 * 作成日: 2008/05/18
 */
package jp.ac.fit.asura.nao.strategy;

import java.util.HashMap;
import java.util.Map;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class TaskManager {
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
	}

	public void add(Task task) {
		assert !tasks.containsKey(task.getName());
		assert !initialized;
		tasks.put(task.getName(), task);
	}

	public Task find(String name) {
		return tasks.get(name);
	}
}
