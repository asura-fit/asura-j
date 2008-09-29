/*
 * 作成日: 2008/09/26
 */
package jp.ac.fit.asura.nao.sensation;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.misc.NDFilter;

/**
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public class JointState {
	private Joint id;
	private NDFilter.Float nd;
	private float value;
	private float dValue;
	private float force;

	public JointState(Joint id) {
		this.id = id;
		nd = new NDFilter.Float();
	}

	/**
	 * 関節状態を更新します.
	 * 
	 * @param value
	 */
	public void updateValue(float value) {
		this.value = value;
		dValue = nd.eval(value);
	}

	public void updateForce(float force) {
		this.force = force;
	}

	/**
	 * この関節状態の浅い(Shallow)コピーを作成します.
	 * 
	 * 関節値，微分値は複製されますが，微分フィルタはコピーされたインスタンスとの間で共有されるため，取り扱いには注意が必要です.
	 */
	public JointState clone() {
		JointState c = new JointState(id);
		c.nd = nd;
		c.value = value;
		c.dValue = dValue;
		c.force = force;
		return c;
	}

	public float getValue() {
		return value;
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
	 * @return the id
	 */
	public Joint getId() {
		return id;
	}
}
