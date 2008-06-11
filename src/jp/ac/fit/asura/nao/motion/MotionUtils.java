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

	/**
	 * 指定した関節の角度を制限値以内にクリッピングします.
	 * @param angleInDeg180
	 * @param joint
	 * @return
	 */
	public static float clipping(float angleInDeg180, Joint joint) {
		return MathUtils.clipping(angleInDeg180, getMinAngle(joint),
				getMaxAngle(joint));
	}

	public static float getMaxAngle(Joint joint) {
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

	public static float getMinAngle(Joint joint) {
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
}
