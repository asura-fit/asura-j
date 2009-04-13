/*
 * 作成日: 2009/04/11
 */
package jp.ac.fit.asura.nao.naoji.motion;

import org.apache.log4j.Logger;

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

		if (walkp.getForward() != 0 && walkp.getTurn() != 0) {
			log.debug("walkArc with:" + param);
			taskId = jalmotion.walkArc(walkp.getTurn(), walkp.getForward(),
					samples);
		} else if (walkp.getForward() != 0) {
			log.debug("walkForward with:" + param);
			taskId = jalmotion.walkStraight(walkp.getForward(), samples);
		} else if (walkp.getLeft() != 0) {
			log.debug("walkSideways with:" + param);
			taskId = jalmotion.walkSideways(walkp.getLeft(), samples);
		} else if (walkp.getTurn() != 0) {
			log.debug("turn with:" + param);
			taskId = jalmotion.turn(walkp.getTurn(), samples);
		}
	}

	@Override
	public void stepNextFrame(Sensor sensor, Effector effector) {
	}

	@Override
	public void stop() {
		log.debug("stop() called. clearFootsteps.");
		jalmotion.clearFootsteps();
	}

	@Override
	public boolean canStop() {
		boolean active = jalmotion.walkIsActive();
		log.trace("canStop? " + active);
		return !active;
	}

	@Override
	public String getName() {
		return "NaojiWalker";
	}

	@Override
	public void requestStop() {
		log.debug("requestStop is called.");
		jalmotion.clearFootsteps();
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
