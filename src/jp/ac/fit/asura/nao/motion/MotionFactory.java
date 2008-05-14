/*
 * 作成日: 2008/05/09
 */
package jp.ac.fit.asura.nao.motion;

/**
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public abstract class MotionFactory {
	public enum Type {
		Raw(1), Liner(2);
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

	public static Motion create(Type type, Object arg) {
		switch (type) {
		case Raw:
			return raw.create(arg);
		case Liner:
			return liner.create(arg);
		default:
			assert false;
			return null;
		}
	}

	public abstract Motion create(Object arg);

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

	static class LinerMotionFactory extends MotionFactory {
		class LinerMotion extends Motion {
			float[][] frames;

			public LinerMotion(float[][] frames, int[] steps) {
				this.frames = frames;
				this.totalFrames = 0;
				for (int step : steps)
					this.totalFrames += step;
			}

			public float[] stepNextFrame(float[] current) {
				// TODO not implemented
				return frames[0];
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
}
