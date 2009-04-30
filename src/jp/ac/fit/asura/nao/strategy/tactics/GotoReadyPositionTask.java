/**
 *
 */
package jp.ac.fit.asura.nao.strategy.tactics;

import static jp.ac.fit.asura.nao.motion.Motions.MOTION_LEFT_YY_TURN;
import static jp.ac.fit.asura.nao.motion.Motions.MOTION_RIGHT_YY_TURN;
import static jp.ac.fit.asura.nao.motion.Motions.MOTION_STOP;
import static jp.ac.fit.asura.nao.motion.Motions.MOTION_YY_FORWARD;
import static jp.ac.fit.asura.nao.motion.Motions.NAOJI_WALKER;

import java.awt.Point;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.strategy.Role;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask.Mode;

import org.apache.log4j.Logger;

/**
 * @author kilo
 *
 */
public class GotoReadyPositionTask extends Task {

	private Logger log = Logger.getLogger(getClass());

	private BallTrackingTask tracking;

	private int step;

	public String getName() {
		return "GotoReadyPositionTask";
	}

	public void init(RobotContext context) {
		tracking = (BallTrackingTask) context.getStrategy().getTaskManager()
				.find("BallTracking");
		assert tracking != null;
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(400);
		super.enter(context);
	}

	public void continueTask(StrategyContext context) {

		WorldObject self = context.getSelf();
		tracking.setMode(Mode.Cont);
		Role currentRole = context.getRole();
		boolean isKickoff = (context.getGameState().getKickOffTeam() == context
				.getTeam().toInt()) ? true : false;

		int targetX = ReadyPosition.getPosotion(currentRole, isKickoff).x;
		int targetY = ReadyPosition.getPosotion(currentRole, isKickoff).y;

		float dist = (float) Math.sqrt((targetY - self.getY())
				* (targetX - self.getX()));

		float deg = MathUtils.normalizeAngle180(MathUtils.toDegrees(MathUtils
				.atan2(targetY - self.getY(), targetX - self.getX()))
				- self.getYaw());

		tracking.setMode(BallTrackingTask.Mode.Localize);

		log.trace("sx:" + self.getX() + " sy:" + self.getY() + " sh:"
				+ self.getYaw() + " tx:" + targetX + " ty:" + targetY);

		// 移動できてるか
		if (Math.abs(self.getX() - targetX) > 200
				|| Math.abs(self.getY() - targetY) > 200) {

			// 移動する
			if (deg < -20) {
				if (context.hasMotion(NAOJI_WALKER))
					context.makemotion(NAOJI_WALKER, 0, 0, 0.75f * deg);
				else
					context.makemotion(MOTION_RIGHT_YY_TURN);
			} else if (deg > 20) {
				if (context.hasMotion(NAOJI_WALKER))
					context.makemotion(NAOJI_WALKER, 0, 0, 0.75f * deg);
				else
					context.makemotion(MOTION_LEFT_YY_TURN);
			} else {
				if (context.hasMotion(NAOJI_WALKER))
					context.makemotion(NAOJI_WALKER, dist * 0.25f / 1e3f, 0, 0);
				else
					context.makemotion(MOTION_YY_FORWARD);
			}
		} else {
			// 大体目標位置
			// 方向が正面か
			float heading = self.getYaw();

			if (Math.abs(heading) > 20) {
				// 0に近づける
				if (heading > 0) {
					if (context.hasMotion(NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER, 0, 0, 0.75f * heading);
					else
						context.makemotion(MOTION_RIGHT_YY_TURN);
				} else {
					if (context.hasMotion(NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER, 0, 0, 0.75f * heading);
					else
						context.makemotion(MOTION_LEFT_YY_TURN);
				}
			} else {
				// 移動終わり
				context.makemotion(MOTION_STOP);
				context.getScheduler().abort();
				return;
			}
		}

		super.continueTask(context);
	}

	static class ReadyPosition {

		public static Point getPosotion(Role role, boolean isKickoff) {
			Point pos;

			switch (role) {
			case Goalie:
				if (isKickoff)
					pos = new Point(0, -2950); // 0,-2950mm
				else
					pos = new Point(0, -2950); // 0,-2950mm
				break;
			case Striker:
				if (isKickoff)
					pos = new Point(0, -625); // 0,-625mm
				else
					pos = new Point(300, -2000); // 300,-2000mm
				break;
			case Defender:
			case Libero:
				if (isKickoff)
					pos = new Point(1200, -2000); // 1200,-2000mm
				else
					pos = new Point(-1200, -2000); // -1200,-2000mm
				break;
			default:
				pos = new Point(0, 0);
			}

			return pos;
		}
	}
}
