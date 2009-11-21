/*
 * 作成日: 2009/04/12
 */
package jp.ac.fit.asura.nao.motion;

import jp.ac.fit.asura.nao.misc.MathUtils;

/**
 * @author sey
 *
 * @version $Id: $
 *
 */
public interface MotionParam {
	public static final MotionParam EMPTY = new MotionParam() {
	};

	public static final class Float1Param implements MotionParam {
		private float param1;

		public Float1Param() {
		}

		public Float1Param(float param1) {
			setParam(param1);
		}

		public float getParam1() {
			return param1;
		}

		public void setParam(float param1) {
			this.param1 = param1;
		}
	}

	/**
	 * WalkParam - 速度と歩数を指定して歩行する時のパラメータ.
	 *
	 */
	public static final class WalkParam implements MotionParam {
		private float forward;
		private float left;
		private float turn;
		private float pedometer;

		public WalkParam() {
			setParam(0, 0, 0, Float.POSITIVE_INFINITY);
		}

		public WalkParam(float forward, float left, float turn) {
			setParam(forward, left, turn, Float.POSITIVE_INFINITY);
		}

		public WalkParam(float forward, float left, float turn, float pedometer) {
			setParam(forward, left, turn, pedometer);
		}

		public void clear() {
			setParam(0, 0, 0, Float.POSITIVE_INFINITY);
		}

		public float getForward() {
			return forward;
		}

		public float getLeft() {
			return left;
		}

		public float getTurn() {
			return turn;
		}

		public float getPedometer() {
			return pedometer;
		}

		/**
		 * 歩行パラメータをセットします.
		 *
		 * @param forward
		 *            一歩あたりの前進/後退速度
		 * @param left
		 *            一歩あたりの側方移動速度
		 * @param turn
		 *            一歩あたりの回転速度
		 * @param pedometer
		 *            何歩移動するか. 制限しない場合はFloat.POSITIVE_INFINITYを指定する
		 */
		public void setParam(float forward, float left, float turn,
				float pedometer) {
			assert !Float.isNaN(forward);
			assert !Float.isNaN(left);
			assert !Float.isNaN(turn);
			this.forward = forward;
			this.left = left;
			this.turn = turn;
			this.pedometer = pedometer;
		}

		public void setForward(float forward) {
			this.forward = forward;
		}

		public void setLeft(float left) {
			this.left = left;
		}

		public void setTurn(float turn) {
			this.turn = turn;
		}

		public void setPedometer(float pedometer) {
			this.pedometer = pedometer;
		}

		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (obj instanceof WalkParam) {
				WalkParam p2 = (WalkParam) obj;
				return MathUtils.epsEquals(forward, p2.forward)
						&& MathUtils.epsEquals(left, p2.left)
						&& MathUtils.epsEquals(turn, p2.turn)
						&& (pedometer == p2.pedometer || MathUtils.epsEquals(
								pedometer, p2.pedometer));
			}
			return false;
		}

		public String toString() {
			return "WalkParam forward:" + forward + " left:" + left + " turn:"
					+ turn + " pedometer:" + pedometer;
		}
	}
}
