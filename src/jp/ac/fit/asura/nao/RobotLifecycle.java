/*
 * 作成日: 2008/05/05
 */
package jp.ac.fit.asura.nao;

/**
 * @author sey
 * 
 * @version $Id: $
 *
 */
public interface RobotLifecycle {
	public void init(RobotContext rctx);
	public void start();
	public void stop();
}
