/*
 * 作成日: 2008/05/09
 */
package jp.ac.fit.asura.nao.motion;

import jp.ac.fit.asura.nao.Joint;

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

			public float[] stepNextFrame(float[] current) {
				float[] frame = frames[currentStep];
				currentStep++;
				return frame;
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

			public float[] stepNextFrame(float[] current) {
				if (currentStep == 0) {
					sequence = 0;
					sequenceStep = 0;
					interpolateFrame(current);
				} else if (sequenceStep >= steps[sequence]) {
					sequence++;
					sequenceStep = 0;
					interpolateFrame(current);
				}
				currentStep++;
				return interpolatedFrames[sequence][sequenceStep++];
			}

			protected void interpolateFrame(float[] cp) {
				int divides = steps[sequence];
				float[] tp = frames[sequence];
				for (int i = 0; i < divides; i++) {
					float[] ipf = interpolatedFrames[sequence][i];
					for (int j = 0; j < ipf.length; j++) {
						float ratio = (float) (i + 1.0f) / divides;
						ipf[j] = cp[j] * (1.0F - ratio)
								+ (float) Math.toRadians(tp[j]) * ratio;
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

			public float[] stepNextFrame(float[] current) {
				if (currentStep == 0) {
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
				return ip;
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
