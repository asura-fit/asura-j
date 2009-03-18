/*
 * 作成日: 2008/06/11
 */
package jp.ac.fit.asura.nao.misc;

import java.util.Random;

/**
 * @author sey
 * 
 * @version $Id: MathUtils.java 717 2008-12-31 18:16:20Z sey $
 * 
 */
public class MathUtils {
	public static final float PIf = 3.14159265358979323846f;
	public static final float EPSf = 1e-6f;
	public static final float EPSd = 1e-6f;
	private static Random rand = new Random();

	/**
	 * valueの値をminからmaxの間にクリッピングします.
	 * 
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	public static float clipping(float value, float min, float max) {
		if (value > max)
			return max;
		if (value < min)
			return min;
		return value;
	}

	public static double clipping(double value, double min, double max) {
		if (value > max)
			return max;
		if (value < min)
			return min;
		return value;
	}

	public static int clipping(int value, int min, int max) {
		if (value > max)
			return max;
		if (value < min)
			return min;
		return value;
	}

	/**
	 * valueの絶対値がabsMaxより大きければ，クリッピングして返します. このとき，valueの符号は保存されます.
	 */
	public static float clipAbs(float value, float absMax) {
		absMax = Math.abs(absMax);
		if (Math.abs(value) > absMax)
			return Math.signum(value) * absMax;
		return value;
	}

	public static double clipAbs(double value, double absMax) {
		absMax = Math.abs(absMax);
		if (Math.abs(value) > absMax)
			return Math.signum(value) * absMax;
		return value;
	}

	/**
	 * angleを-180度以上～180度未満の間に正規化します.
	 * 
	 * @param angle
	 * @return
	 */
	public static float normalizeAngle180(float angle) {
		while (angle >= 180)
			angle -= 360;
		while (angle < -180)
			angle += 360;
		return angle;
	}

	public static int normalizeAngle180(int angle) {
		while (angle >= 180)
			angle -= 360;
		while (angle < -180)
			angle += 360;
		return angle;
	}

	/**
	 * angleを0度以上～360度未満の間に正規化します.
	 * 
	 * @param angle
	 * @return
	 */
	public static float normalizeAngle360(float angle) {
		while (angle >= 360)
			angle -= 360;
		while (angle < 0)
			angle += 360;
		return angle;
	}

	public static float normalizeAnglePI(float angle) {
		while (angle >= Math.PI)
			angle -= 2 * Math.PI;
		while (angle < -Math.PI)
			angle += 2 * Math.PI;
		return angle;
	}

	public static int square(int x) {
		return x * x;
	}

	public static float square(float x) {
		return x * x;
	}

	public static double square(double x) {
		return x * x;
	}

	public static int rand(int min, int maxExclusive) {
		return rand.nextInt(maxExclusive - min) + min;
	}

	public static double rand(double min, double maxExclusive) {
		return rand.nextDouble() * (maxExclusive - min) + min;
	}

	public static double gaussian(double avg, double sd) {
		return rand.nextGaussian() * sd + avg;
	}

	public static boolean epsEquals(float f1, float f2) {
		assert !Float.isNaN(f1);
		assert !Float.isNaN(f2);
		return Math.abs(f1 - f2) < EPSf;
	}

	public static boolean epsEquals(double f1, double f2) {
		assert !Double.isNaN(f1);
		assert !Double.isNaN(f2);
		return Math.abs(f1 - f2) < EPSd;
	}

	public static float toDegrees(float angrad) {
		return angrad * 180.0f / PIf;
	}
}
