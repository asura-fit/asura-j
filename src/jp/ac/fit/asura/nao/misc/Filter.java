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
	public interface BooleanFilter extends Filter {
		public boolean eval(boolean value);

		public boolean value();
	}

	public interface IntFilter extends Filter {
		public int eval(int value);

		public void eval();

		public int value();
	}

	public interface FloatFilter extends Filter {
		public float eval(float value);

		public void eval();

		public float value();
	}

	public void clear();

	public boolean isFilled();
}