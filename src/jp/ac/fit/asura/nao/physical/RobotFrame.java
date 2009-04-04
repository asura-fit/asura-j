/*
 * 作成日: 2008/07/05
 */
package jp.ac.fit.asura.nao.physical;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.physical.Robot.Frames;

/**
 * @author sey
 *
 * @version $Id: RobotFrame.java 717 2008-12-31 18:16:20Z sey $
 *
 */
public class RobotFrame {
	private Frames id;

	// Joint translation(mm)
	private Vector3f translation;

	// Joint rotation axis(rad)
	private AxisAngle4f axis;

	//
	private Vector3f centerOfMass;

	// Joint weight(kg)
	private float mass;
	private float grossMass;

	private float maxAngle;
	private float minAngle;

	private float maxAngleDeg;
	private float minAngleDeg;

	private RobotFrame parent;
	private RobotFrame[] children;

	public RobotFrame(Frames id) {
		this.id = id;
		translation = new Vector3f();
		axis = new AxisAngle4f();
		centerOfMass = new Vector3f();
		children = new RobotFrame[0];
	}

	public void clear() {
		translation.set(0, 0, 0);
		axis.set(0, 0, 0, 0);
		centerOfMass.set(0, 0, 0);
		parent = null;
		children = new RobotFrame[0];
		grossMass = 0;
		mass = 0;
		maxAngle = 0;
		maxAngleDeg = 0;
		minAngle = 0;
		minAngleDeg = 0;
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
	 * @return centerOfMass
	 */
	public Vector3f getCenterOfMass() {
		return centerOfMass;
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

	/**
	 * @return the maxAngle
	 */
	public float getMaxAngle() {
		return maxAngle;
	}

	/**
	 * @return the maxAngleDeg
	 */
	public float getMaxAngleDeg() {
		return maxAngleDeg;
	}

	/**
	 * @return the minAngle
	 */
	public float getMinAngle() {
		return minAngle;
	}

	/**
	 * @return the minAngleDeg
	 */
	public float getMinAngleDeg() {
		return minAngleDeg;
	}

	/**
	 * @param children
	 *            the children to set
	 */
	public void setChildren(RobotFrame[] children) {
		this.children = children;
	}

	/**
	 * @param mass
	 *            the mass to set
	 */
	public void setMass(float mass) {
		this.mass = mass;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(RobotFrame parent) {
		this.parent = parent;
	}

	/**
	 * @param maxAngle
	 *            the maxAngle to set
	 */
	public void setMaxAngle(float maxAngle) {
		this.maxAngle = maxAngle;
		this.maxAngleDeg = MathUtils.toDegrees(maxAngle);
	}

	/**
	 * @param minAngle
	 *            the minAngle to set
	 */
	public void setMinAngle(float minAngle) {
		this.minAngle = minAngle;
		this.minAngleDeg = MathUtils.toDegrees(minAngle);
	}

	public float calculateGrossMass() {
		float mass = this.mass;

		if (children != null)
			for (RobotFrame rf : children)
				mass += rf.calculateGrossMass();
		grossMass = mass;
		return mass;
	}
}
