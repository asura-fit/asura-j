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
	private NDFilter.Float dx;
	private float value;
	private float dv;

	public JointState(Joint id) {
		this.id = id;
		dx = new NDFilter.Float();
	}

	public void updateValue(float value) {
		this.value = value;
		dv = dx.eval(value);
	}

	/**
	 * この関節状態の浅い(Shallow)コピーを作成します.
	 * 
	 * 関節値，微分値は複製されますが，微分フィルタはコピーされたインスタンスとの間で共有されるため，取り扱いには注意が必要です.
	 */
	public Object clone() throws CloneNotSupportedException {
		JointState c = new JointState(id);
		c.value = value;
		c.dv = dv;
		return c;
	}

	public float getValue() {
		return value;
	}

	public float getDValue() {
		return dv;
	}

	/**
	 * @return the id
	 */
	public Joint getId() {
		return id;
	}
}
