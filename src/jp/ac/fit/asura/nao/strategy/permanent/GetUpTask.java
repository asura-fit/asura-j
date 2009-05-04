/*
 * 作成日: 2008/06/15
 */
package jp.ac.fit.asura.nao.strategy.permanent;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.SensorContext;
import jp.ac.fit.asura.nao.communication.RoboCupGameControlData;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 *
 * @version $Id: GetUpTask.java 717 2008-12-31 18:16:20Z sey $
 *
 */
public class GetUpTask extends Task {
	private Logger log = Logger.getLogger(GetUpTask.class);

	private boolean doStandup;
	private boolean active;
	private int fallDownCount;

	public void init(RobotContext context) {
		active = false;
	}

	public boolean canExecute(StrategyContext context) {
		return true;
	}

	public void after(StrategyContext context) {
		if (active)
			return;

		SensorContext sensor = context.getSensorContext();
		float ax = sensor.getAccelX();
		float ay = sensor.getAccelY();
		float az = sensor.getAccelZ();

		if (ay < 4.0 && (Math.abs(ax) > 8.0 || Math.abs(az) > 8.0)) {
			fallDownCount++;

			byte state = context.getGameState().getState();
			if (state == RoboCupGameControlData.STATE_INITIAL) {
				// Initial状態では起き上がり禁止.
				// FIXME ペナライズ状態でも禁止にする.
				return;
			}

			if (fallDownCount > 5) {
				log.info("Fall down state detected." + " x:" + ax + " y:" + ay
						+ " z:" + az);
				context.getScheduler().preempt(this);
			}
		} else {
			fallDownCount = 0;
		}
	}

	public void continueTask(StrategyContext context) {
		SensorContext sensor = context.getSensorContext();
		float ax = sensor.getAccelX();
		float ay = sensor.getAccelY();
		float az = sensor.getAccelZ();

		if (ay > 9.0f) {
			// 重力が下にかかるようになったら抜ける
			// context.getSuperContext().getEffector().setPower(0.5f);
			context.makemotion(Motions.NULL);
			context.makemotion_head(0.0f, 0.0f);
			context.getScheduler().setTTL(0);
			return;
		}
		// 起き上がり中は頭を動かさない
		context.makemotion_head(0.0f, 0.0f);
		context.getScheduler().setTTL(20);

		// if (context.hasMotion(Motions.NAOJI_WALKER))
		// context.getSuperContext().getEffector().setPower(0.125f);

		int motion = -1;
		// TODO WebotsでもCHORE_FROM_BACKでもいいかも?
		// 逆さになってる
		if (ay < -6.0f) {
			log.info("Getup from ???");
			if (!context.hasMotion(Motions.NAOJI_WALKER))
				motion = Motions.MOTION_YY_GETUP_BACK;
			else
				motion = Motions.CHORE_FROM_BACK;
		} else if (az > 5.0f) {
			log.info("Getup from face-up");
			// 顔が上
			if (!context.hasMotion(Motions.NAOJI_WALKER))
				motion = Motions.MOTION_YY_GETUP_BACK;
			else
				motion = Motions.CHORE_FROM_BACK;
		} else if (az < -5.0f) {
			log.info("Getup from face-down");
			// 背中側が上
			if (!context.hasMotion(Motions.NAOJI_WALKER))
				motion = Motions.MOTION_W_GETUP;
			else
				motion = Motions.CHORE_FROM_FRONT;
		} else if (Math.abs(ax) > 5.0f) {
			log.info("Getup from face sideways. ax:" + ax);
			// 横?
			if (!context.hasMotion(Motions.NAOJI_WALKER))
				motion = Motions.MOTION_W_GETUP;
		}

		if (motion != -1 && doStandup)
			context.makemotion(motion);
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(20);
		active = true;
		Object v = context.getSuperContext().getGlue().getValue(
				"ss-getup-standup");
		doStandup = (v != null && Boolean.TRUE == v);
	}

	public String getName() {
		return "GetUpTask";
	}

	public void leave(StrategyContext context) {
		active = false;
		fallDownCount = 0;
	}
}
