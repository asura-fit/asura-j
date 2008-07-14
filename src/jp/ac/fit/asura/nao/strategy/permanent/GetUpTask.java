/*
 * 作成日: 2008/06/15
 */
package jp.ac.fit.asura.nao.strategy.permanent;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;

import org.apache.log4j.Logger;

/**
 * @author $Author$
 * 
 * @version $Id$
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

		float ax = context.getSuperContext().getSensor().getAccelX();
		float ay = context.getSuperContext().getSensor().getAccelY();
		float az = context.getSuperContext().getSensor().getAccelZ();

		if (ay < 3.0 && (Math.abs(ax) > 5.0 || Math.abs(az) > 5.0)) {
			fallDownCount++;
			if (fallDownCount > 5) {
				log.info("Fall down state detected.");
				context.getScheduler().preempt(this);
			}
		} else {
			fallDownCount = 0;
		}
	}

	public void continueTask(StrategyContext context) {
		float ax = context.getSuperContext().getSensor().getAccelX();
		float ay = context.getSuperContext().getSensor().getAccelY();
		float az = context.getSuperContext().getSensor().getAccelZ();

		if (ay > 9.5) {
			// 重力が下にかかるようになったら抜ける
			context.makemotion(Motions.MOTION_STOP);
			context.makemotion_head(0.0f, 0.0f);
			return;
		}

		// 逆さになってる
		if (ay < -6.0) {
			context.makemotion(Motions.MOTION_YY_GETUP_BACK);
		} else if (ax > 5.0) {
			// 背中側が下
			context.makemotion(Motions.MOTION_YY_GETUP_BACK);
		} else if (ax < -5.0) {
			// 背中側が上
			context.makemotion(Motions.MOTION_GETUP);
		} else if (Math.abs(az) > 5.0) {
			// 横?
			context.makemotion(Motions.MOTION_GETUP);
		}

		// 起き上がり中は頭を動かさない
		context.makemotion_head(0.0f, 0.0f);
		context.getScheduler().setTTL(20);
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
