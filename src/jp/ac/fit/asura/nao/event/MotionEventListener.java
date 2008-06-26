/*
 * 作成日: 2008/06/23
 */
package jp.ac.fit.asura.nao.event;

import java.util.EventListener;

import jp.ac.fit.asura.nao.motion.Motion;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public interface MotionEventListener extends EventListener {
	public void updatePosture();

	public void updateOdometry(int forward, int left, float turnCCW);

	public void startMotion(Motion motion);

	public void stopMotion(Motion motion);
}
