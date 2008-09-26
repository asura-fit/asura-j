/*
 * 作成日: 2008/07/05
 */
package jp.ac.fit.asura.nao.misc;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.physical.Nao.Frames;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class RobotFrame {
	public Frames id;

	// Joint translation(mm)
	public Vector3f translate;

	// Joint rotation axis(rad)
	public AxisAngle4f axis;

	// Joint weight(kg)
	public float mass;

	public RobotFrame parent;
	public RobotFrame[] child;

	public RobotFrame(Frames id) {
		this.id = id;
	}
}
