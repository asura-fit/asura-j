/*
 * 作成日: 2008/06/12
 */
package jp.ac.fit.asura.nao.strategy.schedulers;

import jp.ac.fit.asura.nao.strategy.Task;

/**
 *
 * スケジューラの定義とインターフェイス.
 *
 * @author $Author: sey $
 *
 * @version $Id: Scheduler.java 709 2008-11-23 07:40:31Z sey $
 *
 */
public abstract class Scheduler extends Task {
	/**
	 * このスケジューラのキューにTaskを追加します.
	 *
	 * @param task
	 */
	abstract public void pushQueue(Task task);

	/**
	 * このスケジューラのキューに入っているタスクをすべて削除します.
	 *
	 * すでに実行中のタスクはそのまま継続されます.
	 */
	abstract public void clearQueue();

	/**
	 * 実行中のタスクを停止し，キューにTaskを割り込ませます.
	 */
	abstract public void preempt(Task task);

	/**
	 * 実行中のタスクを停止します.
	 */
	abstract public void abort();

	/**
	 * 現在実行しているタスクを返します.
	 *
	 * @return 実行中のTask
	 */
	abstract public Task getCurrentTask();

	/**
	 * 現在実行しているタスクの残り時間(Time to live)を返します.
	 *
	 * @return
	 */
	abstract public int getTTL();

	/**
	 * 現在実行しているタスクの残り時間を設定します.
	 *
	 * @param ttl
	 *            新しいTTL
	 */
	abstract public void setTTL(int ttl);
}
