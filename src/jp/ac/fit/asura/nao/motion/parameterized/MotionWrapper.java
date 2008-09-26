/*
 * 作成日: 2008/07/07
 */
package jp.ac.fit.asura.nao.motion.parameterized;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.motion.Motion;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public abstract class MotionWrapper extends Motion {
	protected Motion motion;

	public MotionWrapper(Motion motion) {
		this.motion = motion;
	}

	public abstract String getName();

	public abstract int getId();

	public void init(RobotContext context) {
		motion.init(context);
	}

	public void start() {
		motion.start();
	}

	public void stop() {
		motion.stop();
	}

	public int getTotalFrames() {
		return motion.getTotalFrames();
	}

	public boolean canStop() {
		return motion.canStop();
	}

	public void requestStop() {
		motion.requestStop();
	}

	public boolean hasNextStep() {
		return motion.hasNextStep();
	}

	public float[] stepNextFrame(float[] current) {
		return motion.stepNextFrame(current);
	}
}
