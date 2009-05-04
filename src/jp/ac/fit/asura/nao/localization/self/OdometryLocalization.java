/**
 *
 */
package jp.ac.fit.asura.nao.localization.self;

import static jp.ac.fit.asura.nao.misc.MathUtils.normalizeAnglePI;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.VisualFrameContext;
import jp.ac.fit.asura.nao.misc.MathUtils;

/**
 * オドメトリ(推定された移動量)によってのみ自己位置を決定するLocalization. 実験用.
 *
 * @author sey
 *
 */
public class OdometryLocalization extends SelfLocalization {
	private float h;
	private float x;
	private float y;

	@Override
	public int getConfidence() {
		return 10;
	}

	@Override
	public float getHeading() {
		return h;
	}

	@Override
	public int getX() {
		return (int) x;
	}

	@Override
	public int getY() {
		return (int) y;
	}

	@Override
	public void reset() {
		x = y = h = 0;
	}

	@Override
	public void step(VisualFrameContext visualFrame) {
	}

	@Override
	public void init(RobotContext rctx) {
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	@Override
	public void updateOdometry(float forward, float left, float turnCCW) {
		x += MathUtils.sin(h) * forward + MathUtils.cos(h) * left;
		y += MathUtils.cos(h) * forward - MathUtils.sin(h) * left;
		h = normalizeAnglePI(h + turnCCW);
	}
}
