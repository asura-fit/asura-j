package jp.ac.fit.asura.nao.strategy.tactics;

import org.apache.log4j.Logger;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.Camera.CameraID;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.motion.MotionParam.CircleTurnParam.Side;
import static jp.ac.fit.asura.nao.motion.Motions.NAOJI_WALKER;
import static jp.ac.fit.asura.nao.motion.Motions.NAOJI_CIRCLETURN;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask.Mode;
import jp.ac.fit.asura.nao.vision.perception.GoalVisualObject;
import jp.ac.fit.asura.nao.vision.perception.VisualObject;

/**
 * TargetGoalの正面までターンするタスク.
 *
 * @author takata
 */

public class TurnTask extends Task {
	private static final Logger log = Logger.getLogger(TurnTask.class);
	private BallTrackingTask tracking;

	private StrategyContext context;

	private enum TurnState {
		LookGoal, LookBall
	}

	/** TargetGoal方向まで回転するときに,左右どちらに回るか. */
	private enum TurnSide {
		Right, Left
	}

	/**
	 * selectSide()でOwnGoalの見え方を示すState.
	 * <p>
	 * [NotFind] 一定時間以上OwnGoalが見えていない. [Lost] OwnGoalの情報を元に回転した結果,見えなくなった. [Get]
	 * 正しい方向に回っていれば見えないはずなのに見えてきた.
	 */
	private enum OwnGoalState {
		NotFind, Lost, Get, Keep
	}

	private TurnState state;
	private OwnGoalState ownstate;
	private OwnGoalState lastOwnstate;
	private TurnSide side;

	/** OwnGoalを認識したら暫くtrueになるflag. */
	private boolean flag;

	private boolean switchSideFlag;

	/** OwnGoalStateを最後に変更した時刻 */
	private long lastStateChangeTime;

	private Motion lastMotion;

	private int step;
	private int count;

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
		this.context = context;
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
//				if (count < 50) {
//					count++;
//					return;
//				}
				if (tracking.getModeName() != "TargetGoal")
					tracking.setMode(Mode.TargetGoal);

				if (goal.getConfidence() > 0) {
					if (Math.abs(goalh) > 15) {
						count = 0;
						if (Math.abs(goalh) < 25) {
							if (goalh > 0) {
								setTurnSide(TurnSide.Left);
								if (context.hasMotion(NAOJI_WALKER))
									context.makemotion(NAOJI_WALKER, 0, 0,
											MathUtils.toRadians(0.2f * goalh));
								else
									context
											.makemotion(Motions.MOTION_LEFT_YY_TURN);
							} else {
								setTurnSide(TurnSide.Right);
								if (context.hasMotion(NAOJI_WALKER))
									context
											.makemotion(
													NAOJI_WALKER,
													0,
													0,
													MathUtils
															.toRadians((0.2f * goalh)));
								else
									context
											.makemotion(Motions.MOTION_RIGHT_YY_TURN);
							}
						} else {
							if (goalh > 0) {
								setTurnSide(TurnSide.Right);
								if (context.hasMotion(NAOJI_WALKER))
									context.makemotion(NAOJI_CIRCLETURN, Side.Right);
								else
									context
											.makemotion(Motions.MOTION_CIRCLE_RIGHT);
							} else {
								setTurnSide(TurnSide.Left);
								if (context.hasMotion(NAOJI_WALKER))
									context.makemotion(NAOJI_CIRCLETURN, Side.Left);
								else
									context
											.makemotion(Motions.MOTION_CIRCLE_LEFT);
							}
						}
					} else {
							log.info("TurnEnd.");
							context.getScheduler().abort();
							context.pushQueue("FrontShotTask");
							return;
					}
				} else {
					selectSide();

					if (side == TurnSide.Left) {
						if (context.hasMotion(NAOJI_WALKER))
							context.makemotion(NAOJI_CIRCLETURN, Side.Left);
						else
							context.makemotion(Motions.MOTION_CIRCLE_LEFT);
					} else {
						if (context.hasMotion(NAOJI_WALKER))
							context.makemotion(NAOJI_CIRCLETURN, Side.Right);
						else
							context.makemotion(Motions.MOTION_CIRCLE_RIGHT);
					}
				}

				step++;

				return;
			} else {
				if (goal.getConfidence() > 0) {
					if (Math.abs(goalh) > 25) {
						lastMotion = context.getSuperContext().getMotor()
								.getCurrentMotion();
						log.info("save lastMotiion:" + lastMotion.toString());
						changeState(TurnState.LookBall);
					} else
						step = 0;
				} else {
					// LookBallに行く前に、左右どちらに回ってたか保存する
					lastMotion = context.getSuperContext().getMotor()
							.getCurrentMotion();
					log.info("save lastMotiion:" + lastMotion.toString());

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
						if (context.hasMotion(NAOJI_WALKER)) {
							context.makemotion(NAOJI_WALKER, 0, 0, MathUtils
									.toRadians(0.3f * ballh));
						} else {
							if (ballh > 0) {
								context.makemotion(Motions.MOTION_LEFT_YY_TURN);
							} else {
								context
										.makemotion(Motions.MOTION_RIGHT_YY_TURN);
							}
						}
					} else if (balld > 250) {
						if (context.hasMotion(NAOJI_WALKER))
							context.makemotion(NAOJI_WALKER,
									balld * 0.35f / 1e3f, 0, 0);
						else
							context.makemotion(Motions.MOTION_YY_FORWARD_STEP);
					} else if (balld < 160) {
						if (context.hasMotion(NAOJI_WALKER))
							context.makemotion(NAOJI_WALKER, -0.25f, 0, 0);
						else
							context.makemotion(Motions.MOTION_W_BACKWARD);
					} else {
						if (step < 130) {
							// TurnState: LookGoalのときに回っていた方向に回る
							log.info("make lastMotion:" + lastMotion);
							if (side == TurnSide.Left) {
								if (context.hasMotion(NAOJI_WALKER))
									context.makemotion(NAOJI_CIRCLETURN, Side.Left);
								else
									context
											.makemotion(Motions.MOTION_CIRCLE_LEFT);
							} else {
								if (context.hasMotion(NAOJI_WALKER))
									context.makemotion(NAOJI_CIRCLETURN, Side.Right);
								else
									context
											.makemotion(Motions.MOTION_CIRCLE_RIGHT);
							}

							step++;
						} else {
							changeState(TurnState.LookGoal);
							step = 0;
						}
					}
				} else {
					// TurnState: LookGoalのときに回っていた方向に回る
					log.info("make lastMotion:" + lastMotion);
					if (side == TurnSide.Left) {
						if (context.hasMotion(NAOJI_WALKER))
							context.makemotion(NAOJI_CIRCLETURN, Side.Left);
						else
							context.makemotion(Motions.MOTION_CIRCLE_LEFT);
					} else {
						if (context.hasMotion(NAOJI_WALKER))
							context.makemotion(NAOJI_CIRCLETURN, Side.Right);
						else
							context.makemotion(Motions.MOTION_CIRCLE_RIGHT);
					}

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

	@Override
	public void after(StrategyContext context) {
		// 最後にOwnGoalを認識してから30秒以上経過していたら,flagをfalseにする
		if (context.getOwnGoal().getDifftime() > 30000) {
			flag = false;
		}
	}

	public void leave(StrategyContext context) {
	}

	private void changeState(TurnState newState) {
		log.debug("change TurnState from " + state + " to " + newState);
		state = newState;
	}

	/**
	 * TurnSideを決定する.
	 *
	 * @return
	 */
	private void selectSide() {
		if (tracking.getModeName() != "Goal") {
			tracking.setMode(Mode.Goal);
		}
		WorldObject own = context.getOwnGoal();

		if (own.getConfidence() > 0) {
			flag = true;
		}

		// OwnGoalStateの設定
		if (!flag) {
			changeOwnGoalState(OwnGoalState.NotFind);
		} else {
			if (own.getConfidence() == 0) {
				changeOwnGoalState(OwnGoalState.Lost);
			} else {
				changeOwnGoalState(OwnGoalState.Get);
			}
		}

		if (context.getFrame() % 10 == 0) {
			log.info("OwnGoalState: " + ownstate);
		}

		// OwnGoalStateに応じた動作をする
		switch (ownstate) {
		case NotFind:
			notFindStateProcess();
			break;
		case Lost:
			lostStateProcess();
			break;
		case Get:
			getStateProcess();
			break;
		}
	}

	/**
	 * OwnGoalStateを変更する.
	 *
	 * @param newState
	 *            新しいOwnGoalState
	 */
	private void changeOwnGoalState(OwnGoalState newState) {
		// 諸々の都合上, lastStateはstate != newStateでも更新する.
		lastOwnstate = ownstate;
		if (ownstate != newState) {
			log.info("OwnGoalState changes from " + ownstate + " to "
					+ newState);
			this.ownstate = newState;
			lastStateChangeTime = context.getTime();
		}
	}

	/**
	 * TurnSideを現在とは反対に切り替える(Right->Left, Left->Right).
	 */
	private void switchSide() {
		if (side == TurnSide.Left) {
			side = TurnSide.Right;
		} else {
			side = TurnSide.Left;
		}
		flag = true;
		switchSideFlag = true;
	}

	/**
	 * TurnSideを引数sideでしたしたものに切り替える.
	 *
	 * @param side
	 *            指定したい回転方向
	 */
	private void setTurnSide(TurnSide side) {
		this.side = side;
	}

	/**
	 * OwnGoalState = notFind のときに実行される処理.
	 *
	 * @return
	 */
	private void notFindStateProcess() {
		float targeth = context.getTargetGoal().getHeading();

		if (targeth > 0) {
			setTurnSide(TurnSide.Left);
		} else {
			setTurnSide(TurnSide.Right);
		}
	}

	/**
	 * OwnGoalState = Lost のときに実行される処理.
	 *
	 * @return
	 */
	private void lostStateProcess() {
		// LostではTurnSideを変更する必要がない.
		// なので、今のところ処理なし.
		// 何か処理をさせたい場合はここに記述.
	}

	/**
	 * OwnGoalState = Get のときに実行される処理.
	 *
	 * @return
	 */
	private void getStateProcess() {
		float targeth = context.getTargetGoal().getHeading();
		int ownd = context.getOwnGoal().getDistance();
		GoalVisualObject own = (GoalVisualObject) context.getOwnGoal()
				.getVision();

		if (!switchSideFlag) {
			if (lastOwnstate == OwnGoalState.NotFind) {
				log.info("Get: from switchSide");
				switchSide();
			} else {
				if (ownd < 3000) {
					// OwnGoalが近い
					if ((own.isLeftTouched() && own.isRightTouched())
							|| (!own.isLeftTouched() && !own.isRightTouched())) {
						log.info("Get: [NEAR] from targeth");
						if (targeth > 0) {
							setTurnSide(TurnSide.Left);
						} else {
							setTurnSide(TurnSide.Right);
						}
					} else {
						if (own.isLeftTouched()) {
							log.info("Get: from Left Touched");
							setTurnSide(TurnSide.Right);
						} else {
							log.info("Get: from Right Touched");
							setTurnSide(TurnSide.Left);
						}
					}
				} else {
					// OwnGoalが遠い
					log.info("Get: [FAR] from targeth.");
					if (targeth > 0) {
						setTurnSide(TurnSide.Left);
					} else {
						setTurnSide(TurnSide.Right);
					}
				}
			}
		}
	}
}