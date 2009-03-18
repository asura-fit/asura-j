/*
 * 作成日: 2008/06/11
 */
package jp.ac.fit.asura.nao.motion;

import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.physical.RobotFrame;

/**
 * @author sey
 * 
 * @version $Id: MotionUtils.java 717 2008-12-31 18:16:20Z sey $
 * 
 */
public class MotionUtils {
	/**
	 * 指定した関節の角度(rad)を制限値以内にクリッピングします.
	 * 
	 * @param angle
	 * @param joint
	 * @return
	 */
	public static float clipping(RobotFrame joint, float angle) {
		return MathUtils.clipping(angle, joint.getMinAngle(), joint
				.getMaxAngle());
	}

	public static float clippingDeg(RobotFrame joint, float angleInDegrees) {
		return MathUtils.clipping(angleInDegrees, joint.getMinAngleDeg(), joint
				.getMaxAngleDeg());
	}

	public static boolean canMoveDeg(RobotFrame joint, float targetPositionDeg,
			float currentPositionDeg) {
		if (targetPositionDeg >= joint.getMaxAngleDeg()) {
			return Math.abs(currentPositionDeg - joint.getMaxAngleDeg()) > 1.0f;
		} else if (targetPositionDeg <= joint.getMinAngleDeg()) {
			return Math.abs(currentPositionDeg - joint.getMinAngleDeg()) > 1.0f;
		}
		return true;
	}

	public static boolean isInRange(RobotFrame joint, float rad) {
		return rad <= joint.getMaxAngle() && rad >= joint.getMinAngle();
	}
}
