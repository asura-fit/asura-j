/*
 * 作成日: 2008/09/24
 */
package jp.ac.fit.asura.nao.misc;

import jp.ac.fit.asura.nao.misc.Filter.FloatFilter;

/**
 * Three Points Numeric Differentiation Filter.
 * 
 * 3点公式のみを使う暫定実装.
 * 
 * 評価が三回未満の場合はNaNを返す.
 * 
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class NDFilter {
	protected int tail;
	protected int length;
	protected int size;

	public static class Float extends NDFilter implements FloatFilter {
		private float[] state;

		public Float() {
			super();
			size = 3;
			tail = length = 0;
			state = new float[size];
		}

		public void eval() {
		}

		public float eval(float value) {
			push(value);
			if (size > length)
				return java.lang.Float.NaN;
			return value();
		}

		public float value() {
			return (state[last(2)] - 4 * state[last(1)] + 3 * state[last(0)]) / (2.0f);
		}

		private void push(float value) {
			state[tail] = value;
			tail++;
			tail %= size;
			if (length < size)
				length++;
		}

		private int last(int i) {
			// s = 7 t = 1
			// r = 5 i = 3
			int index = tail - 1 - i;
			while (index < 0)
				index += size;
			return index;
		}
	}
}
