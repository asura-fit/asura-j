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
 * @version $Id: $
 * 
 */
public class MatrixUtils {
	public static Matrix3f rotationMatrix(AxisAngle4f axisAngle) {
		Matrix3f mat = new Matrix3f();
		mat.set(axisAngle);
		return mat;
	}

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
