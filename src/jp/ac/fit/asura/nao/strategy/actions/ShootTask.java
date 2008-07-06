/*
 * 作成日: 2008/07/07
 */
package jp.ac.fit.asura.nao.strategy.actions;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.motion.parameterized.LeftShootAction;
import jp.ac.fit.asura.nao.motion.parameterized.RightShootAction;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class ShootTask extends Task {
	private Logger log = Logger.getLogger(ShootTask.class);

	private LeftShootAction left;
	private RightShootAction right;

	public String getName() {
		return "ShootTask";
	}

	public boolean canExecute(StrategyContext context) {
		WorldObject ball = context.getBall();
		if (ball.getConfidence() < 100)
			return false;
		if (ball.getDistance() > 400) {
			return false;
		}
		return true;
	}

	public void init(RobotContext context) {
		left = (LeftShootAction) context.getMotor().getParaAction(
				Motions.ACTION_SHOOT_LEFT);
		right = (RightShootAction) context.getMotor().getParaAction(
				Motions.ACTION_SHOOT_RIGHT);
		assert left != null;
		assert right != null;
	}

	public void enter(StrategyContext context) {
		WorldObject ball = context.getBall();

		// キックする足．1が左，-1が右
		int kickSide = 0;

		// シュートのプランニング
		// SomatoSensoryCortex ssc =
		// context.getSuperContext().getSensoryCortex();
		// Vector4f position = ssc.getBallPosition();
		// if (position.w > 100) {
		// log.trace("use SensoryCortex info");
		// kickSide = position.x > 0 ? -1 : 1;
		// }

		Motion motion;
		int x = ball.getX() - context.getSelf().getX();
		int y = ball.getY() - context.getSelf().getY();
		if (ball.getHeading() > 0) {
			motion = left.create(x, y);
		} else {
			motion = right.create(x, y);
		}
		context.makemotion(motion);
		context.getScheduler().setTTL(motion.getTotalFrames()/2);
	}

	public void continueTask(StrategyContext context) {

	}
}
