/*
 * 作成日: 2008/06/29
 */
package jp.ac.fit.asura.nao.communication.messages;

/**
 * @author $Author: sey $
 * 
 * @version $Id$
 * 
 */
public abstract class AsuraMessage {
	public enum Type {
		NONE(0), WMOBJECT(1), STRATEGY(2), PENALTY(3), STATUS(4);

		private int type;

		private Type(int type) {
			this.type = type;
		}

		public int getType() {
			return type;
		}

		public static Type toType(int i) {
			for (Type t : values()) {
				if (t.type == i)
					return t;
			}
			return null;
		}
	}

	public AsuraMessage(int senderId, long frame) {

	}
}
