/*
 * 作成日: 2009/04/11
 */
package jp.ac.fit.asura.nao.naoji.motion;

import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.MotionParam;
import jp.ac.fit.asura.nao.motion.MotionParam.WalkParam;
import jp.ac.fit.asura.naoji.jal.JALMotion;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 *
 * @version $Id: $
 *
 */
public class NaojiWalker extends Motion {
	private static final Logger log = Logger.getLogger(NaojiWalker.class);
	private JALMotion jalmotion;
	private int samples;
	private int taskId;

	public NaojiWalker(JALMotion motion, int samples) {
		this.jalmotion = motion;
		this.samples = samples;
		taskId = -1;
	}

	@Override
	public boolean canAccept(MotionParam param) {
		return param instanceof MotionParam.WalkParam;
	}

	@Override
	public void start(MotionParam param) {
		if (isRunning()) {
			log.warn("Motion " + taskId + " is running.");
			return;
		}
		taskId = -1;
		assert param instanceof MotionParam.WalkParam;
		WalkParam walkp = (WalkParam) param;

		float forward = MathUtils.clipAbs(walkp.getForward(), 5.0f);
		float left = MathUtils.clipAbs(walkp.getLeft(), 5.0f);
		float turn = MathUtils.clipAbs(walkp.getTurn(), MathUtils.PIf);

		if (forward != 0 && turn != 0) {
			log.debug("walkArc with:" + param);
			taskId = jalmotion.walkArc(turn, forward, samples);
		} else if (forward != 0) {
			log.debug("walkForward with:" + param);
			taskId = jalmotion.walkStraight(forward, samples);
		} else if (left != 0) {
			log.debug("walkSideways with:" + param);
			taskId = jalmotion.walkSideways(left, samples);
		} else if (turn != 0) {
			log.debug("turn with:" + param);
			taskId = jalmotion.turn(turn, samples);
		}
	}

	@Override
	public void step() {
	}

	@Override
	public void stop() {
		log.debug("stop() called.");
		// jalmotion.clearFootsteps();
	}

	@Override
	public boolean canStop() {
		boolean active = jalmotion.walkIsActive();
		log.trace("canStop? " + active);
		return !active && !isRunning();
	}

	@Override
	public String getName() {
		return "NaojiWalker";
	}

	@Override
	public void requestStop() {
		log.debug("requestStop is called.");
		// jalmotion.clearFootsteps();
	}

	@Override
	public boolean hasNextStep() {
		boolean active = jalmotion.walkIsActive();
		log.trace("hasNextStep? " + active);
		return active || isRunning();
	}

	public boolean isRunning() {
		if (taskId < 0)
			return false;
		boolean isRunning = jalmotion.isRunning(taskId);
		log.trace("isRunning? " + isRunning);
		if (!isRunning)
			taskId = -1;
		return isRunning;
	}
}
