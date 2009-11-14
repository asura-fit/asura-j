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

	// 親フレームからみたこのフレームの回転軸ベクトルと現在角度
	private AxisAngle4f axisAngle;

	// these parameters derived by Forward Kinematics.
	// 親フレームからみたこのフレームの回転行列
	private Matrix3f rotation;

	// 親フレームからみたこのフレームの座標(常に対応するFrame.getTranslation()と同じ)
	private Vector3f position;

	// ボディ座標系からみたこのフレームの回転行列
	private Matrix3f bodyRotation;

	// ボディ座標系からみたこのフレームの絶対座標
	private Vector3f bodyPosition;

	// ボディ座標系からみたこのフレームの重心位置の絶対座標
	private Vector3f bodyCenterOfMass;

	// このフレームの角速度
	private float angularVelocity;

	// 親フレームからみたこのフレームの速度(常に0)
	// private Vector3f velocity;

	// ボディ座標系からみたこのフレームの速度
	// private Vector3f bodyVelocity;

	/**
	 *
	 */
	public FrameState(RobotFrame frame) {
		this.axisAngle = new AxisAngle4f(frame.getAxis());
		this.rotation = new Matrix3f();
		this.position = new Vector3f(frame.getTranslation());
		this.bodyPosition = new Vector3f();
		this.bodyRotation = new Matrix3f();
		this.bodyCenterOfMass = new Vector3f();
		this.frame = frame;
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
	 * 関節の角度をセットします.
	 *
	 * @param value
	 */
	public void setAngle(float value) {
		axisAngle.setAngle(value);
	}

	/**
	 * @return the axisAngle
	 */
	public AxisAngle4f getAxisAngle() {
		return axisAngle;
	}

	/**
	 * 関節の角速度を返します.
	 *
	 * @return
	 */
	public float getAngularVelocity() {
		return angularVelocity;
	}

	/**
	 * 関節の角速度をセットします.
	 *
	 * @param angularVelocity
	 */
	public void setAngularVelocity(float angularVelocity) {
		this.angularVelocity = angularVelocity;
	}

	/**
	 * この関節状態のコピーを作成します.
	 */
	public FrameState clone() {
		FrameState obj = new FrameState(frame);
		obj.angularVelocity = angularVelocity;
		obj.axisAngle.set(axisAngle);
		obj.position.set(position);
		obj.rotation.set(rotation);
		obj.bodyPosition.set(bodyPosition);
		obj.bodyRotation.set(bodyRotation);
		obj.bodyCenterOfMass.set(bodyCenterOfMass);
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

	/**
	 * @return bodyCenterOfMass
	 */
	public Vector3f getBodyCenterOfMass() {
		return bodyCenterOfMass;
	}

	public RobotFrame getFrame() {
		return frame;
	}
}
