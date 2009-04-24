/*
 * 作成日: 2008/07/07
 */
package jp.ac.fit.asura.nao.motion.parameterized;

import jp.ac.fit.asura.nao.MotionFrameContext;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.MotionParam;

/**
 * @author $Author: sey $
 *
 * @version $Id: MotionWrapper.java 709 2008-11-23 07:40:31Z sey $
 *
 */
public abstract class MotionWrapper extends Motion {
	protected Motion motion;

	public MotionWrapper(Motion motion) {
		this.motion = motion;
	}

	@Override
	public abstract String getName();

	@Override
	public abstract int getId();

	@Override
	public void init(RobotContext context) {
		motion.init(context);
	}

	@Override
	public void start(MotionParam param) {
		motion.start(param);
	}

	@Override
	public void stop() {
		motion.stop();
	}

	@Override
	public int getTotalFrames() {
		return motion.getTotalFrames();
	}

	@Override
	public boolean canStop() {
		return motion.canStop();
	}

	@Override
	public void requestStop() {
		motion.requestStop();
	}

	@Override
	public boolean hasNextStep() {
		return motion.hasNextStep();
	}

	@Override
	public void step() {
		motion.step();
	}

	@Override
	public void setContext(MotionFrameContext context) {
		motion.setContext(context);
	}

	@Override
	public boolean canAccept(MotionParam param) {
		return motion.canAccept(param);
	}
}
