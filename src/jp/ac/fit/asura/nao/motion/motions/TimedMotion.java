/*
 * 作成日: 2009/04/15
 */
package jp.ac.fit.asura.nao.motion.motions;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.MotionParam;

import org.apache.log4j.Logger;

/**
 * @author sey
 *
 * @version $Id: $
 *
 */
public class TimedMotion extends Motion {
	private static final Logger log = Logger.getLogger(TimedMotion.class);

	float[] frames;
	int[] times;
	int sequence;
	int sequenceStep;
	boolean isStarted;
	long startTime;
	int totalTimes;

	public TimedMotion(float[] frames, int[] times) {
		assert frames.length == (Joint.values().length - 2) * times.length : "Invalid matrix:"
				+ frames.length;
		this.frames = frames;
		this.totalFrames = 0;

		int time = 0;
		this.times = new int[times.length];
		for (int i = 0; i < times.length; i++) {
			time += times[i];
			this.times[i] = time;
		}
		totalTimes = time;
		log.debug("new TimedMotion with matrix:" + frames.length
				+ " totalTimes:" + totalTimes);
	}

	@Override
	public void start(MotionParam param) throws IllegalArgumentException {
		isStarted = false;
		startTime = System.currentTimeMillis();
		log.debug("TimedMotion start" + startTime);
	}

	@Override
	public void stop() {
		log.debug("TimedMotion stop" + System.currentTimeMillis());
	}

	@Override
	public void stepNextFrame(Sensor sensor, Effector effector) {
		if (isStarted)
			return;
		effector.setBodyJoints(frames, times);
		isStarted = true;
	}

	@Override
	public boolean hasNextStep() {
		long current = System.currentTimeMillis();
		return current - startTime < totalTimes;
	}
}
