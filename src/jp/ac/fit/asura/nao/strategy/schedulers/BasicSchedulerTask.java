/*
 * 作成日: 2008/06/12
 */
package jp.ac.fit.asura.nao.strategy.schedulers;

import java.util.LinkedList;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;

import org.apache.log4j.Logger;

/**
 *
 * 基本的な処理を実装しているスケジューラ.
 *
 * Queueを元にタスクを実行/切り替えします.
 *
 * @author $Author: sey $
 *
 * @version $Id: BasicSchedulerTask.java 709 2008-11-23 07:40:31Z sey $
 *
 */
public abstract class BasicSchedulerTask extends Scheduler {
	private Logger log = Logger.getLogger(BasicSchedulerTask.class);

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
		log.info("Scheduler: enter task " + currentTask.getName());
		currentTask.enter(context);
	}

	public void pushQueue(Task task) {
		assert task != null;
		queue.add(task);
	}

	public void clearQueue() {
		queue.clear();
	}

	public void abort() {
		timeToLive = 0;
	}

	/**
	 * 実行中のタスクを停止し，キューにTaskを割り込ませます.
	 */
	public void preempt(Task task) {
		abort();
		queue.clear();
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
