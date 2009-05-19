/*
 * 作成日: 2008/06/13
 */
package jp.ac.fit.asura.nao.strategy.actions;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask.Mode;

/**
 * @author $Author: sey $
 *
 * @version $Id: InitialTask.java 709 2008-11-23 07:40:31Z sey $
 *
 */
public class InitialTask extends Task {
	private BallTrackingTask tracking;

	public String getName() {
		return "InitialTask";
	}

	@Override
	public void init(RobotContext context) {
		tracking = (BallTrackingTask) context.getStrategy().getTaskManager()
				.find("BallTracking");
		assert tracking != null;
	}

	@Override
	public void enter(StrategyContext context) {
		continueTask(context);
	}

	@Override
	public void continueTask(StrategyContext context) {
		context.getScheduler().setTTL(10);
		// Initialでは立ち上がってはいけない.
		switch (context.getGameState()) {
		case SET:
		case READY:
			break;
		default:
			// context.makemotion(Motions.MOTION_STOP);
			tracking.setMode(Mode.Disable);
		}
	}
}
