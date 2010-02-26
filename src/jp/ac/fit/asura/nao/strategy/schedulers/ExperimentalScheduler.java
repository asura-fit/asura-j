/*
 * 作成日: 2008/06/27
 */
package jp.ac.fit.asura.nao.strategy.schedulers;

import org.apache.log4j.Logger;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.event.MotionEventListener;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask.Mode;

/**
 * なにかいろいろ実験用のスケジューラ.
 *
 * オドメトリの計測とかはここでやるといいかも
 *
 * @author $Author: sey $
 *
 * @version $Id: ExperimentalScheduler.java 709 2008-11-23 07:40:31Z sey $
 *
 */
public class ExperimentalScheduler extends BasicSchedulerTask implements
		MotionEventListener {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private BallTrackingTask tracking;
	
	public String getName() {
		return "ExperimentalScheduler";
	}
	
	public void init(RobotContext context) {
		tracking = (BallTrackingTask) context.getStrategy().getTaskManager().find("BallTracking");
		assert tracking != null;
	}

	public void enter(StrategyContext context) {
		super.enter(context);
		context.getSuperContext().getMotor().addEventListener(this);
	}

	public void leave(StrategyContext context) {
		super.leave(context);
		context.getSuperContext().getMotor().removeEventListener(this);
	}

	public void continueTask(StrategyContext context) {
		// なにかする
		// context.makemotion(Motions.NAOJI_WALKER, 0.5f, 0, 0);
		tracking.setMode(Mode.Cont);
	}

	protected void fillQueue(StrategyContext context) {
		assert false;
	}

	public void startMotion(Motion motion) {
	}

	public void stopMotion(Motion motion) {
	}

	public void updateOdometry(float forward, float left, float turnCCW) {
	}

	public void updatePosture() {
	}
}
