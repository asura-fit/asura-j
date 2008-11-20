/*
 * 作成日: 2008/07/01
 */
package jp.ac.fit.asura.nao.misc;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.physical.RobotFrame;

/**
 * @author sey
 *
 * @version $Id$
 *
 */
public class MatrixUtils {
	public static Matrix3f rotationMatrix(AxisAngle4f axisAngle) {
		Matrix3f mat = new Matrix3f();
		mat.set(axisAngle);
		return mat;
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
	public static void transform(Vector3f vector, RobotFrame frame, float rad) {
		Matrix3f mat = new Matrix3f();
		AxisAngle4f axisAngle = new AxisAngle4f(frame.getAxis());
		axisAngle.angle += rad;
		mat.set(axisAngle);
		mat.transform(vector);
		vector.add(frame.getTranslation());
	}

	public static void inverseTransform(Vector3f vector, RobotFrame frame,
			float rad) {
		vector.sub(frame.getTranslation());
		Matrix3f mat = new Matrix3f();
		AxisAngle4f axisAngle = new AxisAngle4f(frame.getAxis());
		axisAngle.angle += rad;
		axisAngle.angle = -axisAngle.angle;
		mat.set(axisAngle);
		mat.transform(vector);
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
