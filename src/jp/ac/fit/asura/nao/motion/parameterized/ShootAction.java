/*
 * 作成日: 2008/07/05
 */
package jp.ac.fit.asura.nao.motion.parameterized;

import static java.lang.Math.PI;
import static java.lang.Math.asin;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static jp.ac.fit.asura.nao.misc.MathUtils.clipAbs;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.MotionFactory;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.motion.MotorCortex;
import jp.ac.fit.asura.nao.motion.MotionFactory.Compatible.CompatibleMotion;

import org.apache.log4j.Logger;

/**
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public abstract class ShootAction extends ParameterizedAction {
	protected Logger log = Logger.getLogger(getClass());

	protected MotorCortex motor;

	public void init(RobotContext context) {
		motor = context.getMotor();
	}

	public static class LeftShootAction extends ShootAction {

		public int getId() {
			return Motions.ACTION_SHOOT_LEFT;
		}

		public String getName() {
			return "LeftShootAction";
		}

		public Motion create(int x, int y) {
			CompatibleMotion left = (CompatibleMotion) motor
					.getMotion(Motions.MOTION_KAKICK_LEFT);

			float[][] frames = left.frames.clone();
			int[] steps = left.steps;

			float t = (float) max(toDegrees(asin(clipAbs(-(x - 10) / 350.0,
					sin(PI / 4)))) + 5, -5);
			log.debug(t);

			frames[2][Joint.LHipRoll.ordinal()] = (15 + t) / 2;
			frames[3][Joint.LHipRoll.ordinal()] = t;
			frames[4][Joint.LHipRoll.ordinal()] = t;
			frames[5][Joint.LHipRoll.ordinal()] = t;
			Motion motion = MotionFactory.Compatible.create(frames, steps);
			motion.setName("LeftShootMotion");
			return motion;
		}
	}

	public static class RightShootAction extends ShootAction {
		public int getId() {
			return Motions.ACTION_SHOOT_RIGHT;
		}

		public String getName() {
			return "RightShootAction";
		}

		public Motion create(int x, int y) {
			CompatibleMotion right = (CompatibleMotion) motor
					.getMotion(Motions.MOTION_KAKICK_RIGHT);

			float[][] frames = right.frames.clone();
			int[] steps = right.steps;

			float t = (float) min(toDegrees(asin(clipAbs(-(x + 10) / 350.0,
					sin(PI / 4)))) - 5, 5);
			log.debug(t);

			frames[2][Joint.RHipRoll.ordinal()] = (-15 + t) / 2;
			frames[3][Joint.RHipRoll.ordinal()] = t;
			frames[4][Joint.RHipRoll.ordinal()] = t;
			frames[5][Joint.RHipRoll.ordinal()] = t;

			Motion motion = MotionFactory.Compatible.create(frames, steps);
			motion.setName("RightShootMotion");
			return motion;
		}
	}
}
