package jp.ac.fit.asura.nao.vision;

public class VisualParam {
	public static enum Float {
		BALL_DIST_CALIB;

		private float defaultValue;

		public float getDefault() {
			return defaultValue;
		}

		private Float() {
			this.defaultValue = 0;
		}

		private Float(float defaultValue) {
			this.defaultValue = defaultValue;
		}
	}

	public static enum Boolean {
		USE_HOUGH(false);

		private boolean defaultValue;

		public boolean getDefault() {
			return defaultValue;
		}

		private Boolean() {
			this.defaultValue = false;
		}

		private Boolean(boolean defaultValue) {
			this.defaultValue = defaultValue;
		}
	}
}
