/*
 * 作成日: 2008/09/26
 */
package jp.ac.fit.asura.nao.sensation;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.misc.NDFilter;
import jp.ac.fit.asura.nao.physical.Nao;
import jp.ac.fit.asura.nao.physical.Nao.Frames;

/**
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public class FrameState {
	private Frames id;
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

	// ロボット座標系からみたこのフレームの回転行列
	private Matrix3f robotRotation;

	// ロボット座標系からみたこのフレームの絶対座標
	private Vector3f robotPosition;

	/**
	 * 
	 */
	public FrameState(Frames id) {
		this.id = id;
		this.axisAngle = new AxisAngle4f(Nao.get(id).axis);
		this.rotation = new Matrix3f();
		this.position = new Vector3f(Nao.get(id).translate);
		this.robotPosition = new Vector3f();
		this.robotRotation = new Matrix3f();
		nd = new NDFilter.Float();
	}

	/**
	 * 関節状態を更新します.
	 * 
	 * @param value
	 */
	public void updateValue(float value) {
		assert id.isJoint();
		axisAngle.setAngle(value);
		dValue = nd.eval(value);
	}

	public void updateForce(float force) {
		assert id.isJoint();
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
		FrameState obj = new FrameState(id);
		// filterもDeep Copyすべきか?
		obj.nd = nd;
		obj.dValue = dValue;
		obj.force = force;
		// axisは不変だがangleはインスタンスによって変わるのでコピーする必要がある.
		obj.axisAngle.set(axisAngle);
		// 位置ベクトルは不変なので必要ないはず.
		// obj.position.set(position);
		obj.rotation.set(rotation);
		obj.robotPosition.set(robotPosition);
		obj.robotRotation.set(robotRotation);
		return obj;
	}

	/**
	 * @return the id
	 */
	public Frames getId() {
		return id;
	}

	public Vector3f getPosition() {
		return position;
	}

	/**
	 * @return the robotPosition
	 */
	public Vector3f getRobotPosition() {
		return robotPosition;
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
	public Matrix3f getRobotRotation() {
		return robotRotation;
	}
}
