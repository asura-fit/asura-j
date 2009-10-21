/**
 *
 */
package jp.ac.fit.asura.nao.vision.perception;

import java.util.Arrays;

import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.vision.GCD;

/**
 * @author sey
 *
 */
public class HoughVision extends AbstractVision {
	public static final int THETA_MAX = 72;
	public static final int RHO_SCALE = 2;
	public static final int RHO_MAX = 160 / RHO_SCALE;
	private byte[] th_rho;
	private float[] c;
	private float[] s;

	public HoughVision() {
		th_rho = new byte[THETA_MAX * RHO_MAX];
		s = new float[THETA_MAX];
		c = new float[THETA_MAX];
		for (int th = 0; th < THETA_MAX; th++) {
			c[th] = MathUtils.cos(th * MathUtils.toRadians(360 / THETA_MAX));
			s[th] = MathUtils.sin(th * MathUtils.toRadians(360 / THETA_MAX));
		}
	}

	public void process() {
		getContext().houghPlane = th_rho;
		if (getContext().getFrameContext().getFrame() % 5 != 0)
			return;
		byte[] plane = getContext().gcdPlane;
		int width = getContext().image.getWidth();
		int height = getContext().image.getHeight();

		Arrays.fill(th_rho, (byte) 0);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int i = y * width + x;

				if (plane[i] == GCD.cWHITE) {
					for (int th = 0; th < THETA_MAX; th++) {
						int rho = (int) (x * c[th] + y * s[th]) / RHO_SCALE;
						if (th_rho[th * RHO_MAX + rho] != -1)
							th_rho[th * RHO_MAX + rho]++;
					}
				}
			}
		}

		for (int th = 0; th < THETA_MAX; th++) {
			for (int rho = 0; rho < RHO_MAX; rho++) {
				if ((th_rho[th * RHO_MAX + rho] & 0xFF) > 128)
					System.out.println("th:" + (th * (360 / THETA_MAX))
							+ " rho:" + rho + " v:"
							+ (th_rho[th * RHO_MAX + rho] & 0xFF));
			}
		}
	}
}
