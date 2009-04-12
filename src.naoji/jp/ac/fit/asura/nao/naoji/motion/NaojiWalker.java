/*
 * 作成日: 2009/04/11
 */
package jp.ac.fit.asura.nao.naoji.motion;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.MotionParam;
import jp.ac.fit.asura.nao.motion.MotionParam.WalkParam;
import jp.ac.fit.asura.naoji.jal.JALMotion;

/**
 * @author $Author: sey $
 *
 * @version $Id: $
 *
 */
public class NaojiWalker extends Motion {
	private JALMotion jalmotion;
	private int samples;

	public NaojiWalker(JALMotion motion, int samples) {
		this.jalmotion = motion;
		this.samples = samples;
	}

	@Override
	public boolean canAccept(MotionParam param) {
		return param instanceof MotionParam.WalkParam;
	}

	@Override
	public void start(MotionParam param) {
		assert param instanceof MotionParam.WalkParam;
		WalkParam walkp = (WalkParam) param;

		if (walkp.getForward() != 0 && walkp.getTurn() != 0) {
			jalmotion.walkArc(walkp.getTurn(), walkp.getForward(), samples);
		} else if (walkp.getForward() != 0) {
			jalmotion.walkStraight(walkp.getForward(), samples);
		} else if (walkp.getLeft() != 0) {
			jalmotion.walkSideways(walkp.getLeft(), samples);
		} else if (walkp.getTurn() != 0) {
			jalmotion.walkSideways(walkp.getTurn(), samples);
		}
	}

	@Override
	public void stepNextFrame(Sensor sensor, Effector effector) {
	}

	@Override
	public boolean canStop() {
		return !jalmotion.walkIsActive();
	}

	@Override
	public String getName() {
		return "NaojiWalker";
	}

	@Override
	public void requestStop() {
		jalmotion.clearFootsteps();
	}

	@Override
	public boolean hasNextStep() {
		return jalmotion.walkIsActive();
	}
}
