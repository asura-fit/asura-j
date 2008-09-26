/*
 * 作成日: 2008/07/09
 */
package jp.ac.fit.asura.nao.misc;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public interface Filter {
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