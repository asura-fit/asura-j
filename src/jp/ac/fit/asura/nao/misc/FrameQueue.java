/*
 * 作成日: 2009/04/22
 */
package jp.ac.fit.asura.nao.misc;

import jp.ac.fit.asura.nao.FrameContext;

/**
 * @author sey
 *
 * @version $Id: $
 *
 */
public class FrameQueue<T extends FrameContext> {
	private T[] array;
	private int capacity;
	private int size;
	private int lower;
	private int upper;

	/**
	 *
	 */
	public FrameQueue(int capacity) {
		array = (T[]) new FrameContext[capacity * 2];
		lower = 0;
		upper = 0;
		size = 0;
		this.capacity = capacity;
	}

	public T findNearest(long keyTime) {
		assert lower >= 0;
		assert upper <= array.length;
		assert upper >= lower;
		assert upper - lower == size;

		if (size == 0)
			return null;

		int low = lower;
		int high = upper - 1;

		while (low <= high) {
			int mid = (low + high) >>> 1;
			int cmp = (int) (array[mid].getTime() - keyTime);

			if (cmp < 0)
				low = mid + 1;
			else if (cmp > 0)
				high = mid - 1;
			else {
				// key found
				return array[mid];
			}
		}
		// key not found.
		if (low == lower)
			return array[low];
		if (low == upper)
			return array[low - 1];
		if (keyTime - array[low - 1].getTime() > array[low].getTime() - keyTime)
			return array[low];
		else
			return array[low - 1];
	}

	public T enqueue(T obj) {
		assert lower >= 0;
		assert upper <= array.length;
		assert upper >= lower;
		assert upper - lower == size;
		assert upper == 0 || array[upper - 1].getTime() < obj.getTime() : array[upper - 1]
				.getTime()
				+ ":" + obj.getTime();

		if (upper == array.length) {
			upper -= lower;
			for (int i = 0; i < upper; i++) {
				array[i] = array[lower + i];
			}
			lower = 0;
		}
		// 先頭の1要素分むだになる?
		array[upper++] = obj;
		if (size == capacity) {
			lower++;
			return array[lower - 1];
		} else {
			size++;
			return null;
		}
	}

	public T dequeue() {
		if (size == 0)
			return null;
		size--;
		return array[lower++];
	}
}
