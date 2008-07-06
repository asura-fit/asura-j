/*
 * 作成日: 2008/07/05
 */
package jp.ac.fit.asura.nao.motion.parameterized;

import org.apache.log4j.Logger;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.MotionFactory;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.motion.MotorCortex;
import jp.ac.fit.asura.nao.motion.MotionFactory.Compatible.CompatibleMotion;

/**
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public class RightShootAction extends ParameterizedAction {
	private Logger log = Logger.getLogger(RightShootAction.class);

	private MotorCortex motor;

	public int getId() {
		return Motions.ACTION_SHOOT_RIGHT;
	}

	public String getName() {
		return "RightShootAction";
	}

	public void init(RobotContext context) {
		motor = context.getMotor();
	}

	public Motion create(int x, int y) {
		CompatibleMotion right = (CompatibleMotion) motor
				.getMotion(Motions.MOTION_KAKICK_RIGHT);

		float[][] frames = right.frames.clone();
		int[] steps = right.steps;

		int t = MathUtils.clipping(-x / 5, -70, -15);
		log.debug(t);

		frames[2][Joint.RHipRoll.ordinal()] = (-15 + t) / 2;
		frames[3][Joint.RHipRoll.ordinal()] = t;
		frames[4][Joint.RHipRoll.ordinal()] = t;
		frames[5][Joint.RHipRoll.ordinal()] = t;

		return MotionFactory.Compatible.create(frames, steps);
	}
}
