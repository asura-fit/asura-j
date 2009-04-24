/*
 * 作成日: 2008/06/15
 */
package jp.ac.fit.asura.nao.strategy.permanent;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.SensorContext;
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

		if (ay < 3.0 && (Math.abs(ax) > 5.0 || Math.abs(az) > 5.0)) {
			fallDownCount++;
			if (fallDownCount > 5) {
				log.info("Fall down state detected." + " x:" + ax + " y:" + ay
						+ " z:" + az);
				if (!context.hasMotion(Motions.NAOJI_WALKER))
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

		if (ay > 9.5) {
			// 重力が下にかかるようになったら抜ける
			context.makemotion(Motions.MOTION_STOP);
			context.makemotion_head(0.0f, 0.0f);
			return;
		}
		// 起き上がり中は頭を動かさない
		context.makemotion_head(0.0f, 0.0f);
		context.getScheduler().setTTL(20);

		// 逆さになってる
		if (ay < -6.0) {
			log.info("Getup from ???");
			context.makemotion(Motions.MOTION_YY_GETUP_BACK);
		} else if (az > 5.0) {
			log.info("Getup from face-up");
			// 顔が上
			context.makemotion(Motions.MOTION_YY_GETUP_BACK);
		} else if (az < -5.0) {
			log.info("Getup from face-down");
			// 背中側が上
			context.makemotion(Motions.MOTION_W_GETUP);
		} else if (Math.abs(ax) > 5.0) {
			log.info("Getup from face sideways. ax:" + ax);
			// 横?
			context.makemotion(Motions.MOTION_W_GETUP);
		}
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(20);
		active = true;
	}

	public String getName() {
		return "GetUpTask";
	}

	public void leave(StrategyContext context) {
		active = false;
		fallDownCount = 0;
	}
}
