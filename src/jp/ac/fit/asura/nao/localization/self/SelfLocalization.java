/*
 * 作成日: 2008/06/23
 */
package jp.ac.fit.asura.nao.localization.self;

import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.event.MotionEventListener;
import jp.ac.fit.asura.nao.event.VisualEventListener;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.vision.VisualContext;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public abstract class SelfLocalization implements RobotLifecycle,
		VisualEventListener, MotionEventListener {
	public abstract void reset();

	public abstract int getX();

	public abstract int getY();

	public abstract float getHeading();

	public abstract int getConfidence();

	public void updateOdometry(int forward, int left, float turnCCW) {
	}

	public void updatePosture() {
	}

	public void startMotion(Motion motion) {
	}

	public void stopMotion(Motion motion) {
	}

	public void updateVision(VisualContext context) {
	}
}
