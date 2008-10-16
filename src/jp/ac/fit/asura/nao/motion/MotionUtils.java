/*
 * 作成日: 2008/06/11
 */
package jp.ac.fit.asura.nao.motion;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.misc.MathUtils;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class MotionUtils {
	private static final float[] maxAnglesRad = new float[] { 1.0472f, // HeadYaw
			0.7854f, // HeadPitch

			2.0944f, // LShoulderPitch
			1.658f, // LShoulderRoll
			2.095f, // LElbowYaw
			0f, // LElbowRoll

			0.1745f, // LHipYawPitch, Webotsの定義ファイル上では 0.1754
			0.4363f,// LHipPitch
			0.7845f,// LHipRoll
			2.2689f, // LKneePitch
			0.5236f, // LAnklePitch
			0.3491f, // LAnkleRoll

			0.1745f, // RHipYawPitch
			0.4363f, // RHipPitch
			0.3491f,// RHipRoll
			2.2689f, // RKneePitch
			0.5236f, // RAnklePitch
			0.7854f, // RAnkleRoll

			2.0944f,// RShoulderPitch
			0.2618f,// RShoulderRoll
			2.095f,// RElbowYaw
			1.5709f, // RElbowRoll
	};

	private static final float[] minAnglesRad = new float[] { -1.0472f, // HeadYaw
			-0.7854f, // HeadPitch
			-2.0944f, // LShoulderPitch
			-0.2618f, // LShoulderRoll
			-2.095f, // LElbowYaw
			-1.5709f,// LElbowRoll
			-1.5708f, // LHipYawPitch
			-1.5708f,// LHipPitch
			-0.3491f, // LHipRoll
			0f, // LKneePitch
			-1.3963f, // LAnklePitch
			-0.7854f, // LAnkleRoll

			-1.5708f, // RHipYawPitch
			-1.5708f, // RHipPitch
			-0.7845f, // RHipRoll
			0, // RKneePitch
			-1.3963f, // RAnklePitch
			-0.3491f, // RAnkleRoll
			-2.0944f, // RShoulderPitch
			-1.658f,// RShoulderRoll
			-2.095f, // RElbowYaw
			0f // RElbowRoll
	};

	/**
	 * 指定した関節の角度(rad)を制限値以内にクリッピングします.
	 * 
	 * @param angle
	 * @param joint
	 * @return
	 */
	public static float clipping(Joint joint, float angle) {
		return MathUtils
				.clipping(angle, getMinAngle(joint), getMaxAngle(joint));
	}

	public static float clippingDeg(Joint joint, float angleInDegrees) {
		return MathUtils.clipping(angleInDegrees, getMinAngleDeg(joint),
				getMaxAngleDeg(joint));
	}

	public static float getMaxAngle(Joint joint) {
		return maxAnglesRad[joint.ordinal()];
	}

	public static float getMinAngle(Joint joint) {
		return minAnglesRad[joint.ordinal()];
	}

	/**
	 * 関節確度の最大値を返す. 若干ちがうかも.
	 * 
	 * @param joint
	 * @return
	 */
	public static float getMaxAngleDeg(Joint joint) {
		switch (joint) {
		case HeadYaw:
			return 120;
		case HeadPitch:
			return 45;
		case LShoulderPitch:
			return 120;
		case LShoulderRoll:
			return 95;
		case LElbowYaw:
			return 90;
		case LElbowRoll:
			return 120;
		case LHipYawPitch:
			return 0;
		case LHipPitch:
			return 25;
		case LHipRoll:
			return 45;
		case LKneePitch:
			return 130;
		case LAnklePitch:
			return 45;
		case LAnkleRoll:
			return 25;
		case RHipYawPitch:
			return 0;
		case RHipPitch:
			return 25;
		case RHipRoll:
			return 25;
		case RKneePitch:
			return 130;
		case RAnklePitch:
			return 45;
		case RAnkleRoll:
			return 45;
		case RShoulderPitch:
			return 120;
		case RShoulderRoll:
			return 0;
		case RElbowYaw:
			return 120;
		case RElbowRoll:
			return 0;
		default:
			// unknown joint
			assert false;
			return Float.MAX_VALUE;
		}
	}

	/**
	 * 関節確度の最小値を返す. 若干ちがうかも.
	 * 
	 * @param joint
	 * @return
	 */
	public static float getMinAngleDeg(Joint joint) {
		switch (joint) {
		case HeadYaw:
			return -120;
		case HeadPitch:
			return -45;
		case LShoulderPitch:
			return -120;
		case LShoulderRoll:
			return 0;
		case LElbowYaw:
			return 0;
		case LElbowRoll:
			return -120;
		case LHipYawPitch:
			return -90;
		case LHipPitch:
			return -100;
		case LHipRoll:
			return -25;
		case LKneePitch:
			return 0;
		case LAnklePitch:
			return -75;
		case LAnkleRoll:
			return -45;
		case RHipYawPitch:
			return -90;
		case RHipPitch:
			return -100;
		case RHipRoll:
			return -45;
		case RKneePitch:
			return 0;
		case RAnklePitch:
			return -75;
		case RAnkleRoll:
			return -25;
		case RShoulderPitch:
			return -120;
		case RShoulderRoll:
			return -95;
		case RElbowYaw:
			return -90;
		case RElbowRoll:
			return -120;
		default:
			// unknown joint
			assert false;
			return Float.MIN_VALUE;
		}
	}

	public static boolean canMoveDeg(Joint joint, float targetPositionDeg,
			float currentPositionDeg) {
		if (targetPositionDeg >= getMaxAngleDeg(joint)) {
			return Math.abs(currentPositionDeg - getMaxAngleDeg(joint)) > 1.0f;
		} else if (targetPositionDeg <= getMinAngleDeg(joint)) {
			return Math.abs(currentPositionDeg - getMinAngleDeg(joint)) > 1.0f;
		}
		return true;
	}

	public static boolean isInRange(Joint joint, float rad) {
		return rad <= getMaxAngle(joint) && rad >= getMinAngle(joint);
	}
}
