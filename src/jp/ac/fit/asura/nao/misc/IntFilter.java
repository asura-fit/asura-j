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
public interface IntFilter {
	public int eval(int value);

	public void eval();

	public int value();
}
