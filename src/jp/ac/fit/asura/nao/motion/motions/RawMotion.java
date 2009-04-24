/*
 * 作成日: 2009/04/15
 */
package jp.ac.fit.asura.nao.motion.motions;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.motion.Motion;

/**
 * TimeStepごとにフレームを指定するモーション.
 *
 * @author sey
 *
 * @version $Id: $
 *
 */
@Deprecated
public class RawMotion extends Motion {
	float[] frames;

	public RawMotion(float[] frames) {
		this.frames = frames;
		this.totalFrames = frames.length;
	}

	@Override
	public void step() {
		Effector effector = context.getRobotContext().getEffector();
		int joints = Joint.values().length;
		for (Joint j : Joint.values())
			effector.setJoint(j, frames[currentStep * joints + j.ordinal()]);
		currentStep++;
	}
}
