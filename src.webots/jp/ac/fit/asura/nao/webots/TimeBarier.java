/*
 * 作成日: 2009/04/23
 */
package jp.ac.fit.asura.nao.webots;

/**
 * @author sey
 *
 * @version $Id: $
 *
 */
public class TimeBarier {
	private long current;

	public synchronized void waitTime(long keyTime) {
		if (keyTime > current) {
			current = keyTime;
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
	}

	public synchronized void notifyTime(long keyTime) {
		if (keyTime >= current) {
			notifyAll();
			current = keyTime;
		}
	}
}
