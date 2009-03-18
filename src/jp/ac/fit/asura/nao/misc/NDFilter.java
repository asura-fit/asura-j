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
 * @version $Id: NDFilter.java 709 2008-11-23 07:40:31Z sey $
 * 
 */
public class NDFilter {
	private static final int SIZE = 3;
	protected int length;

	public static class Float extends NDFilter implements FloatFilter {
		private float x1;
		private float x2;
		private float x3;

		public Float() {
			super();
			length = 0;
		}

		public void eval() {
			throw new UnsupportedOperationException();
		}

		public float eval(float value) {
			push(value);
			if (SIZE > length)
				return java.lang.Float.NaN;
			return value();
		}

		public float value() {
			return (x1 - 4 * x2 + 3 * x3) / (2.0f);
		}

		private void push(float value) {
			assert length >= 0 && length <= SIZE;
			if (length == SIZE) {
				x1 = x2;
				x2 = x3;
				x3 = value;
				return;
			}
			switch (length++) {
			case 0:
				x1 = value;
				return;
			case 1:
				x2 = value;
				return;
			case 2:
				x3 = value;
				return;
			}
		}
	}
}
