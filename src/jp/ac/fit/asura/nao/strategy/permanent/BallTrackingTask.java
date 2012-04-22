/*
 * 作成日: 2008/05/18
 */
package jp.ac.fit.asura.nao.strategy.permanent;

import static jp.ac.fit.asura.nao.misc.MathUtils.toRadians;

import javax.vecmath.Point2f;
import jp.ac.fit.asura.nao.Camera;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.Camera.CameraID;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.motion.MotionUtils;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.SomaticContext;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.vision.VisualObjects;
import jp.ac.fit.asura.nao.vision.perception.BallVisualObject;
import jp.ac.fit.asura.nao.vision.perception.VisualObject;
import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 *
 * @version $Id: BallTrackingTask.java 717 2008-12-31 18:16:20Z sey $
 *
 *
 * @changed $Author: matsumo $
 *
 * @version $Id: BallTrackingTask.java 717 2012-04-02 20:02:20Z matsumo $
 */
public class BallTrackingTask extends Task {
	private Logger log = Logger.getLogger(BallTrackingTask.class);
	private static int BALLCONF_THRESHOLD = 10;
	private static int GOALCONF_THRESHOLD = 10;

	public enum Mode {
		Cont, Localize, Disable, LookFront, TargetGoal, OwnGoal, Goal, Alt
	}

	private enum State {
		Tracking, PreFindBall, PreFindBallSwitched, PreFindBallBottomCamera, PreFindBallTopCamera, PreFindBallMiddleCamera, Recover, LookAround, TargetTracking, OwnTracking, GoalTracking, preFindGoal
	}

	private StrategyContext context;

	private Mode mode;

	private State state;

	private long lastTransition;
	private long stateTime;
	private long currentTime;

	private float lastBallYaw;
	private float lastBallPitch;
	private CameraID lastBallCamera;
	private long lastBallSeen;

	// 最後に見た方向. 1 == 左, -1 == 右.
	private int lastLookSide;

	// 最後に見た方向. 1 == 下, -1 == 上.
	private int lastLookUpSide;

	private int preFindBallCount;
	private int preFindGoalCount;

	private int alternateCount;

	public String getName() {
		return "BallTracking";
	}

	public boolean canExecute(StrategyContext context) {
		return false;
	}

	public void init(RobotContext context) {
		lastLookSide = 1;
		lastLookUpSide = 1;
		mode = Mode.LookFront;
		state = State.PreFindBall;
	}

	public void before(StrategyContext context) {
		// LookFrontとLocalizeは調整中.
		mode = Mode.LookFront;
		// mode = Mode.Cont;
	}

	public void after(StrategyContext context) {
		this.context = context;

		currentTime = context.getTime();

		stateTime = currentTime - lastTransition;

		switch (mode) {
		case TargetGoal:
		case OwnGoal:
		case Goal:
			break;
		default:
			VisualObject vo = context.getBall().getVision();
			if (vo.confidence > 0) {
				Point2f angle = vo.angle;

				// ボールをみたときのyaw/pitchを保存.
				lastBallYaw = context.getSensorContext()
						.getJoint(Joint.HeadYaw);
				lastBallYaw += -angle.getX();

				lastBallPitch = context.getSensorContext().getJoint(
						Joint.HeadPitch);
				lastBallPitch += -angle.getY();

				lastBallSeen = currentTime;
				lastBallCamera = context.getSuperContext().getCamera()
						.getSelectedId();
				log.trace("update last ball Head Yaw:" + lastBallYaw
						+ " Pitch:" + lastBallPitch);
			}
		}

		// 頭が動かされていたら実行しない
		if (context.isHeadSet())
			return;

		switch (mode) {
		case Disable: {
			break;
		}
		case LookFront:
		case Localize:
			// たまにローカライズするモード
			localizeMode();
			break;
		case Cont:
			Camera cam = context.getSuperContext().getCamera();

			BallVisualObject vo =(BallVisualObject) context.getBall().getVision();
			WorldObject ball = context.getBall();
			int balld;
			balld = ball.getDistance();

			if(vo.isBottomTouched()){
				if (cam.getSelectedId() == CameraID.TOP) {
					if(balld<500)
					log.trace("switch camera to BOTTOM");
					cam.selectCamera(CameraID.BOTTOM);
				}
			}
			continuousMode();
			break;
		case TargetGoal:
			targetGoalMode();
			break;
		case OwnGoal:
			ownGoalMode();
			break;
		case Goal:
			selectGoal();
			break;
		case Alt:
			alternateMode();
			break;
		}
	}

	private void localizeMode() {
		Camera cam = context.getSuperContext().getCamera();
		// たまにローカライズするモード.
		switch (state) {
		case LookAround:
			if (stateTime > 3000) {
				// 時間切れ
				log.debug("LookAround time out");
				lastLookSide *= -1;
				changeState(State.Recover);
				break;
			}

			cam.selectCamera(CameraID.TOP);
			float destYaw = toRadians(45) * -lastLookSide;
			float destPitch = toRadians(5) * -lastLookUpSide;
			if (!moveHead(destYaw, destPitch, 0.35f, 500)) {
				lastLookSide *= -1;
				lastLookUpSide *= -1;
				changeState(State.Recover);
			}
			break;
		case Recover:
			if (trackBall())
				changeState(State.Tracking);
			else {
				cam.selectCamera(lastBallCamera);
				if (!moveHead(lastBallYaw, lastBallPitch, 0.75f, 500)
						|| stateTime > 3000)
					changeState(State.PreFindBall);
			}
			break;
		case Tracking:
			if (trackBall()) {
				if (stateTime > 4000) {
					changeState(State.LookAround);
				}
			} else if (currentTime - lastBallSeen < 2000) {
				changeState(State.Recover);
			} else {
				preFindBall();
				changeState(State.PreFindBall);
			}
			break;
		case PreFindBall:
		case PreFindBallSwitched:
		case PreFindBallTopCamera:
		case PreFindBallMiddleCamera:
		case PreFindBallBottomCamera:
			preFindBall();
			if (trackBall())
				changeState(State.Tracking);
			break;
		default:
			assert false : state;
		}
	}

	private void continuousMode() {
		// 常時トラッキングモード
		if (trackBall()) {
			if (state != State.Tracking)
				changeState(State.Tracking);
		} else {
			switch (state) {
			case PreFindBall:
			case PreFindBallSwitched:
			case PreFindBallTopCamera:
			case PreFindBallMiddleCamera:
			case PreFindBallBottomCamera:
				break;
			default:
				changeState(State.PreFindBall);
			}
			// ボールがないなら頭をふって探す.
			preFindBall();
		}
	}

	private void targetGoalMode() {
		// ターゲットゴールをトラッキングするモード
		VisualObject vo = context.getTargetGoal().getVision();
		if (trackGoal(vo)) {
			if (state != State.TargetTracking)
				changeState(State.TargetTracking);
		} else {
			if (state != State.preFindGoal)
				changeState(State.preFindGoal);

			preFindGoal(vo);
		}
	}

	private void ownGoalMode() {
		// オウンゴールをトラッキングするモード
		VisualObject vo = context.getOwnGoal().getVision();
		if (trackGoal(vo)) {
			if (state != State.OwnTracking)
				changeState((State.OwnTracking));
		} else {
			if (state != State.preFindGoal)
				changeState(State.preFindGoal);

			preFindGoal(vo);
		}
	}

	private void selectGoal() {
		// confidenceの高い方のゴールをトラッキングするモード
		VisualObject target = context.getTargetGoal().getVision();
		VisualObject own = context.getOwnGoal().getVision();

		if (own.confidence > target.confidence) {
			ownGoalMode();
		} else {
			targetGoalMode();
		}
	}

	/**
	 * ボールとターゲットゴールを交互に見るモード. 雑なので改良した方がよさげ.
	 *
	 * @return
	 */
	private void alternateMode() {
		if (alternateCount < 70) {
			continuousMode();
		} else if (alternateCount < 120) {
			targetGoalMode();
		} else {
			alternateCount = 0;
		}
		alternateCount++;
	}

	/**
	 * ボールをトラッキングします.
	 *
	 * makemotion_headが発行されればtrueを返し，ボールが見つからない場合はfalseを返します.
	 *
	 * @return
	 */
	private boolean trackBall() {
		VisualObject vo = context.getBall().getVision();
		if (vo.confidence > BALLCONF_THRESHOLD) {
			Point2f angle = vo.angle;
			float dx = -angle.getX();
			float dy = -angle.getY();

			float kpTh = 0.25f;
			float kpGain = 0.25f;
			if (Math.abs(dx) > kpTh)
				dx -= Math.copySign(kpTh * kpGain, dx);
			else
				dx *= kpGain;

			if (Math.abs(dy) > kpTh)
				dy -= Math.copySign(kpTh * kpGain, dy);
			else
				dy *= kpGain;
			context.makemotion_head_rel(dx, dy, 200);
			return true;
		}
		return false;
	}

	/*
	 * goalをトラッキングする
	 */
	private boolean trackGoal(VisualObject goal) {

		if (goal.confidence > GOALCONF_THRESHOLD) {
			Point2f angle = goal.angle;
			float dx = -angle.getX();
			float dy = -angle.getY();

			float kpTh = 0.25f;
			float kpGain = 0.4f;
			if (Math.abs(dx) > kpTh)
				dx -= Math.copySign(kpTh * kpGain, dx);
			else
				dx *= kpGain;

			if (Math.abs(dy) > kpTh)
				dy -= Math.copySign(kpTh * kpGain, dy);
			else
				dy *= kpGain;
			context.makemotion_head_rel(dx, dy, 200);
			return true;
		}
		return false;
	}

	private void preFindBall() {
		VisualObject vo = context.getBall().getVision();
		if (vo.confidence > BALLCONF_THRESHOLD)
			return;
		Camera cam = context.getSuperContext().getCamera();
		switch (state) {
		case PreFindBall:

			WorldObject ball = context.getBall();
			int balld;
			balld = ball.getDistance();

			int ballcf = ball.getConfidence();

			if (ballcf < 500) {

				if (stateTime > 300) {
					if (cam.getSelectedId() == CameraID.TOP) {
						log.trace("switch camera to BOTTOM");
						cam.selectCamera(CameraID.BOTTOM);
					} else {
						log.trace("switch camera to TOP");
						cam.selectCamera(CameraID.TOP);
					}
					changeState(State.PreFindBallSwitched);
				}
			} else {
				if (stateTime > 500) {
					if (cam.getSelectedId() == CameraID.TOP) {
						log.trace("switch camera to BOTTOM");
						cam.selectCamera(CameraID.BOTTOM);
					} else {
						log.trace("switch camera to TOP");
						cam.selectCamera(CameraID.TOP);
					}
					changeState(State.PreFindBallSwitched);
				}
			}
			break;
		case PreFindBallSwitched:
			if (stateTime > 500) {
				if (cam.getSelectedId() == CameraID.TOP)
					changeState(State.PreFindBallTopCamera);
				else if (stateTime > 400) {
					changeState(State.PreFindBallBottomCamera);
				} else
					changeState(State.PreFindBallMiddleCamera);
			}
			break;
		case PreFindBallTopCamera: {
			// 最後に見た方向と逆に振る.
			float yaw = Math.copySign(toRadians(60), -lastLookSide);
			float pitch = Math.copySign((float) Math.cos(yaw) * toRadians(-20),
					-lastLookUpSide) + toRadians(10);
			// 0,5f→1.1f
			if (!moveHead(yaw, pitch, 1.1f, 800)) {
				lastLookSide *= -1;
				lastLookUpSide *= -1;
				preFindBallCount++;
			}
			if (preFindBallCount >= 1) {
				preFindBallCount = 0;
				changeState(State.PreFindBall);
			}
			break;
		}
		case PreFindBallMiddleCamera: {
			// 最後と見た方向と逆に降る
			if (cam.getSelectedId() == CameraID.TOP)
				changeState(State.PreFindBallTopCamera);
			else
				changeState(State.PreFindBallBottomCamera);
			float yaw = toRadians(45) * -lastLookSide;
			float pitch = toRadians(15);
			// 0.5f→1.1f
			if (!moveHead(yaw, pitch, 1.1f, 800)) {
				lastLookSide *= -1;
				preFindBallCount++;
			}

			if (preFindBallCount >= 1) {
				preFindBallCount = 0;
				changeState(State.PreFindBall);
			}
			break;
		}
		case PreFindBallBottomCamera: {
			// 最後に見た方向と逆に振る.
			float yaw = toRadians(45) * -lastLookSide;
			float pitch = toRadians(55);
			// 0.5f→1.1f
			if (!moveHead(yaw, pitch, 1.1f, 800)) {
				lastLookSide *= -1;
				preFindBallCount++;
			}

			if (preFindBallCount >= 1) {
				preFindBallCount = 0;
				changeState(State.PreFindBall);
			}
			break;
		}
		}
	}

	private void preFindGoal(VisualObject goal) {
		if (goal.confidence > GOALCONF_THRESHOLD)
			return;

		Camera cam = context.getSuperContext().getCamera();
		// ゴールは常にTOPカメラで探す
		if (cam.getSelectedId() == CameraID.BOTTOM)
			cam.selectCamera(CameraID.TOP);

		float yaw = Math.copySign(toRadians(60), -lastLookSide);
		float pitch = Math.copySign((float) Math.cos(yaw) * toRadians(-20),
				-lastLookUpSide) + toRadians(10);
		// 0.5f→1.1f
		if (!moveHead(yaw, pitch, 1.1f, 800)) {
			lastLookSide *= -1;
			lastLookUpSide *= -1;
			preFindGoalCount++;
		}

		if (preFindGoalCount >= 1)
			preFindGoalCount = 0;
		changeState(State.PreFindBall);
	}

	/**
	 * pitch,yawを目標にkpGainに比例した速度で頭を動かします.
	 *
	 * 目標値が遠ければmakemotion_headを発行しtrueを返します.
	 *
	 * 目標値に到達すればfalseを返します.
	 *
	 * @return
	 */
	private boolean moveHead(float yaw, float pitch, float kpGain, int duration) {
		assert kpGain != 0;
		SomaticContext sc = context.getSomaticContext();
		float ssYaw = sc.get(Frames.HeadYaw).getAngle();
		float ssPitch = sc.get(Frames.HeadPitch).getAngle();

		log.trace("moveHead called:" + yaw + ", " + pitch);

		float dp = pitch - ssPitch;
		float dy = yaw - ssYaw;
		if (Math.abs(dy) < 0.25f && Math.abs(dp) < 0.25f) {
			log.trace("moveHead reached target angle:" + dy + " and " + dp);
			return false;
		}

		if (!MotionUtils.canMove(sc.getRobot().get(Frames.HeadYaw), yaw, ssYaw)
				&& !MotionUtils.canMove(sc.getRobot().get(Frames.HeadPitch),
						pitch, ssPitch)) {
			log.trace("moveHead can't move joint:");
			return false;
		}

		context.makemotion_head_rel(dy * kpGain, dp * kpGain, duration);
		return true;
	}

	private void changeState(State newState) {
		if (state != newState) {
			afterState(state);
			// log.debug(stateTime);
			log.debug("change state from " + state + " to " + newState);
			lastTransition = context.getTime();
			state = newState;
			beforeState(newState);
		}
	}

	private void afterState(State state) {
	}

	private void beforeState(State state) {
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public String getModeName() {
		return this.mode.name();
	}

}