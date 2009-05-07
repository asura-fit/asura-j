/*
 * 作成日: 2008/07/09
 */
package jp.ac.fit.asura.nao.misc;

import jp.ac.fit.asura.nao.misc.Filter.BooleanFilter;
import jp.ac.fit.asura.nao.misc.Filter.FloatFilter;
import jp.ac.fit.asura.nao.misc.Filter.IntFilter;

/**
 * @author sey
 *
 * @version $Id: MeanFilter.java 691 2008-09-26 06:40:26Z sey $
 *
 */
public class MedianFilter {
	protected int tail;
	protected int length;
	protected int size;

	public MedianFilter(int size) {
		tail = length = 0;
		this.size = size;
	}

	protected int current() {
		return (tail + length) % size;
	}

	protected void next() {
		if (length >= size)
			tail = (tail + 1) % size;
		else
			length++;
	}

	public void clear() {
		tail = length = 0;
	}

	public boolean isFilled() {
		return length == size;
	}

	public static class Boolean extends MedianFilter implements BooleanFilter {
		private boolean[] state;

		public Boolean(int size) {
			super(size);
			state = new boolean[size];
		}

		public void eval() {
			next();
			if (length > 0)
				length--;
		}

		public boolean eval(boolean value) {
			// フィルタを更新
			state[current()] = value;
			next();
			return value();
		}

		public boolean value() {
			int count = 0;
			for (int i = 0; i < length; i++) {
				if (state[current()])
					count++;
			}

			if (length <= 0) {
				assert false;
				return false;
			}

			return count > length / 2;
		}
	}

	public static class Int extends MedianFilter implements IntFilter {
		private int[] state;

		public Int(int size) {
			super(size);
			state = new int[size];
		}

		public void eval() {
			next();
			if (length > 0)
				length--;
		}

		public int eval(int value) {
			// フィルタを更新
			state[current()] = value;
			next();
			return value();
		}

		public int value() {
			int value = 0;
			for (int i = 0; i < length; i++) {
				value += state[current()];
			}

			if (length <= 0) {
				assert false;
				return Integer.MIN_VALUE;
			}

			return value / length;
		}
	}

	public static class Float extends MedianFilter implements FloatFilter {
		private float[] state;

		public Float(int size) {
			super(size);
			state = new float[size];
		}

		public void eval() {
			next();
			if (length > 0)
				length--;
		}

		public float eval(float value) {
			// フィルタを更新
			state[current()] = value;
			next();
			return value();
		}

		public float value() {
			float value = 0;
			for (int i = 0; i < length; i++) {
				value += state[current()];
			}

			if (length <= 0) {
				assert false;
				return java.lang.Float.NaN;
			}

			return value / length;
		}
	}
}
