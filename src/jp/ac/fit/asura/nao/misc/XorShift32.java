/*
 * 作成日: 2009/11/25
 */
package jp.ac.fit.asura.nao.misc;

import java.util.Random;

import javax.vecmath.Point2f;

/**
 * 32bitのXOR-SHIFTによる疑似乱数生成器.
 *
 * Java標準の乱数(48bit線形合同法)より3倍ぐらい速い.
 *
 * @author sey
 *
 * @version $Id: $
 *
 */
public class XorShift32 extends Random {
	private int x;

	public XorShift32() {
		x = (int) (System.nanoTime() & 0xFFFFFFFF);
	}

	public XorShift32(int seed) {
		this.x = seed;
	}

	public int xorshift() {
		x ^= (x << 1);
		x ^= (x >>> 5);
		x ^= (x << 9);
		return x;
	}

	protected int next(int bits) {
		return xorshift() >>> (Integer.SIZE - bits);
	}

	// ボックス・ミュラー法による正規分布の作成
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

	float nextGaussian;
	boolean hasNextGaussian;

	public synchronized float nextGaussianFloat2() {
		if (hasNextGaussian) {
			hasNextGaussian = false;
			return nextGaussian;
		}
		hasNextGaussian = true;
		float v1, v2, s;
		do {
			v1 = 2 * nextFloat() - 1; // between -1 and 1
			v2 = 2 * nextFloat() - 1; // between -1 and 1
			s = v1 * v1 + v2 * v2;
		} while (s >= 1 || s == 0);
		float multiplier = (float) Math.sqrt(-2 * (float) Math.log(s) / s);
		nextGaussian = v2 * multiplier;
		return v1 * multiplier;
	}

	@Override
	public void setSeed(long seed) {
		x = (int) (seed & 0xFFFFFFFF);
	}
}
