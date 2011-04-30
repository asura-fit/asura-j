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
		BALL_DIST_CALIBa(17000.2f), BALL_DIST_CALIBb(2.55062f), BALL_DIST_CALIBc(
				-67.2262f),

		BALL_COMPARE(0.5f),

		GOAL_DIST_CALIB_POLEa(2*35111.1f), GOAL_DIST_CALIB_POLEb(3.82416f), GOAL_DIST_CALIB_POLEc(
				113.482f),

		GOAL_DIST_CALIB_HEIGHTa(2*173890), GOAL_DIST_CALIB_HEIGHTb(5.01436f), GOAL_DIST_CALIB_HEIGHTc(
				80.5422f),

		GOAL_DIST_CALIB_WIDTHa(2*312111.1f), GOAL_DIST_CALIB_WIDTHb(5.01436f), GOAL_DIST_CALIB_WIDTHc(
				80.482f);

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
