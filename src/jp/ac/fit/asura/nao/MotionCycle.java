/*
 * 作成日: 2009/04/22
 */
package jp.ac.fit.asura.nao;

/**
 * @author sey
 *
 * @version $Id: $
 *
 */
public interface MotionCycle extends RobotLifecycle {
	public void step(MotionFrameContext motionFrame);
}
