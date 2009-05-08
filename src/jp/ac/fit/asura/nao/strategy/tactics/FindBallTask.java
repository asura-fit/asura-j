/*
 * 作成日: 2008/06/13
 */
package jp.ac.fit.asura.nao.strategy.tactics;

import static jp.ac.fit.asura.nao.motion.Motions.MOTION_LEFT_YY_TURN;
import static jp.ac.fit.asura.nao.motion.Motions.MOTION_RIGHT_YY_TURN;
import static jp.ac.fit.asura.nao.motion.Motions.MOTION_YY_FORWARD;
import static jp.ac.fit.asura.nao.motion.Motions.NAOJI_WALKER;
import static jp.ac.fit.asura.nao.motion.Motions.NULL;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask.Mode;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 *
 * @version $Id: FindBallTask.java 709 2008-11-23 07:40:31Z sey $
 *
 */
public class FindBallTask extends Task {
	private static final int MAX_PITCH = 45;
	private static final int MIN_PITCH = 0;

	private Logger log = Logger.getLogger(FindBallTask.class);

	private int step;

	private int destPitch;

	private int lastTurnSide = 0;

	private enum FindState {
		PRE, TURN, FINDBALL
	}

	private FindState state;

	private BallTrackingTask tracking;

	public String getName() {
		return "FindBallTask";
	}

	public void init(RobotContext context) {
		tracking = (BallTrackingTask) context.getStrategy().getTaskManager()
				.find("BallTracking");
		assert tracking != null;
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(500);
		step = 0;
		state = FindState.PRE;
	}

	public void continueTask(StrategyContext context) {
		if (context.getBall().getConfidence() > 0) {
			tracking.setMode(Mode.Cont);
			context.makemotion(NULL);
			context.getScheduler().abort();
			return;
		}

		tracking.setMode(Mode.Cont);
		if (step == 50) {
			state = FindState.TURN;
			log.debug("state = " + state);
		} else if (step == 150) {
			state = FindState.FINDBALL;
			log.debug("state = " + state);
		}
		switch (state) {
		case PRE:
			tracking.setMode(BallTrackingTask.Mode.Cont);
			context.makemotion(NULL);
			break;
		case TURN:
			if (lastTurnSide > 0) {
				if (context.hasMotion(NAOJI_WALKER))
					context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
							.toRadians(30));
				else
					context.makemotion(MOTION_LEFT_YY_TURN);
			} else {
				if (context.hasMotion(NAOJI_WALKER))
					context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
							.toRadians(-30));
				else
					context.makemotion(MOTION_RIGHT_YY_TURN);
			}
			break;
		case FINDBALL:
			// どうしても見つからないとき指定した場所に行く
			WorldObject self = context.getSelf();
			int selfX = self.getX();
			int selfY = self.getY();
			int tx = 0; // 目標の位置
			int ty = 0; //

			float deg = MathUtils.normalizeAngle180(MathUtils
					.toDegrees(MathUtils.atan2(tx - self.getX(), ty
							- self.getY()))
					- self.getYaw());
			float dist = (float) Math.sqrt((ty - self.getY())
					* (tx - self.getX()));
			if (Math.abs(selfX - tx) > 20 || Math.abs(selfY - ty) > 20) {
				// log.info(deg);
				if (deg < -20) {
					if (context.hasMotion(NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER, 0, 0,
								0.75f * MathUtils.toRadians(deg));
					else
						context.makemotion(MOTION_RIGHT_YY_TURN);
					lastTurnSide = -1;
				} else if (deg > 20) {
					if (context.hasMotion(NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER, 0, 0,
								0.75f * MathUtils.toRadians(deg));
					else
						context.makemotion(MOTION_LEFT_YY_TURN);
					lastTurnSide = 1;
				} else {
					if (context.hasMotion(NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER, dist * 0.25f / 1e3f,
								0, 0);
					else
						context.makemotion(MOTION_YY_FORWARD);
				}
			} else {
				step = 0;
			}
			break;
		default:
			assert false;
		}
		step++;
	}
}
