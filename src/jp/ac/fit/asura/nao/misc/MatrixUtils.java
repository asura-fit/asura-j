/*
 * 作成日: 2008/07/01
 */
package jp.ac.fit.asura.nao.misc;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.SingularMatrixException;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.sensation.FrameState;
import jp.ac.fit.asura.vecmathx.GMatrix;
import jp.ac.fit.asura.vecmathx.GVector;
import jp.ac.fit.asura.vecmathx.XVecMathUtils;

/**
 * @author sey
 *
 * @version $Id: MatrixUtils.java 717 2008-12-31 18:16:20Z sey $
 *
 */
public class MatrixUtils {
	public static Matrix3f rotationMatrix(AxisAngle4f axisAngle) {
		Matrix3f mat = new Matrix3f();
		mat.set(axisAngle);
		return mat;
	}

	/**
	 * 回転行列rotから、ZXYオイラー角(Roll-Pitch-Yaw, ロール-ピッチ-ヨー)を求めrpyに格納します.
	 *
	 * @param rot
	 * @param rpy
	 */
	public static void rot2rpy(Matrix3f rot, Vector3f rpy) {
		// ZXYオイラー角の場合
		// Roll
		double roll = Math.atan2(-rot.m01, rot.m11);

		double cosRoll = Math.cos(roll);
		double sinRoll = Math.sin(roll);

		rpy.x = (float) roll;

		// Pitch
		rpy.y = (float) Math.atan2(rot.m21, rot.m11 * cosRoll - rot.m01
				* sinRoll);

		// Yaw
		rpy.z = (float) Math.atan2(rot.m12 * sinRoll + rot.m02 * cosRoll,
				rot.m01 * sinRoll + rot.m00 * cosRoll);

		// ZYXオイラー角の場合
		// // Roll
		// double roll = Math.atan2(rot.m10, rot.m00);
		//
		// double cosRoll = Math.cos(roll);
		// double sinRoll = Math.sin(roll);
		// rpy.x = (float) roll;
		// // Pitch
		// rpy.y = (float) Math.atan2(-rot.m20, rot.m00 * cosRoll + rot.m10
		// * sinRoll);
		//
		// // Yaw
		// rpy.z = (float) Math.atan2(rot.m02 * sinRoll - rot.m12 * cosRoll,
		// -rot.m01 * sinRoll + rot.m11 * cosRoll);
	}

	/**
	 * vectorで表される位置ベクトルに，frameによる座標変換を行います. <br>
	 * frameの関節角度にはradが使用されます. 変換結果はvectorに上書きされます.
	 *
	 * @param vector
	 *            変換する位置ベクトル
	 * @param frame
	 * @param rad
	 */
	public static void transform(Vector3f vector, FrameState frame) {
		Matrix3f mat = new Matrix3f();
		mat.set(frame.getAxisAngle());
		mat.transform(vector);
		vector.add(frame.getPosition());
	}

	public static void inverseTransform(Vector3f vector, FrameState frame) {
		vector.sub(frame.getPosition());
		Matrix3f mat = new Matrix3f();
		mat.set(frame.getAxisAngle());
		mat.transpose();
		mat.transform(vector);
	}

	/**
	 * 連立一次方程式mat*x=bを解きます.
	 *
	 * 現在の実装では保持されますが、 一般に行列matの中身は保持されないことに注意してください.
	 *
	 * @param mat
	 * @param b
	 * @param x
	 * @throws SingularMatrixException
	 */
	public static void solve(GMatrix mat, GVector b, GVector x)
			throws SingularMatrixException {
		// LU分解その1
		// GVector perm = new GVector(jacobi.getNumRow());
		// mat.LUD(mat, perm);
		// x.LUDBackSolve(mat, b, perm);

		// LU分解その2
		XVecMathUtils.solve(mat, b, x);

		// 逆行列(中身はLU分解)
		// jacobi.invert();
		// x.mul(mat, b);
	}

	/**
	 * 連立一次方程式mat*x=bを解きます. その2.
	 */
	public static void solve2(GMatrix mat, GVector b, GVector x)
			throws SingularMatrixException {
		int rows = mat.getNumRow();
		int cols = mat.getNumCol();
		GMatrix u = new GMatrix(rows, rows);
		GMatrix w = new GMatrix(rows, cols);
		GMatrix v = new GMatrix(cols, cols);
		int rank = mat.SVD(u, w, v);
		x.SVDBackSolve(u, w, v, b);
	}

	/**
	 * この行列が単位行列であるかを調べます.
	 *
	 * @param matrix
	 * @return
	 */
	private static final Matrix3f E3f = new Matrix3f(1, 0, 0, 0, 1, 0, 0, 0, 1);

	public static boolean isIdentity(Matrix3f matrix) {
		return matrix.epsilonEquals(E3f, 1e-6f);
	}

	public static void setAxis(AxisAngle4f axisAngle, Vector3f axis) {
		axis.x = axisAngle.x;
		axis.y = axisAngle.y;
		axis.z = axisAngle.z;
	}
}
