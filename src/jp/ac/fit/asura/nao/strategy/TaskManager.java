/*
 * 作成日: 2008/05/18
 */
package jp.ac.fit.asura.nao.strategy;

import java.util.HashMap;
import java.util.Map;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class TaskManager implements RobotLifecycle {
	private RobotContext context;
	private Map<String, Task> tasks;

	public TaskManager() {
		tasks = new HashMap<String, Task>(64);
	}

	public void init(RobotContext rctx) {
		this.context = rctx;
		tasks.clear();
		add(new BallTrackingTask());
	}

	public void start() {
		tasks.get("BallTracking").enter(context);
	}

	public void stop() {
		tasks.get("BallTracking").leave(context);
	}

	public void step() {
		tasks.get("BallTracking").step(context);
	}

	public void add(Task task) {
		assert !tasks.containsKey(task.getName());
		tasks.put(task.getName(), task);
	}

	public Task find(String name) {
		return tasks.get(name);
	}
}
