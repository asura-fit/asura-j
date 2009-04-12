/*
 * 作成日: 2009/04/12
 */
package jp.ac.fit.asura.nao.motion;

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

	public static final class WalkParam implements MotionParam {
		private float forward;
		private float left;
		private float turn;

		public WalkParam() {
		}

		public WalkParam(float forward, float left, float turn) {
			setParam(forward, left, turn);
		}

		public void clear() {
			setParam(0, 0, 0);
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

		public void setParam(float forward, float left, float turn) {
			this.forward = forward;
			this.left = left;
			this.turn = turn;
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
	}
}
