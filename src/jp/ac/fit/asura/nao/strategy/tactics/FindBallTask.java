/*
 * 作成日: 2008/06/13
 */
package jp.ac.fit.asura.nao.strategy.tactics;

import static jp.ac.fit.asura.nao.motion.Motions.MOTION_KAGAMI;
import static jp.ac.fit.asura.nao.motion.Motions.MOTION_LEFT_YY_TURN;
import static jp.ac.fit.asura.nao.motion.Motions.MOTION_RIGHT_YY_TURN;
import static jp.ac.fit.asura.nao.motion.Motions.MOTION_STOP;
import static jp.ac.fit.asura.nao.motion.Motions.MOTION_YY_FORWARD;
import static jp.ac.fit.asura.nao.motion.Motions.NAOJI_WALKER;
import jp.ac.fit.asura.nao.Joint;
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
		context.getScheduler().setTTL(1000);
		step = 0;
		state = FindState.PRE;
	}

	public void continueTask(StrategyContext context) {
		if (context.getBall().getConfidence() > 0) {
			tracking.setMode(Mode.Cont);
			context.makemotion(MOTION_STOP);
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
		} else if (step == 600) {
			state = FindState.FINDBALL;
			log.debug("state = " + state);
		}
		switch (state) {
		case PRE:
			tracking.setMode(BallTrackingTask.Mode.Cont);
			context.makemotion(MOTION_STOP);
			break;
		case BELOW:
			context.makemotion(MOTION_KAGAMI);
			break;
		case TURN:
			int destYaw;
			if (lastTurnSide > 0) {
				if (context.hasMotion(NAOJI_WALKER))
					context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
							.toRadians(30));
				else
					context.makemotion(MOTION_LEFT_YY_TURN);
				destYaw = 45;
			} else {
				if (context.hasMotion(NAOJI_WALKER))
					context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
							.toRadians(-30));
				else
					context.makemotion(MOTION_RIGHT_YY_TURN);
				destYaw = -45;
			}

			float yaw = context.getSuperContext().getSensor().getJointDegree(
					Joint.HeadYaw);
			float pitch = context.getSuperContext().getSensor().getJointDegree(
					Joint.HeadPitch);
			// 100～200stepの間は頭を上下に振る

			if (Math.abs(pitch - destPitch) < 3) {
				destPitch = destPitch == MAX_PITCH ? MIN_PITCH : MAX_PITCH;
			}
			context.makemotion_head_rel(-(yaw - destYaw) / 64.0f,
					-(pitch - destPitch) / 16.0f);
			break;
		case FINDBALL:
			// どうしても見つからないとき指定した場所に行く
			WorldObject self = context.getSelf();
			int selfX = self.getX();
			int selfY = self.getY();
			int tx = 0; // 目標の位置
			int ty = 0; //

			float deg = MathUtils.normalizeAngle180((float) Math.toDegrees(Math
					.atan2(ty - self.getY(), tx - self.getX()))
					- self.getYaw());
			float dist = (float) Math.sqrt((ty - self.getY())
					* (tx - self.getX()));
			if (Math.abs(selfX - tx) > 20 || Math.abs(selfY - ty) > 20) {
				// log.info(deg);
				if (deg < -20) {
					if (context.hasMotion(NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER, 0, 0, 0.75f * deg);
					else
						context.makemotion(MOTION_RIGHT_YY_TURN);
					lastTurnSide = -1;
				} else if (deg > 20) {
					if (context.hasMotion(NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER, 0, 0, 0.75f * deg);
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
