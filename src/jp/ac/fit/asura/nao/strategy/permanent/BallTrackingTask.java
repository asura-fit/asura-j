/*
 * 作成日: 2008/05/18
 */
package jp.ac.fit.asura.nao.strategy.permanent;

import static jp.ac.fit.asura.nao.misc.MathUtils.toDegrees;
import static jp.ac.fit.asura.nao.misc.MathUtils.toRadians;

import javax.vecmath.Point2f;

import jp.ac.fit.asura.nao.Camera;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.Camera.CameraID;
import jp.ac.fit.asura.nao.motion.MotionUtils;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.SomaticContext;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.vision.perception.VisualObject;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 *
 * @version $Id: BallTrackingTask.java 717 2008-12-31 18:16:20Z sey $
 *
 */
public class BallTrackingTask extends Task {
	private Logger log = Logger.getLogger(BallTrackingTask.class);
	private static int BALLCONF_THRESHOLD = 10;

	public enum Mode {
		Cont, Localize, Disable, LookFront
	}

	private enum State {
		Tracking, PreFindBall, PreFindBallBottomCamera, PreFindBallTopCamera, Recover, LookAround
	}

	private StrategyContext context;

	private Mode mode;

	private State state;

	private long lastTransition;

	private long time;

	private float lastBallYaw;
	private float lastBallPitch;
	private int lastBallSeen;

	private float destYaw;
	private float destPitch;

	private int lastLookSide;

	private int preFindBallCount;

	public String getName() {
		return "BallTracking";
	}

	public boolean canExecute(StrategyContext context) {
		return false;
	}

	public void init(RobotContext context) {
		destYaw = 0;
		destPitch = 0;
		lastLookSide = 1;
		mode = Mode.LookFront;
		state = State.PreFindBall;
	}

	public void before(StrategyContext context) {
		// LookFrontとLocalizeは調整中.
		// mode = Mode.LookFront;
		mode = Mode.Cont;
	}

	public void after(StrategyContext context) {
		this.context = context;

		time = context.getTime() - lastTransition;

		VisualObject vo = context.getBall().getVision();
		if (vo.confidence > 0) {
			Point2f angle = vo.angle;

			// ボールをみたときのyaw/pitchを保存.
			lastBallYaw = context.getSensorContext().getJointDegree(
					Joint.HeadYaw);
			lastBallYaw += Math.toDegrees(angle.getX());

			lastBallPitch = context.getSensorContext().getJointDegree(
					Joint.HeadPitch);
			lastBallPitch += Math.toDegrees(-angle.getY());

			lastBallSeen = 0;
			log.trace("update last ball Head Yaw:" + lastBallYaw + " Pitch:"
					+ lastBallPitch);
		} else {
			lastBallSeen++;
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
			// 常時トラッキングモード
			continuousMode();
		}
	}

	private void localizeMode() {
		// たまにローカライズするモード.
		switch (state) {
		case LookAround:
			if (time > 4000) {
				// 時間切れ
				destYaw = toRadians(0);
				destPitch = toRadians(40);
				changeState(State.Tracking);
				return;
			}

			if (!moveHead(destYaw, destPitch, 0.125f)) {
				// destに到達
				if (destYaw == 0 && mode == Mode.Localize) {
					// ローカライズモードなら，頭を上げた後に左右に振る
					destYaw = toRadians(30) * lastLookSide;
					destPitch = toRadians(25);
					lastLookSide *= -1;
				} else {
					destYaw = toRadians(0);
					destPitch = toRadians(40);
					changeState(State.Tracking);

					// ランダマイズっぽく
					// lastLookSide *= -1;
				}
			}
			break;
		case Recover:
			if (lastBallSeen == 0)
				changeState(State.Tracking);
			else if (!moveHead(lastBallYaw, lastBallPitch, 0.125f)
					|| time > 5000)
				changeState(State.PreFindBall);
			break;
		case Tracking:
			if (trackBall()) {
				if (time > 5000)
					changeState(State.LookAround);
			} else if (lastBallSeen < 150) {
				changeState(State.Recover);
			} else {
				preFindBall();
				changeState(State.PreFindBall);
			}
			break;
		case PreFindBall:
		case PreFindBallTopCamera:
		case PreFindBallBottomCamera:
			preFindBall();
			if (trackBall())
				changeState(State.Tracking);
			break;
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
			case PreFindBallTopCamera:
			case PreFindBallBottomCamera:
				break;
			default:
				changeState(State.PreFindBall);
			}
			// ボールがないなら頭をふって探す.
			preFindBall();
		}
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
			context.makemotion_head_rel((float) (-0.25f * toDegrees(angle
					.getX())), (float) (-0.25f * toDegrees(angle.getY())));
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
			if (time > 250) {
				if (cam.getSelectedId() == CameraID.TOP) {
					log.trace("switch camera to BOTTOM");
					cam.selectCamera(CameraID.BOTTOM);
				} else {
					log.trace("switch camera to TOP");
					cam.selectCamera(CameraID.TOP);
				}
			}
			if (time > 500) {
				if (cam.getSelectedId() == CameraID.TOP)
					changeState(State.PreFindBallTopCamera);
				else
					changeState(State.PreFindBallBottomCamera);
			}
			break;
		case PreFindBallTopCamera: {
			// 左にふる.
			float yaw = toRadians(50);
			if (preFindBallCount % 2 == 0) {
				// 右にふる.
				yaw = toRadians(-50);
			}
			float pitch = (float) Math.cos(yaw) * toRadians(20) + toRadians(5);
			if (!moveHead(yaw, pitch, 0.125f))
				preFindBallCount++;

			if (time > 4000) {
				changeState(State.PreFindBall);
			}
			break;
		}
		case PreFindBallBottomCamera: {
			// 左にふる.
			float yaw = toRadians(50);
			if (preFindBallCount % 2 == 0) {
				// 右にふる.
				yaw = toRadians(-50);
			}
			float pitch = toRadians(10);
			if (!moveHead(yaw, pitch, 0.125f))
				preFindBallCount++;

			if (time > 4000) {
				changeState(State.PreFindBall);
			}
			break;
		}
		}
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
	private boolean moveHead(float yaw, float pitch, float kpGain) {
		assert kpGain != 0;
		SomaticContext sc = context.getSomaticContext();
		float ssYaw = sc.get(Frames.HeadYaw).getAngle();
		float ssPitch = sc.get(Frames.HeadPitch).getAngle();

		if (Math.abs(pitch - ssPitch) < 0.0625
				&& Math.abs(yaw - ssYaw) < 0.0625) {
			return false;
		}

		if (!MotionUtils.canMove(sc.getRobot().get(Frames.HeadYaw), yaw, ssYaw)
				&& !MotionUtils.canMove(sc.getRobot().get(Frames.HeadPitch),
						pitch, ssYaw))
			return false;

		context.makemotion_head_rel(toDegrees(yaw - ssYaw) * kpGain,
				toDegrees(pitch - ssPitch) * kpGain);
		return true;
	}

	private void changeState(State newState) {
		if (state != newState) {
			log.debug("change state from " + state + " to " + newState);
			lastTransition = context.getTime();
			state = newState;
		}
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}
}
