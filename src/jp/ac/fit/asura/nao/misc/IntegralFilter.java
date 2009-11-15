/*
 * 作成日: 2008/09/24
 */
package jp.ac.fit.asura.nao.misc;

import jp.ac.fit.asura.nao.misc.Filter.FloatFilter;

/**
 * 合成シンプソン公式による数値積分フィルタ.
 *
 * h := 1
 *
 * 最大誤差 := -h^4/180*(b-a)*f''''(x)
 *
 * @author $Author: sey $
 *
 * @version $Id: $
 *
 */
public class IntegralFilter {
	protected int length;

	public void clear() {
		length = 0;
	}

	public boolean isFilled() {
		return length != 0;
	}

	public static class Float extends IntegralFilter implements FloatFilter {
		private float first;
		private float last;
		private float even;
		private float odd;
		private float lastValue;

		public Float() {
			super();
			length = 0;
			even = odd = 0;
		}

		@Override
		public void clear() {
			super.clear();
			even = odd = 0;
		}

		@Override
		public void eval() {
			throw new UnsupportedOperationException();
		}

		@Override
		public float eval(float value) {
			if (length == 0) {
				first = value;
			}
			if (length % 2 == 0) {
				last = value;
				lastValue = (first + last + even * 2 + odd * 4) / 3;
				even += value;
			} else {
				// 合成シンプソン公式は偶数個の要素のみなので、台形公式で近似
				lastValue += (last + value) / 2;
				odd += value;
			}
			length++;
			return lastValue;
		}

		public float value() {
			return lastValue;
		}
	}
}
