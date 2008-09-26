/*
 * 作成日: 2008/07/01
 */
package jp.ac.fit.asura.nao.misc;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

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
		AxisAngle4f axisAngle = new AxisAngle4f(frame.axis);
		axisAngle.angle += rad;
		mat.set(axisAngle);
		mat.transform(vector);
		vector.add(frame.translate);
	}

	public static void inverseTransform(Vector3f vector, RobotFrame frame,
			float rad) {
		vector.sub(frame.translate);
		Matrix3f mat = new Matrix3f();
		AxisAngle4f axisAngle = new AxisAngle4f(frame.axis);
		axisAngle.angle += rad;
		axisAngle.angle = -axisAngle.angle;
		mat.set(axisAngle);
		mat.transform(vector);
	}
}
