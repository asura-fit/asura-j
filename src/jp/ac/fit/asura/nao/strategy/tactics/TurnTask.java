package jp.ac.fit.asura.nao.strategy.tactics;

import org.apache.log4j.Logger;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.Camera.CameraID;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask.Mode;
import jp.ac.fit.asura.nao.vision.perception.VisualObject;

public class TurnTask extends Task {
	private static final Logger log = Logger.getLogger(TurnTask.class);
	private BallTrackingTask tracking;

	private enum TurnState {
		LookGoal, LookBall
	}

	TurnState state;

	private int step;

	// 左 : -1 右: 1
	private int turnDirection;

	private Motion lastMotion;

	public String getName() {
		return "TurnTask";
	}

	public void init(RobotContext context) {
		state = TurnState.LookGoal;
		tracking = (BallTrackingTask) context.getStrategy().getTaskManager()
				.find("BallTracking");
	}

	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(400);
	}

	public void continueTask(StrategyContext context) {

		WorldObject goal = context.getTargetGoal();
		WorldObject ball = context.getBall();
		float goalh = goal.getHeading();
		int balld = ball.getDistance();
		float ballh = ball.getHeading();

		switch (state) {
		case LookGoal:
			// 基本ゴールを見ながら回り込む
			if (step % 10 == 0)
				log.info("step: " + step);
			if (step < 80) {
				if (tracking.getModeName() != "TargetGoal")
					tracking.setMode(Mode.TargetGoal);

				if (goal.getConfidence() > 0) {
					if (Math.abs(goalh) > 15) {
						if (Math.abs(goalh) < 25) {
							if (goalh > 0)
								context.makemotion(Motions.MOTION_LEFT_YY_TURN);
							else
								context
										.makemotion(Motions.MOTION_RIGHT_YY_TURN);
						} else {
							if (goalh > 0) {
								context.makemotion(Motions.MOTION_CIRCLE_RIGHT);
							} else {
								context.makemotion(Motions.MOTION_CIRCLE_LEFT);
							}
						}
					} else {
						log.info("TurnEnd.");
					}
				} else {
					context.makemotion(Motions.MOTION_CIRCLE_LEFT);
				}

				step++;

				return;
			} else {
				if (goal.getConfidence() > 0) {
					if (Math.abs(goalh) > 25) {
						lastMotion = context.getSuperContext().getMotor()
								.getCurrentMotion();
						changeState(TurnState.LookBall);
					} else
						step = 0;
				} else {
					// LookBallに行く前に、左右どちらに回ってたか保存する
					lastMotion = context.getSuperContext().getMotor()
							.getCurrentMotion();
					changeState(TurnState.LookBall);
				}
			}
			break;

		case LookBall:
			// ボールを確認して調整する
			if (step % 10 == 0)
				log.info("step: " + step);
			if (step < 160) {
				if (tracking.getModeName() != "Cont") {
					context.getSuperContext().getCamera().selectCamera(
							CameraID.BOTTOM);
					tracking.setMode(Mode.Cont);
				}

				if (ball.getConfidence() > 0) {

					if (Math.abs(ballh) > 28) {
						if (ballh > 0) {
							context.makemotion(Motions.MOTION_LEFT_YY_TURN);
						} else {
							context.makemotion(Motions.MOTION_RIGHT_YY_TURN);
						}
					} else if (balld > 220) {
						context.makemotion(Motions.MOTION_YY_FORWARD_STEP);
					} else if (balld < 150) {
						context.makemotion(Motions.MOTION_W_BACKWARD);
					} else {
						if (step < 130) {
							context.makemotion(lastMotion);
							step++;
						} else {
							changeState(TurnState.LookGoal);
							step = 0;
						}
					}
				} else {
					context.makemotion(lastMotion);
					step++;
				}

				return;
			} else {
				changeState(TurnState.LookGoal);

				step = 0;
			}
			break;
		}
	}

	public void leave(StrategyContext context) {
	}

	private void changeState(TurnState newState) {
		log.debug("change TurnState from " + state + " to " + newState);
		state = newState;
	}
}