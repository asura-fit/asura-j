package jp.ac.fit.asura.nao.vision;

public class VisualParam {
	public static enum Boolean {
		USE_HOUGH(false);

		private boolean defaultValue;

		private Boolean() {
			this.defaultValue = false;
		}

		private Boolean(boolean defaultValue) {
			this.defaultValue = defaultValue;
		}

		public boolean getDefault() {
			return defaultValue;
		}
	}

	public static enum Float {
		BALL_DIST_CALIBa(12345.0f), BALL_DIST_CALIBb(2.55062f), BALL_DIST_CALIBc(
				-67.2262f);

		private float defaultValue;

		private Float() {
			this.defaultValue = 0;
		}

		private Float(float defaultValue) {
			this.defaultValue = defaultValue;
		}

		public float getDefault() {
			return defaultValue;
		}
	}

	public static enum Int {
		BALL_BLOB_THRESHOLD(25), GOAL_BLOB_THRESHOLD(50);

		private int defaultValue;

		private Int() {
			this.defaultValue = 0;
		}

		private Int(int defaultValue) {
			this.defaultValue = defaultValue;
		}

		public int getDefault() {
			return defaultValue;
		}
	}
}
