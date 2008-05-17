/*
 * 作成日: 2008/05/09
 */
package jp.ac.fit.asura.nao.motion;

import jp.ac.fit.asura.nao.Joint;

/**
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public abstract class MotionFactory {
	public enum Type {
		Raw(1), Liner(2), Compatible(3);
		private final int id;

		Type(int id) {
			this.id = id;
		}

		public static Type valueOf(int id) {
			for (Type t : Type.values()) {
				if (t.id == id)
					return t;
			}
			return null;
		}
	}

	private static MotionFactory raw = new RawMotionFactory();
	private static MotionFactory liner = new LinerMotionFactory();
	private static MotionFactory compatible = new CompatibleMotionFactory();

	public static Motion create(Type type, Object arg) {
		switch (type) {
		case Raw:
			return raw.create(arg);
		case Liner:
			return liner.create(arg);
		case Compatible:
			return compatible.create(arg);
		default:
			assert false;
			return null;
		}
	}

	public abstract Motion create(Object arg);

	/**
	 * TimeStepごとにフレームを指定するタイプのモーションファクトリー
	 */
	static class RawMotionFactory extends MotionFactory {
		class RawMotion extends Motion {
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

		public Motion create(Object args) {
			float[][] frames = (float[][]) args;
			return new RawMotion(frames);
		}
	}

	/**
	 * 通常の線形補完モーションファクトリー
	 */
	static class LinerMotionFactory extends MotionFactory {
		class LinerMotion extends Motion {
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

			private void interpolateFrame(float[] cp) {
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

		public Motion create(Object args) {
			float[][] frames = (float[][]) ((Object[]) args)[0];
			int[] steps = (int[]) ((Object[]) args)[1];

			assert frames.length == steps.length;

			Motion motion = new LinerMotion(frames, steps);
			return motion;
		}
	}

	/**
	 * 古いC言語版のモーションプログラム用のデータと互換性のあるファクトリー
	 */
	static class CompatibleMotionFactory extends MotionFactory {
		class CompatibleMotion extends Motion {
			float[][] frames;
			int[] steps;
			int sequence;
			int sequenceStep;

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

			private void interpolateFrame() {
				int divides = steps[sequence];
				float[] tp = frames[sequence];
				for (int i = 0; i < dp.length; i++) {
					dp[i] = (float) (Math.toRadians(tp[i]) - ip[i]) / divides;
				}
			}
		}

		public Motion create(Object args) {
			float[][] frames = (float[][]) ((Object[]) args)[0];
			int[] steps = (int[]) ((Object[]) args)[1];

			assert frames.length == steps.length;

			Motion motion = new CompatibleMotion(frames, steps);
			return motion;
		}
	}
}
