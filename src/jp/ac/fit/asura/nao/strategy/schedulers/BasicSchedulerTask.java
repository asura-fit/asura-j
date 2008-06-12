/*
 * 作成日: 2008/06/12
 */
package jp.ac.fit.asura.nao.strategy.schedulers;

import java.util.LinkedList;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public abstract class BasicSchedulerTask extends Scheduler {
	protected LinkedList<Task> queue;
	protected int timeToLive;
	protected Task currentTask;

	public BasicSchedulerTask() {
		queue = new LinkedList<Task>();
	}

	public void enter(StrategyContext context) {
		queue.clear();
		timeToLive = 0;
		currentTask = null;
	}

	public void init(RobotContext context) {
	}

	public void leave(StrategyContext context) {
		queue.clear();
		timeToLive = 0;
		currentTask = null;
	}

	public void continueTask(StrategyContext context) {
		if (timeToLive <= 0) {
			switchTask(context);
			return;
		}
		currentTask.continueTask(context);
		timeToLive--;
	}

	private void switchTask(StrategyContext context) {
		Task next = null;
		while (!queue.isEmpty()) {
			Task task = queue.removeFirst();

			assert task != null;
			if (task.canExecute(context)) {
				next = task;
				break;
			}
		}

		if (next == null) {
			fillQueue(context);
			assert !queue.isEmpty();
			next = queue.removeFirst();
		}

		if (currentTask != null) {
			currentTask.leave(context);
		}

		currentTask = next;
		timeToLive = 1;
		System.out.println("Scheduler: enter new task " + currentTask.getName());
		currentTask.enter(context);
	}

	public void pushQueue(Task task) {
		assert task != null;
		queue.push(task);
	}

	public void clearQueue() {
		queue.clear();
	}

	public void abort() {
		timeToLive = 0;
	}

	public void preempt(Task task) {
		abort();
		queue.addFirst(task);
	}

	public Task getCurrentTask() {
		return currentTask;
	}

	public int getTTL() {
		return timeToLive;
	}

	public void setTTL(int ttl) {
		timeToLive = ttl;
	}

	abstract protected void fillQueue(StrategyContext context);

}
