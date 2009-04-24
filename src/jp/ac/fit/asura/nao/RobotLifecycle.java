/*
 * 作成日: 2008/05/05
 */
package jp.ac.fit.asura.nao;

/**
 * @author sey
 *
 * @version $Id: RobotLifecycle.java 606 2008-06-11 12:55:14Z sey $
 *
 */
public interface RobotLifecycle {
	public void init(RobotContext rctx);

	public void start();

	public void stop();
}
