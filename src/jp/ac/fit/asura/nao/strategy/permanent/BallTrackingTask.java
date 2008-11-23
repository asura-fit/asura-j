/*
 * 作成日: 2008/05/18
 */
package jp.ac.fit.asura.nao.strategy.permanent;

import javax.vecmath.Point2d;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.motion.MotionUtils;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.vision.perception.VisualObject;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 * 
 * @version $Id$
 * 
 */
public class BallTrackingTask extends Task {
	private Logger log = Logger.getLogger(BallTrackingTask.class);

	public enum Mode {
		Cont, Localize, Disable, LookFront
	}

	private enum State {
		Tracking, PreFindBall, Recover, LookAround
	}

	private StrategyContext context;

	private Mode mode;

	private State state;

	private int step;

	private int count;

	private float lastBallYaw;
	private float lastBallPitch;
	private int lastBallSeen;

	private float destYaw;
	private float destPitch;

	private int lastLookSide;

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
		mode = Mode.LookFront;
	}

	public void after(StrategyContext context) {
		this.context = context;

		VisualObject vo = context.getBall().getVision();
		if (vo.confidence > 0) {
			Point2d angle = vo.angle;

			// ボールをみたときのyaw/pitchを保存.
			lastBallYaw = context.getSuperContext().getSensor().getJointDegree(
					Joint.HeadYaw);
			lastBallYaw += Math.toDegrees(-angle.getX());

			lastBallPitch = context.getSuperContext().getSensor()
					.getJointDegree(Joint.HeadPitch);
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
		case Localize: {
			// たまにローカライズするモード
			localizeMode();
			break;
		}
		case Cont: {
			// 常時トラッキングモード
			if (trackBall()) {
				changeState(State.Tracking);
			} else {
				changeState(State.PreFindBall);
				// ボールがないなら頭をふって探す.
				preFindBall();
			}
			break;
		}
		}
		step++;
		count++;
	}

	private void localizeMode() {
		// ローカライズモード.
		switch (state) {
		case LookAround:
			if (count > 30) {
				// 時間切れ
				destYaw = 0;
				destPitch = -10;
				changeState(State.Tracking);
				return;
			}

			if (!moveHead(destYaw, destPitch, 0.125f)) {
				// destに到達
				if (destYaw == 0 && mode == Mode.Localize) {
					// ローカライズモードなら，頭を上げた後に左右に振る
					destYaw = 60 * lastLookSide;
					destPitch = 0;
					lastLookSide *= -1;
				} else {
					destYaw = 0;
					destPitch = -10;
					changeState(State.Tracking);

					// ランダマイズっぽく
					lastLookSide *= -1;
				}
			}
			break;
		case Recover:
			if (lastBallSeen == 0)
				changeState(State.Tracking);
			else if (!moveHead(lastBallYaw, lastBallPitch, 0.125f)
					|| count > 50)
				changeState(State.PreFindBall);
			break;
		case Tracking:
			if (trackBall()) {
				if (count > 50)
					changeState(State.LookAround);
			} else if (lastBallSeen < 100) {
				changeState(State.Recover);
			} else {
				preFindBall();
				changeState(State.PreFindBall);
			}
			break;
		case PreFindBall:
			preFindBall();
			if (trackBall())
				changeState(State.Tracking);
			break;
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
		if (vo.confidence > 10) {
			Point2d angle = vo.angle;
			context.makemotion_head_rel((float) (-0.4 * Math.toDegrees(angle
					.getX())), (float) (-0.4 * Math.toDegrees(angle.getY())));
			return true;
		}
		return false;
	}

	private void preFindBall() {
		// 8の字
		float yaw = (float) (Math.sin(step * 0.15) * 60.0);
		float pitch = (float) (Math.cos(step * 0.15) * 20.0 + 30.0);
		moveHead(yaw, pitch, 0.25f);
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
		float ssYaw = context.getSuperContext().getSensor().getJointDegree(
				Joint.HeadYaw);
		float ssPitch = context.getSuperContext().getSensor().getJointDegree(
				Joint.HeadPitch);

		if (Math.abs(pitch - ssPitch) < 4 && Math.abs(yaw - ssYaw) < 4) {
			return false;
		}

		if (!MotionUtils.canMoveDeg(Joint.HeadYaw, yaw, ssYaw)
				&& !MotionUtils.canMoveDeg(Joint.HeadPitch, pitch, ssYaw))
			return false;

		context.makemotion_head_rel((yaw - ssYaw) * kpGain, (pitch - ssPitch)
				* kpGain);
		return true;
	}

	private void changeState(State newState) {
		if (state != newState) {
			log.debug("change state from " + state + " to " + newState);
			state = newState;
			count = 0;
		}
	}

	public void setMode(Mode mode) {
		this.mode = mode;
	}
}
