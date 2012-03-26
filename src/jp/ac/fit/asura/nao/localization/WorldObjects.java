/*
 * 作成日: 2008/06/11
 */
package jp.ac.fit.asura.nao.localization;

/**
 * @author sey
 *
 * @version $Id: WorldObjects.java 624 2008-06-24 13:11:13Z sey $
 *
 */
public enum WorldObjects {
	Self(0), Ball(1), RedNao(2), BlueNao(3), YellowGoal(4), BlueGoal(5);

	private int type;

	WorldObjects(int num) {
		type = num;
	}

	/**
	 * 対応する番号を取得する.
	 * @return
	 */
	public int getWorldObjectsNum() {
		return type;
	}

	public static WorldObjects toWorldObjects(int num) {
		for (WorldObjects obj : WorldObjects.values()) {
			if (obj.type == num) {
				return obj;
			}
		}
		return null;
	}
}
