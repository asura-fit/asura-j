/*
 * 作成日: 2008/09/26
 */
package jp.ac.fit.asura.nao.sensation;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.misc.NDFilter;
import jp.ac.fit.asura.nao.physical.RobotFrame;
import jp.ac.fit.asura.nao.physical.Robot.Frames;

/**
 * @author sey
 * 
 * @version $Id: FrameState.java 717 2008-12-31 18:16:20Z sey $
 * 
 */
public class FrameState {
	private RobotFrame frame;
	private NDFilter.Float nd;

	// このフレームの角速度
	private float dValue;
	private float force;

	// 親フレームからみたこのフレームの回転軸ベクトルと現在角度
	private AxisAngle4f axisAngle;

	// these parameters derived by Forward Kinematics.
	// 親フレームからみたこのフレームの回転行列
	private Matrix3f rotation;

	// 親フレームからみたこのフレームの座標
	private Vector3f position;

	// ボディ座標系からみたこのフレームの回転行列
	private Matrix3f bodyRotation;

	// ボディ座標系からみたこのフレームの絶対座標
	private Vector3f bodyPosition;

	/**
	 *
	 */
	public FrameState(RobotFrame frame) {
		this.axisAngle = new AxisAngle4f(frame.getAxis());
		this.rotation = new Matrix3f();
		this.position = new Vector3f(frame.getTranslation());
		this.bodyPosition = new Vector3f();
		this.bodyRotation = new Matrix3f();
		this.frame = frame;
		nd = new NDFilter.Float();
	}

	/**
	 * 関節状態を更新します.
	 * 
	 * @param value
	 */
	public void updateValue(float value) {
		assert frame.getId().isJoint();
		axisAngle.setAngle(value);
		dValue = nd.eval(value);
	}

	public void updateForce(float force) {
		assert frame.getId().isJoint();
		this.force = force;
	}

	/**
	 * この関節の角度を返します.
	 * 
	 * @return
	 */
	public float getAngle() {
		return axisAngle.getAngle();
	}

	/**
	 * @return the axisAngle
	 */
	public AxisAngle4f getAxisAngle() {
		return axisAngle;
	}

	/**
	 * 関節の速度(もしくは角速度)を返します.
	 * 
	 * @return
	 */
	public float getDValue() {
		return dValue;
	}

	/**
	 * @return the force
	 */
	public float getForce() {
		return force;
	}

	/**
	 * この関節状態の浅い(Shallow)コピーを作成します.
	 * 
	 * 関節値，微分値は複製されますが，微分フィルタはコピーされたインスタンスとの間で共有されるため，取り扱いには注意が必要です.
	 */
	public FrameState clone() {
		FrameState obj = new FrameState(frame);
		// filterもDeep Copyすべきか?
		obj.nd = nd;
		obj.dValue = dValue;
		obj.force = force;
		obj.axisAngle.set(axisAngle);
		obj.position.set(position);
		obj.rotation.set(rotation);
		obj.bodyPosition.set(bodyPosition);
		obj.bodyRotation.set(bodyRotation);
		return obj;
	}

	/**
	 * @return the id
	 */
	public Frames getId() {
		return frame.getId();
	}

	public Vector3f getPosition() {
		return position;
	}

	/**
	 * @return the robotPosition
	 */
	public Vector3f getBodyPosition() {
		return bodyPosition;
	}

	/**
	 * @return the rotation
	 */
	public Matrix3f getRotation() {
		return rotation;
	}

	/**
	 * @return the robotRotation
	 */
	public Matrix3f getBodyRotation() {
		return bodyRotation;
	}

	public RobotFrame getFrame() {
		return frame;
	}
}
