/*
 * 作成日: 2008/06/13
 */
package jp.ac.fit.asura.nao.strategy.tactics;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class FindBallTask extends Task {
	private static final int MAX_PITCH = 45;
	private static final int MIN_PITCH = 0;

	private int step;

	private int destPitch;

	private enum FindState {
		PRE, BELOW, FIND
	}

	private FindState state;

	public String getName() {
		return "FindBallTask";
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(400);
		step = 0;
		state = FindState.PRE;
	}

	public void continueTask(StrategyContext context) {
		if (context.getBall().getConfidence() > 0) {
			context.makemotion(Motions.MOTION_STOP);
			context.getScheduler().abort();
			return;
		}

		if(step == 30){
			state = FindState.BELOW;
		}else
		if (step == 110) {
			state = FindState.FIND;
			destPitch = MAX_PITCH;
		}

		switch (state) {
		case PRE:
			break;
		case BELOW:
			context.makemotion(Motions.MOTION_KAGAMI);
			break;
		case FIND:
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
		default:
			assert false;
		}
		step++;
	}
}
