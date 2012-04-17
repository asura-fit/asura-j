package jp.ac.fit.asura.nao.strategy.schedulers;

import static jp.ac.fit.asura.nao.motion.Motions.NAOJI_WALKER;

import org.apache.log4j.Logger;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.motion.MotionParam.CircleTurnParam.Side;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask.Mode;


public class WalkConfigScheduler extends BasicSchedulerTask {
	private Logger log = Logger.getLogger(WalkConfigScheduler.class);
	private BallTrackingTask tracking;

	StrategyContext context;

	public static enum ConfigMode {
		Walker, CircleTurn
	}

	ConfigMode mode;

	Side side;

	long count;


	@Override
	public void init(RobotContext context) {
		tracking = (BallTrackingTask) context.getStrategy().getTaskManager().find("BallTracking");
		assert tracking != null;

		mode = ConfigMode.Walker;
		side = Side.Left;
	}


	@Override
	public void enter(StrategyContext context) {
	}



	@Override
	public void leave(StrategyContext context) {
		// TODO 自動生成されたメソッド・スタブ
		super.leave(context);
	}

	@Override
	public void continueTask(StrategyContext context) {
		this.context = context;

		if (mode == ConfigMode.Walker) {
			walkerMode();
		} else {

		}

	}

	@Override
	protected void fillQueue(StrategyContext context) {
		assert false;
	}

	@Override
	public String getName() {
		return "WalkConfigScheduler";
	}



	private void walkerMode() {
		WorldObject ball = context.getBall();
		int balld;
		float ballh;

		if (ball.getConfidence() == 0) {
			context.getScheduler().abort();
			return;
		}

		balld = ball.getDistance();
		ballh = ball.getHeading();

		if (context.getFrame() % 3 == 0)
			log.trace("bc:" + ball.getConfidence() + " bd:" + balld + " bh:"
					+ ballh);

		if (balld < 800) {
			tracking.setMode(Mode.Cont);
			if (Math.abs(ballh) > 20f) {
				if (ballh > 0) {
					if (context.hasMotion(Motions.NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
								.toRadians(0.4f * ballh));
					else
						context.makemotion(Motions.MOTION_LEFT_YY_TURN);
				} else {
					if (context.hasMotion(Motions.NAOJI_WALKER))
						if (context.hasMotion(NAOJI_WALKER))
							context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
									.toRadians(0.4f * ballh));
						else
							context.makemotion(Motions.MOTION_RIGHT_YY_TURN);
				}
			} else if (balld > 230) {
				if (context.hasMotion(Motions.NAOJI_WALKER))
					context
							.makemotion(NAOJI_WALKER, balld * 0.35f / 1e3f, 0,
									0);
				else
					context.makemotion(Motions.MOTION_YY_FORWARD_STEP);
			}

			return;

		} else {
			tracking.setMode(Mode.Localize);
			if (Math.abs(ballh) > 27) {
				if (ballh > 0) {
					if (context.hasMotion(Motions.NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
								.toRadians(0.4f * ballh));
					else
						context.makemotion(Motions.MOTION_LEFT_YY_TURN);
				} else {
					if (context.hasMotion(NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
								.toRadians(0.4f * ballh));
					else
						context.makemotion(Motions.MOTION_RIGHT_YY_TURN);
				}
			} else {
				if (context.hasMotion(Motions.NAOJI_WALKER))
					context.makemotion(NAOJI_WALKER, balld * 0.5f / 1e3f, 0, 0);
				else
					context.makemotion(Motions.MOTION_YY_FORWARD_STEP);
			}

			return;
		}
	}

	private void circleTurnMode() {
		if  (count < 200) {
			if (context.hasMotion(Motions.NAOJI_WALKER)) {
				context.makemotion(Motions.NAOJI_CIRCLETURN, side);
			}
		}

		count++;
	}

	public void setMode(ConfigMode mode) {
		this.mode = mode;

		log.info("set config mode: " + this.mode);

		if (this.mode == ConfigMode.CircleTurn) {
			count = 0;
		}
	}

	public void setMode(ConfigMode mode, Side side) {
		setMode(mode);
		this.side = side;

		log.info("set side: " + side);
	}

}
