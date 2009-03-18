/*
 * 作成日: 2008/05/18
 */
package jp.ac.fit.asura.nao.strategy;

import jp.ac.fit.asura.nao.RobotContext;

/**
 * @author $Author: sey $
 * 
 * @version $Id: Task.java 716 2008-12-30 09:32:53Z sey $
 * 
 */
public abstract class Task {
	abstract public String getName();

	/**
	 * StrategySystemの起動時に呼ばれるメソッド.
	 * @param context
	 */
	public void init(RobotContext context) {
	}

	/**
	 * タスクが実行中かどうかにかかわらず、各フレームの先頭で呼び出されるメソッド.
	 * @param context
	 */
	public void before(StrategyContext context) {
	}
	
	/**
	 * タスクが実行中かどうかにかかわらず、各フレームの最後に呼び出されるメソッド.
	 * @param context
	 */
	public void after(StrategyContext context) {
	}
	
	/**
	 * タスクが実行中になるときに呼ばれるメソッド.
	 * @param context
	 */
	public void enter(StrategyContext context) {
	}

	/**
	 * タスクが実行中から外れるときに呼ばれるメソッド.
	 * @param context
	 */
	public void leave(StrategyContext context) {
	}
	
	/**
	 * タスクが実行中に呼ばれるメソッド.
	 * @param context
	 */
	public void continueTask(StrategyContext context) {
	}

	/**
	 * タスクが実行可能かどうかを返すメソッド.
	 * @param context
	 */
	public boolean canExecute(StrategyContext context) {
		return true;
	}

}
