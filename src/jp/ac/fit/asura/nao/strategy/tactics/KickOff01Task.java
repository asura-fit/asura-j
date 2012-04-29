package jp.ac.fit.asura.nao.strategy.tactics;

import static jp.ac.fit.asura.nao.motion.Motions.NAOJI_WALKER;

import org.apache.log4j.Logger;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.event.MotionEventListener;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.motion.Motion;

import jp.ac.fit.asura.nao.naoji.motion.NaojiWalker;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.actions.FrontShotTask;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask.Mode;

//import jp.ac.fit.asura.nao.strategy.tactics.TurnTask.TurnState;

public class KickOff01Task extends Task implements MotionEventListener {
	private Logger log = Logger.getLogger(KickOff01Task.class);
	private BallTrackingTask tracking;

	private int preBalld;
	private float preBallh;
	private int count;
	private int step;

	private StrategyContext context;

	@Override
	public String getName() {
		return "KickOff01Task";
	}

	private boolean motionStarted;
	private boolean motionStopped;

	public void init(RobotContext context) {
		tracking = (BallTrackingTask) context.getStrategy().getTaskManager()
				.find("BallTracking");
		assert tracking != null;
		count = 0;

		context.getMotor().addEventListener(this);
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(600);// 時間をここで決める

		preBalld = 999;
	}

	public void continueTask(StrategyContext context) {
		log.info("count:" + count + " ttl:" + context.getScheduler().getTTL()+"step:"+step);
		this.context = context;
		WorldObject ball = context.getBall();
		int balld;
		float ballh;
		// if (context.getWalkFlag() == false){
		if (count < 200) {
			count++;

			if (ball.getConfidence() == 0 && !motionStarted) {
				context.getScheduler().abort();
				log.info("Ball Lost!");
				return;
			}

			if (ball.getConfidence() > 0 && ball.getDistance() != 0) {
				balld = ball.getDistance();
				preBalld = balld;
			} else {
				balld = preBalld;
			}

			ballh = ball.getHeading();

			if (context.getFrame() % 3 == 0)
				log.trace("bc:" + ball.getConfidence() + " bd:" + balld
						+ " bh:" + ballh);

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
								context.makemotion(NAOJI_WALKER, 0, 0,
										MathUtils.toRadians(0.4f * ballh));
							else
								context
										.makemotion(Motions.MOTION_RIGHT_YY_TURN);
					}
				} else if (balld > 200) {
					if (context.hasMotion(Motions.NAOJI_WALKER))
						context.makemotion(NAOJI_WALKER, balld * 0.3f / 1e3f,
								0, 0);
					else

						context.makemotion(Motions.MOTION_YY_FORWARD_STEP);
				} else {

					log.info("Getting ball finished.");
					// ここでキックを決める
					if (!motionStarted) {
						log.info("Kick!");

						context.makemotion(Motions.MOTION_SHOT_W_INSIDE_RIGHT);

						// context.setWalkFlag(true);
					}
					if (step<20&&!motionStopped) {
						context.getScheduler().setTTL(20);
						log.info("motion running");
						step++;
					} else {
						context.getSuperContext().getMotor()
								.removeEventListener(this);
						log.info("motion stopped");
						 context.setWalkFlag(true);
						//context.getScheduler().abort();
					}

				}

				return;

			} else {
				tracking.setMode(Mode.Cont);
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
						context.makemotion(NAOJI_WALKER, balld * 0.5f / 1e3f,
								0, 0);
					else
						context.makemotion(Motions.MOTION_YY_FORWARD_STEP);
				}
			}

		} else {
			context.setWalkFlag(true);
			log.info("timeout");
			return;
		}

	}

	@Override
	public void leave(StrategyContext context) {
		context.getBall().invalidate();
		count = 0;
		step = 0;

	}

	@Override
	public void startMotion(Motion motion) {
		if (motion.getId() == Motions.MOTION_SHOT_W_INSIDE_RIGHT) {
			motionStarted = true;
			context.setWalkFlag(false);
			log.info("motionStarted=false");
		}
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void stopMotion(Motion motion) {
		if (motion.getId() == Motions.MOTION_SHOT_W_INSIDE_RIGHT) {
			motionStopped = true;

			// context.getScheduler().abort();
			log.info("motionStopped=true");
		}
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void updateOdometry(float forward, float left, float turnCCW) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void updatePosture() {
		// TODO 自動生成されたメソッド・スタブ

	}

}
