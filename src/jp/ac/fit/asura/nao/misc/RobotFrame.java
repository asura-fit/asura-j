/*
 * 作成日: 2008/07/05
 */
package jp.ac.fit.asura.nao.misc;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;

/**
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public class RobotFrame {
	public int id;
	public Vector3f translate;
	public AxisAngle4f axis;

	public RobotFrame(int id, Vector3f translate, AxisAngle4f axis) {
		this.id = id;
		this.translate = translate;
		this.axis = axis;
	}
}
