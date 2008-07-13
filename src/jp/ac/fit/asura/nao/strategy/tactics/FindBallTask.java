/*
 * 作成日: 2008/06/13
 */
package jp.ac.fit.asura.nao.strategy.tactics;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask.Mode;

import org.apache.log4j.Logger;

/**
 * @author $Author$
 * 
 * @version $Id$
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
		PRE, BELOW, TURN, FINDBALL
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
		context.getScheduler().setTTL(400);
		step = 0;
		state = FindState.PRE;
	}

	public void continueTask(StrategyContext context) {
		if (context.getBall().getConfidence() > 0) {
			tracking.setMode(Mode.Cont);
			context.makemotion(Motions.MOTION_STOP);
			context.getScheduler().abort();
			return;
		}

		tracking.setMode(Mode.Cont);
		if (step == 50) {
			state = FindState.BELOW;
			log.debug("state = " + state);
		} else if (step == 100) {
			state = FindState.TURN;
			log.debug("state = " + state);
		} else if (step == 200) {
			state = FindState.FINDBALL;
			log.debug("state = " + state);
		}
		switch (state) {
		case PRE:
			tracking.setMode(BallTrackingTask.Mode.Cont);
			break;
		case BELOW:
			context.makemotion(Motions.MOTION_KAGAMI);
			break;
		case TURN:
			if (lastTurnSide > 0)
				context.makemotion(Motions.MOTION_LEFT_YY_TURN);
			else
				context.makemotion(Motions.MOTION_RIGHT_YY_TURN);

			float yaw = context.getSuperContext().getSensor().getJointDegree(
					Joint.HeadYaw);
			float pitch = context.getSuperContext().getSensor().getJointDegree(
					Joint.HeadPitch);
			// 100～200stepの間は頭を上下に振る

			if (Math.abs(pitch - destPitch) < 2) {
				destPitch = destPitch == MAX_PITCH ? MIN_PITCH : MAX_PITCH;
			}
			context.makemotion_head_rel(-(yaw + 100) / 64.0f,
					-(pitch - destPitch) / 16.0f);
			break;
		case FINDBALL:
			// どうしても見つからないとき指定した場所に行く
			WorldObject self = context.getSelf();
			int selfX = self.getX();
			int selfY = self.getY();
			int tx = 0; // 目標の位置
			int ty = 0; //

			double deg = MathUtils.normalizeAngle180((float) Math
					.toDegrees(Math.atan2(ty - self.getY(), tx - self.getX()))
					- self.getYaw());
			if (Math.abs(selfX - tx) > 20 || Math.abs(selfY - ty) > 20) {
				log.info(deg);
				if (deg < -20) {
					context.makemotion(Motions.MOTION_RIGHT_YY_TURN);
					lastTurnSide = -1;
				} else if (deg > 20) {
					context.makemotion(Motions.MOTION_LEFT_YY_TURN);
					lastTurnSide = 1;
				} else {
					context.makemotion(Motions.MOTION_YY_FORWARD);
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
