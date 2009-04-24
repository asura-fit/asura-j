/*
 * 作成日: 2009/04/15
 */
package jp.ac.fit.asura.nao.motion.motions;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.motion.Motion;

/**
 * 通常の線形補完モーションファクトリー.
 *
 * @author sey
 *
 * @version $Id: $
 *
 */
@Deprecated
public class LinerMotion extends Motion {
	float[] frames;
	float[][][] interpolatedFrames;
	int[] steps;
	int sequence;
	int sequenceStep;

	public LinerMotion(float[] frames, int[] steps) {
		this.frames = frames;
		this.steps = steps;
		this.totalFrames = 0;
		interpolatedFrames = new float[steps.length][][];
		for (int i = 0; i < interpolatedFrames.length; i++) {
			interpolatedFrames[i] = new float[steps[i]][Joint.values().length];
			this.totalFrames += steps[i];
		}

		sequence = 0;
		sequenceStep = 0;
	}

	@Override
	public void step() {
		Effector effector = context.getRobotContext().getEffector();
		if (currentStep == 0) {
			sequence = 0;
			sequenceStep = 0;
			interpolateFrame();
		} else if (sequenceStep >= steps[sequence]) {
			sequence++;
			sequenceStep = 0;
			interpolateFrame();
		}
		currentStep++;
		for (Joint j : Joint.values())
			effector.setJoint(j, interpolatedFrames[sequence][sequenceStep][j
					.ordinal()]);

		sequenceStep++;
	}

	protected void interpolateFrame() {
		int divides = steps[sequence];
		int joints = Joint.values().length;
		for (int i = 0; i < divides; i++) {
			float[] ipf = interpolatedFrames[sequence][i];
			for (Joint j : Joint.values()) {
				float ratio = (float) (i + 1.0f) / divides;

				ipf[j.ordinal()] = context.getSensorContext().getJoint(j)
						* (1.0F - ratio)
						+ (float) Math.toRadians(frames[sequence * joints
								+ j.ordinal()]) * ratio;
			}
		}
	}
}