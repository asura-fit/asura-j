/*
 * 作成日: 2008/06/13
 */
package jp.ac.fit.asura.nao.strategy.tactics;

import java.util.Map;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.localization.WorldObjects;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;

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

	private Logger log = Logger.getLogger(getClass());

	private int step;

	private int destPitch;

	private enum FindState {
		PRE, BELOW, TURN, REAR, FINDBALL
	}

	private FindState state;

	private BallTrackingTask tracking;

	private Map<WorldObjects, WorldObject> worldObjects;

	// private SelfLocalization self;

	public String getName() {
		return "FindBallTask";
	}

	public void init(RobotContext context) {
		tracking = (BallTrackingTask) context.getStrategy().getTaskManager()
				.find("BallTracking");
		assert tracking != null;
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(800);
		step = 0;
		state = FindState.PRE;
	}

	public void continueTask(StrategyContext context) {
		if (context.getBall().getConfidence() > 0) {
			context.makemotion(Motions.MOTION_STOP);
			context.getScheduler().abort();
			return;
		}

		if (step == 0) {
			state = FindState.BELOW;
		} else if (step == 80) {
			state = FindState.REAR;
			destPitch = MAX_PITCH;
		} else if (step == 150) {
			state = FindState.TURN;
		} else if (step == 151) {
			state = FindState.FINDBALL;
		}
		switch (state) {
		case PRE:
			tracking.setMode(BallTrackingTask.Mode.Cont);
			break;
		case BELOW:
			context.makemotion(Motions.MOTION_KAGAMI);
			break;
		case REAR:
			context.makemotion(Motions.MOTION_W_BACKWARD);
			break;
		case TURN:
			context.makemotion(Motions.MOTION_RIGHT_YY_TURN);

			float yaw = context.getSuperContext().getSensor().getJointDegree(
					Joint.HeadYaw);
			float pitch = context.getSuperContext().getSensor().getJointDegree(
					Joint.HeadPitch);
			// 100～200stepの間は頭を上下に振る

			if (Math.abs(pitch - destPitch) < 2) {
				destPitch = destPitch == MAX_PITCH ? MIN_PITCH : MAX_PITCH;
			}
			context.makemotion_head_rel(-(yaw + 100) / 30.0f,
					-(pitch - destPitch) / 15.0f);
			break;
		case FINDBALL:
			// どうしても見つからないとき指定した場所に行く
			WorldObject self = context.getSelf();
			int selfX = self.getX();
			int selfY = self.getY();
			int tx = 0; // 目標の位置
			int ty = 0; //
			double way = context.getSelf().getHeading();
			if (!(selfX >= tx + 20 && tx <= tx - 20)
					&& (selfY >= ty + 20 && 20 <= ty - 20)) {
				if (way < -20) {
					context.makemotion(Motions.MOTION_RIGHT_YY_TURN);
				} else if (way > 20) {
					context.makemotion(Motions.MOTION_LEFT_YY_TURN);
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
		if (step > 200) {
			step = 0;
		}
	}
}
