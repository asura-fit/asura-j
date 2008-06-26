/*
 * 作成日: 2008/06/27
 */
package jp.ac.fit.asura.nao.strategy.schedulers;

import jp.ac.fit.asura.nao.event.MotionEventListener;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.strategy.StrategyContext;

/**
 * なにかいろいろ実験用のスケジューラ.
 * 
 * オドメトリの計測とかはここでやるといいかも
 * 
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class ExperimentalScheduler extends BasicSchedulerTask implements MotionEventListener{
	public String getName() {
		return "ExperimentalScheduler";
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
	}

	protected void fillQueue(StrategyContext context) {
		assert false;
	}
	
	public void startMotion(Motion motion) {
	}
	
	public void stopMotion(Motion motion) {
	}

	public void updateOdometry(int forward, int left, float turnCCW) {
	}
	
	public void updatePosture() {
	}
}
