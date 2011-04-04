/*
 * 作成日: 2009/04/12
 */
package jp.ac.fit.asura.nao.motion;

import javax.vecmath.Vector3f;

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
			set(param1);
		}

		public float getParam1() {
			return param1;
		}

		public void set(float param1) {
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
			set(0, 0, 0, Float.POSITIVE_INFINITY);
		}

		public WalkParam(float forward, float left, float turn) {
			set(forward, left, turn, Float.POSITIVE_INFINITY);
		}

		public WalkParam(float forward, float left, float turn, float pedometer) {
			set(forward, left, turn, pedometer);
		}

		public void clear() {
			set(0, 0, 0, Float.POSITIVE_INFINITY);
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
		public void set(float forward, float left, float turn, float pedometer) {
			assert !Float.isNaN(forward);
			assert !Float.isNaN(left);
			assert !Float.isNaN(turn);
			this.forward = forward;
			this.left = left;
			this.turn = turn;
			this.pedometer = pedometer;
		}

		public void set(WalkParam wp2) {
			this.forward = wp2.forward;
			this.left = wp2.left;
			this.turn = wp2.forward;
			this.pedometer = wp2.pedometer;
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

	public static final class CircleTurnParam implements MotionParam {
		public enum Side {
			Left, Right
		}
		Side side;

		public CircleTurnParam() {
			side = Side.Left;
		}

		public CircleTurnParam(Side side) {
			this.side = side;
		}

		public void setSide(Side side) {
			this.side = side;
		}

		public Side getSide() {
			return side;
		}

	}

	/**
	 * ShotParam - シュートのパラメータ.
	 *
	 */
	public static final class ShotParam implements MotionParam {
		private Vector3f ball = new Vector3f();
		private float heading;
		private float force;

		public ShotParam() {
			ball.set(0, 0, 0);
			heading = 0;
			force = 0;
		}

		public ShotParam(Vector3f ball, float heading, float force) {
			set(ball, heading, force);
		}

		public void clear() {
		}

		public void set(Vector3f ball, float heading, float force) {
			this.ball.set(ball);
			this.heading = heading;
			this.force = force;
		}

		public void set(ShotParam sp2) {
			this.ball.set(sp2.ball);
			this.heading = sp2.heading;
			this.force = sp2.force;
		}

		/**
		 * ボールの位置
		 *
		 * @return
		 */
		public Vector3f getBall() {
			return ball;
		}

		/**
		 * シュートの強さ [N]
		 *
		 * @return
		 */
		public float getForce() {
			return force;
		}

		/**
		 * シュートを打ち出す方向
		 *
		 * @return
		 */
		public float getHeading() {
			return heading;
		}

		public void setBall(Vector3f ball) {
			this.ball.set(ball);
		}

		public void setForce(float force) {
			this.force = force;
		}

		public void setHeading(float heading) {
			this.heading = heading;
		}

		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (obj instanceof ShotParam) {
				ShotParam p2 = (ShotParam) obj;

				return ball.epsilonEquals(p2.ball, MathUtils.EPSf)
						&& MathUtils.epsEquals(heading, p2.heading)
						&& MathUtils.epsEquals(force, p2.force);

			}
			return false;
		}

		public String toString() {
			return "ShotParam ball:" + ball + " heading:" + heading + " force:"
					+ force;
		}
	}
}
