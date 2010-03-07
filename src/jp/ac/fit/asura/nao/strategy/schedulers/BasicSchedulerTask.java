/*
 * 作成日: 2008/06/12
 */
package jp.ac.fit.asura.nao.strategy.schedulers;

import java.util.LinkedList;
import java.util.Queue;

import javax.management.ImmutableDescriptor;

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

	/**
	 * タスクの切替処理.
	 *
	 * 実行キューに待ち状態のタスクがある場合は切り替える.
	 *
	 * キューが空の場合は{@link #fillQueue(StrategyContext)} を呼び出し，次のタスクを決定する.
	 *
	 * @param context
	 */
	private void switchTask(StrategyContext context) {
		Task next = null;
		// 実行キューからタスクを取り出す
		while (!queue.isEmpty()) {
			Task task = queue.removeFirst();

			assert task != null;
			if (task.canExecute(context)) {
				next = task;
				break;
			}
		}

		// 次のタスクがなければfillQueue
		if (next == null) {
			fillQueue(context);
			assert !queue.isEmpty();
			next = queue.removeFirst();
		}

		// 現在実行中のタスクを終了
		if (currentTask != null) {
			currentTask.leave(context);
		}

		// 次のタスクに切り替える． TTLの初期値は1
		currentTask = next;
		timeToLive = 1;
		log.info("Scheduler: enter task " + currentTask.getName());
		currentTask.enter(context);
	}

	/**
	 * このスケジューラのキューにTaskを追加します.
	 *
	 * @param task
	 */
	public void pushQueue(Task task) {
		assert task != null;
		queue.add(task);
	}

	/**
	 * このスケジューラのキューに入っているタスクをすべて削除します.
	 *
	 * すでに実行中のタスクはそのまま継続されます.
	 */
	public void clearQueue() {
		queue.clear();
	}

	/**
	 * 現在実行しているタスクを中断します. 実行キューはそのまま.
	 *
	 * @see #clearQueue()
	 */
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

	/**
	 * 実行キューを返します.
	 *
	 * このキューを変更するべきではありません.
	 */
	@Override
	public Queue<Task> getQueue() {
		return queue;
	}

	/**
	 * 現在実行しているタスクを返します.
	 *
	 * @return 実行中のTask
	 */
	public Task getCurrentTask() {
		return currentTask;
	}

	/**
	 * 現在実行しているタスクの残り時間(Time to live)を返します.
	 *
	 * @return
	 */
	public int getTTL() {
		return timeToLive;
	}

	/**
	 * 現在実行しているタスクの残り時間を設定します.
	 *
	 * 0に設定した場合は，タスクの実行を中断します.
	 */
	public void setTTL(int ttl) {
		timeToLive = ttl;
	}

	abstract protected void fillQueue(StrategyContext context);
}
