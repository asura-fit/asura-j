/*
 * 作成日: 2008/07/09
 */
package jp.ac.fit.asura.nao.misc;

/**
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public class MeanFilter {
	public static class Int implements IntFilter {
		private int[] state;
		private int tail;
		private int length;

		public Int(int size) {
			state = new int[size];
			tail = length = 0;
		}

		public void eval() {
			tail = (tail + 1) % state.length;
			if (length > 0)
				length--;
		}

		public int eval(int value) {
			// フィルタを更新
			state[(tail + length) % state.length] = value;
			if (length < state.length)
				length++;
			else
				tail = (tail + 1) % state.length;
			return value();
		}

		public int value() {
			int value = 0;
			for (int i = 0; i < length; i++) {
				value += state[(tail + i) % state.length];
			}

			if (length <= 0) {
				assert false;
				return Integer.MIN_VALUE;
			}

			return value / length;
		}
	}
}
