/*
 * 作成日: 2008/05/09
 */
package jp.ac.fit.asura.nao.motion;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.Sensor;

/**
 * @author sey
 *
 * @version $Id: MotionFactory.java 658 2008-07-06 18:56:48Z sey $
 *
 */
public abstract class MotionFactory {
	/**
	 * TimeStepごとにフレームを指定するタイプのモーションファクトリー
	 */
	public static class Raw extends MotionFactory {
		public static class RawMotion extends Motion {
			float[][] frames;

			public RawMotion(float[][] frames) {
				this.frames = frames;
				this.totalFrames = frames.length;
			}

			@Override
			public void stepNextFrame(Sensor sensor, Effector effector) {
				float[] frame = frames[currentStep];
				for (Joint j : Joint.values())
					effector.setJoint(j, frame[j.ordinal()]);
				currentStep++;
			}
		}

		public static Motion create(float[][] args) {
			return new RawMotion(args);
		}
	}

	/**
	 * 通常の線形補完モーションファクトリー
	 */
	public static class Liner extends MotionFactory {
		public static class LinerMotion extends Motion {
			float[][] frames;
			float[][][] interpolatedFrames;
			int[] steps;
			int sequence;
			int sequenceStep;

			public LinerMotion(float[][] frames, int[] steps) {
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
			public void stepNextFrame(Sensor sensor, Effector effector) {
				if (currentStep == 0) {
					sequence = 0;
					sequenceStep = 0;
					interpolateFrame(sensor);
				} else if (sequenceStep >= steps[sequence]) {
					sequence++;
					sequenceStep = 0;
					interpolateFrame(sensor);
				}
				currentStep++;
				for (Joint j : Joint.values())
					effector.setJoint(j,
							interpolatedFrames[sequence][sequenceStep][j
									.ordinal()]);

				sequenceStep++;
			}

			protected void interpolateFrame(Sensor sensor) {
				int divides = steps[sequence];
				float[] tp = frames[sequence];
				for (int i = 0; i < divides; i++) {
					float[] ipf = interpolatedFrames[sequence][i];
					for (Joint j : Joint.values()) {
						float ratio = (float) (i + 1.0f) / divides;

						ipf[j.ordinal()] = sensor.getJoint(j) * (1.0F - ratio)
								+ (float) Math.toRadians(tp[j.ordinal()])
								* ratio;
					}
				}
			}
		}

		public static Motion create(float[][] frames, int[] steps) {
			assert frames.length == steps.length;
			Motion motion = new LinerMotion(frames, steps);
			return motion;
		}
	}

	/**
	 * 古いC言語版のモーションプログラム用のデータと互換性のあるファクトリー
	 */
	public static class Compatible extends MotionFactory {
		public static class CompatibleMotion extends Motion {
			public float[][] frames;
			public int[] steps;
			public int sequence;
			public int sequenceStep;

			float[] dp;
			float[] ip;

			public CompatibleMotion(float[][] frames, int[] steps) {
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
			public void stepNextFrame(Sensor sensor, Effector effector) {
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
				for (Joint j : Joint.values())
					effector.setJoint(j, ip[j.ordinal()]);
			}

			protected void interpolateFrame() {
				int divides = steps[sequence];
				float[] tp = frames[sequence];
				for (int i = 0; i < dp.length; i++) {
					dp[i] = (float) (Math.toRadians(tp[i]) - ip[i]) / divides;
				}
			}
		}

		public static Motion create(float[][] frames, int[] steps) {
			assert frames.length == steps.length;
			Motion motion = new CompatibleMotion(frames, steps);
			return motion;
		}
	}

	public static class Forward extends MotionFactory {
		public static Motion create(float[][] frames, int[] steps) {
			assert frames.length == steps.length;
			Motion motion = new ForwardMotion(frames, steps);
			return motion;
		}
	}
}
