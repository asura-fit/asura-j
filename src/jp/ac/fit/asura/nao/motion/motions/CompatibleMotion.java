/*
 * 作成日: 2009/04/15
 */
package jp.ac.fit.asura.nao.motion.motions;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.SensorContext;
import jp.ac.fit.asura.nao.motion.Motion;

/**
 * 古いC言語版のモーションプログラム用のデータと互換性のあるファクトリー
 *
 * @author sey
 *
 * @version $Id: $
 *
 */
@Deprecated
public class CompatibleMotion extends Motion {
	public float[] frames;
	public int[] steps;
	public int sequence;
	public int sequenceStep;

	float[] dp;
	float[] ip;

	public CompatibleMotion(float[] frames, int[] steps) {
		this.frames = frames;
		this.steps = steps;
		this.totalFrames = 0;
		for (int step : steps)
			this.totalFrames += step;

		sequence = 0;
		sequenceStep = 0;

		dp = new float[Joint.values().length];
		ip = new float[Joint.values().length];
	}

	@Override
	public void step() {
		SensorContext sensor = context.getSensorContext();
		Effector effector = context.getRobotContext().getEffector();
		if (currentStep == 0) {
			float[] current = sensor.getJointAngles();
			assert current.length == ip.length;
			System.arraycopy(current, 0, ip, 0, ip.length);
			sequence = 0;
			sequenceStep = 0;
			interpolateFrame();
		} else if (sequenceStep >= steps[sequence]) {
			sequence++;
			sequenceStep = 0;
			interpolateFrame();
		}
		currentStep++;
		sequenceStep++;
		for (int j = 0; j < ip.length; j++) {
			ip[j] += dp[j];
		}
		for (Joint j : Joint.values()) {
			if (j == Joint.HeadYaw || j == Joint.HeadPitch)
				continue;
			effector.setJoint(j, ip[j.ordinal()]);
		}
	}

	protected void interpolateFrame() {
		int divides = steps[sequence];
		int joints = Joint.values().length;
		for (int i = 0; i < dp.length; i++) {
			dp[i] = (float) (Math.toRadians(frames[sequence * joints + i]) - ip[i])
					/ divides;
		}
	}
}