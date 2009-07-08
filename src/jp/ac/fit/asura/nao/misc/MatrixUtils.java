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
	 * 回転行列rotからZYXオイラー角(Roll-Pitch-Yaw, ロール-ピッチ-ヨー)を求めpyrに格納します.
	 *
	 * ただしzをRoll, yをYaw, xをPitchとします.
	 *
	 * @param rot
	 * @param ypr
	 */
	public static void rot2pyr(Matrix3f rot, Vector3f ypr) {
		// // Roll
		// rpy.x = (float) Math.atan2(rot.m21, rot.m22);
		//
		// // Pitch
		// // rpy.y = (float) Math.atan2(rot.m12 * sYaw + rot.m02 * cYaw,
		// rot.m01
		// // * sYaw + rot.m00 * cYaw);
		// rpy.y = (float) Math.atan2(rot.m12 * sYaw + rot.m02 * cYaw, rot.m22);
		//
		// // Yaw
		// rpy.z = (float) yaw;

		// ZYXオイラー角の場合
		// Z軸上の回転角度
		double yaw = Math.atan2(rot.m10, rot.m00);

		float cosYaw = (float) Math.cos(yaw);
		float sinYaw = (float) Math.sin(yaw);

		// Roll
		ypr.x = (float) Math.atan2(rot.m02 * sinYaw - rot.m12 * cosYaw,
				-rot.m01 * sinYaw + rot.m11 * cosYaw);

		// Pitch
		ypr.y = (float) Math.atan2(-rot.m20, rot.m00 * cosYaw + rot.m10
				* sinYaw);

		// Yaw
		ypr.z = (float) yaw;
	}

	/**
	 * ZYXオイラー角(Roll-Pitch-Yaw, ロール-ピッチ-ヨー) pyrから回転行列を求め、rotに格納します.
	 *
	 * ただしzをRoll, yをYaw, xをPitchとします.
	 *
	 * @param ypr
	 * @param rot
	 */
	public static void pyr2rot(Vector3f ypr, Matrix3f rot) {
		float sRoll = (float) Math.sin(ypr.x); // ロール(yaw)
		float cRoll = (float) Math.cos(ypr.x);
		float sPitch = (float) Math.sin(ypr.y);
		float cPitch = (float) Math.cos(ypr.y);
		float sYaw = (float) Math.sin(ypr.z);
		float cYaw = (float) Math.cos(ypr.z);

		rot.m00 = cYaw * cPitch;
		rot.m01 = -sYaw * cRoll + cYaw * sPitch * sRoll;
		rot.m02 = sYaw * sRoll + cYaw * sPitch * cRoll;
		rot.m10 = sYaw * cPitch;
		rot.m11 = cYaw * cRoll + sYaw * sPitch * sRoll;
		rot.m12 = -cYaw * sRoll + sYaw * sPitch * cRoll;
		rot.m20 = -sPitch;
		rot.m21 = cPitch * sRoll;
		rot.m22 = cPitch * cRoll;
	}

	/**
	 * 回転行列から角速度ベクトルへの変換. ヒューマノイドロボット p35より.
	 *
	 * @param rotation
	 *            変換する回転行列
	 * @param omega
	 *            角速度ベクトルの書き込み先
	 */
	public static void rot2omega(Matrix3f rot, Vector3f omega) {
		double theta = Math.acos(MathUtils.clipAbs(
				(rot.m00 + rot.m11 + rot.m22 - 1) / 2.0, 1));
		if (MathUtils.epsEquals(Math.sin(theta), 0)
				|| MatrixUtils.isIdentity(rot)) {
			// System.out.println(Math.sin(theta));
			omega.set(0, 0, 0);
			return;
		}
		assert !Double.isNaN(theta) && Math.sin(theta) != 0;

		omega.setX(rot.m21 - rot.m12);
		omega.setY(rot.m02 - rot.m20);
		omega.setZ(rot.m10 - rot.m01);
		omega.scale((float) theta / (float) (2 * Math.sin(theta)));
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
	 * 行列matの中身は保持されないことに注意してください.
	 *
	 * @param mat
	 * @param b
	 * @param x
	 * @throws SingularMatrixException
	 */
	public static void solve(GMatrix mat, GVector b, GVector x)
			throws SingularMatrixException {
		// LU分解その1
		GVector perm = new GVector(mat.getNumRow());
		mat.LUD(mat, perm);
		x.LUDBackSolve(mat, b, perm);

		// 逆行列(中身はLU分解)
		// jacobi.invert();
		// x.mul(mat, b);
	}

	/**
	 * 連立一次方程式mat*x=bを解きます. その2.
	 */
	public static int solve2(GMatrix mat, GVector b, GVector x)
			throws SingularMatrixException {

		int rows = mat.getNumRow();
		int cols = mat.getNumCol();
		GMatrix u = new GMatrix(rows, rows);
		GMatrix w = new GMatrix(rows, cols);
		GMatrix v = new GMatrix(cols, cols);
		int rank = mat.SVD(u, w, v);
		x.SVDBackSolve(u, w, v, b);
		return rank;
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
