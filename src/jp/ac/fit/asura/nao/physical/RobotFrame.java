/*
 * 作成日: 2008/07/05
 */
package jp.ac.fit.asura.nao.physical;

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
	private Frames id;

	// Joint translation(mm)
	private Vector3f translation;

	// Joint rotation axis(rad)
	private AxisAngle4f axis;

	// Joint weight(kg)
	protected float mass;
	private float grossMass;

	protected RobotFrame parent;
	protected RobotFrame[] children;

	public RobotFrame(Frames id) {
		this.id = id;
		translation = new Vector3f();
		axis = new AxisAngle4f();
	}

	/**
	 * @return translation
	 */
	public Vector3f getTranslation() {
		return translation;
	}

	/**
	 * @return axis
	 */
	public AxisAngle4f getAxis() {
		return axis;
	}

	/**
	 * @return child
	 */
	public RobotFrame[] getChildren() {
		return children;
	}

	/**
	 * @return id
	 */
	public Frames getId() {
		return id;
	}

	/**
	 * @return mass
	 */
	public float getMass() {
		return mass;
	}

	/**
	 * @return grossMass
	 */
	public float getGrossMass() {
		return grossMass;
	}

	/**
	 * @return parent
	 */
	public RobotFrame getParent() {
		return parent;
	}

	protected float calculateGrossMass() {
		float mass = this.mass;

		if (children != null)
			for (RobotFrame rf : children)
				mass += rf.calculateGrossMass();
		grossMass = mass;
		return mass;
	}
}
