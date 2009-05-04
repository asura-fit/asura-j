/*
 * 作成日: 2008/06/23
 */
package jp.ac.fit.asura.nao.localization.self;

import jp.ac.fit.asura.nao.VisualCycle;
import jp.ac.fit.asura.nao.event.MotionEventListener;
import jp.ac.fit.asura.nao.event.VisualEventListener;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.vision.VisualContext;

/**
 * @author sey
 *
 * @version $Id: SelfLocalization.java 652 2008-07-05 01:53:05Z sey $
 *
 */
public abstract class SelfLocalization implements VisualCycle,
		VisualEventListener, MotionEventListener {
	public abstract void reset();

	public abstract int getX();

	public abstract int getY();

	public abstract float getHeading();

	public abstract int getConfidence();

	@Override
	public void updateOdometry(float forward, float left, float turnCCW) {
	}

	@Override
	public void updatePosture() {
	}

	@Override
	public void startMotion(Motion motion) {
	}

	@Override
	public void stopMotion(Motion motion) {
	}

	@Override
	public void updateVision(VisualContext context) {
	}
}
