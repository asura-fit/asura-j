/*
 * 作成日: 2008/06/13
 */
package jp.ac.fit.asura.nao.strategy.tactics;

import java.awt.geom.Point2D;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.vision.VisualObjects;
import jp.ac.fit.asura.nao.vision.objects.VisualObject;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class ApproachBallTask extends Task {
	public String getName() {
		return "ApproachBallTask";
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(300);
	}

	public void continueTask(StrategyContext context) {
		WorldObject ball = context.getBall();
		if (ball.getConfidence() == 0) {
			context.getScheduler().abort();
			return;
		}

		float ballh = ball.getHeading();
		if (ballh > 20) {
			context.makemotion(Motions.MOTION_LEFT_YY_TURN);
		} else if (ballh < -20) {
			context.makemotion(Motions.MOTION_RIGHT_YY_TURN1);
		} else {
			context.makemotion(Motions.MOTION_YY_FORWARD);
		}
	}
}
