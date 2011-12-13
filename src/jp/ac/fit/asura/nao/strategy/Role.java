/*
 * 作成日: 2008/06/12
 */
package jp.ac.fit.asura.nao.strategy;

/**
 *
 * 役割(ロール)を表現します.
 *
 * Goalie, Striker, Libero, Defenderなど.
 *
 * @author $Author: sey $
 *
 * @version $Id: Role.java 709 2008-11-23 07:40:31Z sey $
 *
 */
public enum Role {
	Goalie(0), Striker(1), Libero(2), Defender(3);

	private int num;

	private Role(int n) {
		num = n;
	}

	/**
	 * Role番号を取得.
	 *
	 * @return Roleごとの番号（Goalie=0, Striker=1など）
	 * @author takata
	 */
	public int getRoleNum() {
		return num;
	}
}
