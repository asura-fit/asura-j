/*
 * 作成日: 2009/05/03
 */
package jp.ac.fit.asura.nao.motion;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.event.MotionEventListener;
import jp.ac.fit.asura.nao.misc.Coordinates;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.misc.MatrixUtils;
import jp.ac.fit.asura.nao.misc.MeanFilter;
import jp.ac.fit.asura.nao.misc.Filter.BooleanFilter;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.SomaticContext;

/**
 * 運動学によるオドメトリの計算.
 *
 * @author sey
 *
 * @version $Id: $
 *
 */
public class KinematicOdometer {
	private BooleanFilter leftFilter;
	private Vector3f lastLeftPosition;
	private Matrix3f lastLeftRotation;

	private BooleanFilter rightFilter;
	private Vector3f lastRightPosition;
	private Matrix3f lastRightRotation;

	private Vector3f dx;
	private Matrix3f dr;
	private Vector3f dpyr;

	public KinematicOdometer() {
		leftFilter = new MeanFilter.Boolean(4);
		rightFilter = new MeanFilter.Boolean(4);
		lastLeftPosition = new Vector3f();
		lastLeftRotation = new Matrix3f();
		lastRightPosition = new Vector3f();
		lastRightRotation = new Matrix3f();
		dx = new Vector3f();
		dr = new Matrix3f();
		dpyr = new Vector3f();
	}

	public void step(SomaticContext sc) {
		boolean isLeftTouched = leftFilter.eval(sc.isLeftOnGround());
		boolean isRightTouched = rightFilter.eval(sc.isRightOnGround());

		// 変位の計算に使う支持脚を決定する
		boolean useLeft;
		if (isLeftTouched && isRightTouched) {
			// 両方接地 > 圧力の高い方
			float leftp = sc.getLeftPressure();
			float rightp = sc.getRightPressure();
			if (leftp > rightp)
				useLeft = true;
			else
				useLeft = false;
		} else if (isLeftTouched)
			useLeft = true;
		else if (isRightTouched)
			useLeft = false;
		else {
			// 両足ともついていない
			dx.set(0, 0, 0);
			dr.setIdentity();
			dpyr.set(0, 0, 0);
			return;
		}

		Frames support = useLeft ? Frames.LSole : Frames.RSole;
		Vector3f supportPos = useLeft ? lastLeftPosition : lastRightPosition;
		Matrix3f supportRot = useLeft ? lastLeftRotation : lastRightRotation;
		Frames swing = !useLeft ? Frames.LSole : Frames.RSole;
		Vector3f swingPos = !useLeft ? lastLeftPosition : lastRightPosition;
		Matrix3f swingRot = !useLeft ? lastLeftRotation : lastRightRotation;

		Vector3f pos = sc.get(support).getBodyPosition();
		Matrix3f rot = sc.get(support).getBodyRotation();
		// 支持脚の位置の変化を計算
		dx.set(supportPos);
		Coordinates.body2robotCoord(sc, pos, supportPos);
		dx.sub(supportPos);

		// 支持脚の姿勢の変化を計算
		// pyr2.sub(pyr1)でもよさそう?
		dr.transpose(supportRot);
		supportRot.mul(sc.getBodyPosture(), rot);
		dr.mul(supportRot);
		// ロールピッチヨー表現に直す
		MatrixUtils.rot2pyr(dr, dpyr);

		// 次の実行のために遊脚も計算(なくてもいい?)
		Coordinates.body2robotCoord(sc, sc.get(swing).getBodyPosition(),
				swingPos);
		swingRot.mul(sc.getBodyPosture(), sc.get(swing).getBodyRotation());
	}

	public void updateOdometry(MotionEventListener listener) {
		float f = dx.z;
		float l = dx.x;
		float t = dpyr.y;
		assert Math.abs(f) < 1e2f;
		assert Math.abs(l) < 1e2f;
		assert Math.abs(t) < MathUtils.PIf / 2;
		listener.updateOdometry(f, l, t);
	}
}
