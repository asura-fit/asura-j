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
public class LeftShootAction extends ParameterizedAction {
	private Logger log = Logger.getLogger(LeftShootAction.class);

	private MotorCortex motor;

	public int getId() {
		return Motions.ACTION_SHOOT_LEFT;
	}

	public String getName() {
		return "LeftShootAction";
	}

	public void init(RobotContext context) {
		motor = context.getMotor();
	}

	public Motion create(int x, int y) {
		CompatibleMotion left = (CompatibleMotion) motor
				.getMotion(Motions.MOTION_KAKICK_LEFT);

		float[][] frames = left.frames.clone();
		int[] steps = left.steps;

		int t = MathUtils.clipping(-x / 5, 15, 70);
		log.debug(t);

		frames[2][Joint.LHipRoll.ordinal()] = (15 + t) / 2;
		frames[3][Joint.LHipRoll.ordinal()] = t;
		frames[4][Joint.LHipRoll.ordinal()] = t;
		frames[5][Joint.LHipRoll.ordinal()] = t;

		return MotionFactory.Compatible.create(frames, steps);
	}

}
