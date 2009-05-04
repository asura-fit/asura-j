/*
 * 作成日: 2008/07/09
 */
package jp.ac.fit.asura.nao.misc;

/**
 * @author sey
 *
 * @version $Id: Filter.java 691 2008-09-26 06:40:26Z sey $
 *
 */
public interface Filter {
	public interface BooleanFilter {
		public boolean eval(boolean value);

		public boolean value();
	}

	public interface IntFilter {
		public int eval(int value);

		public void eval();

		public int value();
	}

	public interface FloatFilter {
		public float eval(float value);

		public void eval();

		public float value();
	}
}