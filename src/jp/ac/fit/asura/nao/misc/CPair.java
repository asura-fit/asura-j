/*
 * 作成日: 2008/10/01
 */
package jp.ac.fit.asura.nao.misc;

/**
 * Chained Pair.
 * 
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public class CPair<T1> {
	private T1 first;
	private CPair<T1> second;

	public CPair(T1 first, CPair<T1> second) {
		this.first = first;
		this.second = second;
	}

	public T1 first() {
		return first;
	}

	public CPair<T1> second() {
		return second;
	}
}
