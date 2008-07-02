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

	public static Vector3f transform(Vector3f from, AxisAngle4f axis,
			float rad, Vector3f translate) {
		Vector3f to = new Vector3f(from);
		Matrix3f mat = new Matrix3f();
		AxisAngle4f axisAngle = new AxisAngle4f(axis);
		axisAngle.angle += rad;
		mat.set(axisAngle);
		mat.transform(to);
		to.add(translate);
		return to;
	}

	public static Vector3f inverseTransform(Vector3f from, AxisAngle4f axis,
			float rad, Vector3f translate) {
		Vector3f to = new Vector3f(from);
		to.sub(translate);
		Matrix3f mat = new Matrix3f();
		AxisAngle4f axisAngle = new AxisAngle4f(axis);
		axisAngle.angle += rad;
		axisAngle.angle = -axisAngle.angle;
		mat.set(axisAngle);
		mat.transform(to);
		return to;
	}
}
