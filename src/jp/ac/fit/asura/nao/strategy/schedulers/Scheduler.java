/*
 * 作成日: 2008/06/12
 */
package jp.ac.fit.asura.nao.strategy.schedulers;

import jp.ac.fit.asura.nao.strategy.Task;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public abstract class Scheduler extends Task {
	abstract public void pushQueue(Task task);
	abstract public void clearQueue();

	abstract public void preempt(Task task);
	abstract public void abort();
	
	abstract public Task getCurrentTask();
	abstract public int getTTL();

	abstract public void setTTL(int ttl);
}
