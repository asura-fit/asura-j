/*
 * 作成日: 2009/11/25
 */
package jp.ac.fit.asura.nao.misc;

import java.util.Random;

import javax.vecmath.Point2f;

/**
 * @author sey
 *
 * @version $Id: $
 *
 */
public class XorShift64 extends Random {
	private long x;

	public XorShift64() {
		x = System.nanoTime();
	}

	public XorShift64(long seed) {
		this.x = seed;
	}

	public long xorshift() {
		x ^= (x << 21);
		x ^= (x >>> 35);
		x ^= (x << 4);
		return x;
	}

	protected int next(int bits) {
		return (int) (xorshift() >>> (Long.SIZE - bits));
	}

	public void nextGaussian(Point2f p) {
		float v1, v2, s;
		do {
			v1 = 2 * nextFloat() - 1; // between -1 and 1
			v2 = 2 * nextFloat() - 1; // between -1 and 1
			s = v1 * v1 + v2 * v2;
		} while (s >= 1 || s == 0);
		float multiplier = (float) Math.sqrt(-2 * (float) Math.log(s) / s);
		p.x = v1 * multiplier;
		p.y = v2 * multiplier;
	}

	public float nextGaussianFloat() {
		float v1, v2, s;
		do {
			v1 = 2 * nextFloat() - 1; // between -1 and 1
			v2 = 2 * nextFloat() - 1; // between -1 and 1
			s = v1 * v1 + v2 * v2;
		} while (s >= 1 || s == 0);
		float multiplier = (float) Math.sqrt(-2 * (float) Math.log(s) / s);
		return v1 * multiplier;
	}

	float nextg;
	boolean hasnext;

	public float nextGaussianFlaot2() {
		if (hasnext) {
			hasnext = false;
			return nextg;
		}
		float v1, v2, s;
		do {
			v1 = 2 * nextFloat() - 1; // between -1 and 1
			v2 = 2 * nextFloat() - 1; // between -1 and 1
			s = v1 * v1 + v2 * v2;
		} while (s >= 1 || s == 0);
		float multiplier = (float) Math.sqrt(-2 * (float) Math.log(s) / s);
		nextg = v2 * multiplier;
		hasnext = true;
		return v1 * multiplier;
	}

	@Override
	public void setSeed(long seed) {
		x = seed;
	}
}
