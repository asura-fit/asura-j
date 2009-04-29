/*
 * 作成日: 2008/04/13
 */
package jp.ac.fit.asura.nao.motion;

import static jp.ac.fit.asura.nao.Joint.HeadPitch;
import static jp.ac.fit.asura.nao.Joint.HeadYaw;
import static jp.ac.fit.asura.nao.Joint.LAnklePitch;
import static jp.ac.fit.asura.nao.Joint.LAnkleRoll;
import static jp.ac.fit.asura.nao.Joint.LElbowRoll;
import static jp.ac.fit.asura.nao.Joint.LElbowYaw;
import static jp.ac.fit.asura.nao.Joint.LHipPitch;
import static jp.ac.fit.asura.nao.Joint.LHipRoll;
import static jp.ac.fit.asura.nao.Joint.LHipYawPitch;
import static jp.ac.fit.asura.nao.Joint.LKneePitch;
import static jp.ac.fit.asura.nao.Joint.LShoulderPitch;
import static jp.ac.fit.asura.nao.Joint.LShoulderRoll;
import static jp.ac.fit.asura.nao.Joint.RAnklePitch;
import static jp.ac.fit.asura.nao.Joint.RAnkleRoll;
import static jp.ac.fit.asura.nao.Joint.RElbowRoll;
import static jp.ac.fit.asura.nao.Joint.RElbowYaw;
import static jp.ac.fit.asura.nao.Joint.RHipPitch;
import static jp.ac.fit.asura.nao.Joint.RHipRoll;
import static jp.ac.fit.asura.nao.Joint.RHipYawPitch;
import static jp.ac.fit.asura.nao.Joint.RKneePitch;
import static jp.ac.fit.asura.nao.Joint.RShoulderPitch;
import static jp.ac.fit.asura.nao.Joint.RShoulderRoll;
import static jp.ac.fit.asura.nao.motion.MotionUtils.clipping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.MotionCycle;
import jp.ac.fit.asura.nao.MotionFrameContext;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.event.MotionEventListener;
import jp.ac.fit.asura.nao.motion.MotionParam.WalkParam;
import jp.ac.fit.asura.nao.motion.motions.TestWalkMotion;
import jp.ac.fit.asura.nao.physical.Robot;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.SomaticContext;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 *
 * @version $Id: MotorCortex.java 717 2008-12-31 18:16:20Z sey $
 *
 */
public class MotorCortex implements MotionCycle {
	private Logger log = Logger.getLogger(MotorCortex.class);

	private Map<Integer, Motion> motions;
	private RobotContext robotContext;
	private Effector effector;
	private boolean hasHeadCommand;
	private int headDuration;
	private float headYaw;
	private float headPitch;

	private Motion currentMotion;
	private MotionParam currentParam;
	private Motion nextMotion;
	private MotionParam nextParam;

	private List<MotionEventListener> listeners;

	/**
	 *
	 */
	public MotorCortex() {
		listeners = new CopyOnWriteArrayList<MotionEventListener>();
		motions = new HashMap<Integer, Motion>();
	}

	public void init(RobotContext context) {
		robotContext = context;
		effector = context.getEffector();
		currentMotion = null;
		nextMotion = null;

		registMotion(new TestWalkMotion(TestWalkMotion.TESTWALK_MOTION));
	}

	public void start() {
		currentMotion = null;
		nextMotion = null;
		// set default position
		setDefaultPosition();
	}

	public void stop() {
	}

	private void setDefaultPosition() {
		effector.setJointDegree(LShoulderPitch, 110);
		effector.setJointDegree(LShoulderRoll, 20);
		effector.setJointDegree(LElbowYaw, -80);
		effector.setJointDegree(LElbowRoll, -90);
		effector.setJointDegree(LHipYawPitch, 0);
		effector.setJointDegree(LHipPitch, -25);
		effector.setJointDegree(LHipRoll, 0);
		effector.setJointDegree(LKneePitch, 40);
		effector.setJointDegree(LAnklePitch, -20);
		effector.setJointDegree(LAnkleRoll, 0);
		effector.setJointDegree(RHipYawPitch, 0);
		effector.setJointDegree(RHipPitch, -25);
		effector.setJointDegree(RHipRoll, 0);
		effector.setJointDegree(RKneePitch, 40);
		effector.setJointDegree(RAnklePitch, -20);
		effector.setJointDegree(RAnkleRoll, 0);
		effector.setJointDegree(RShoulderPitch, 110);
		effector.setJointDegree(RShoulderRoll, -20);
		effector.setJointDegree(RElbowYaw, 80);
		effector.setJointDegree(RElbowRoll, 90);
	}

	@Override
	public void step(MotionFrameContext context) {
		SomaticContext sc = context.getSomaticContext();
		Robot robot = sc.getRobot();

		if (currentMotion != null)
			currentMotion.setContext(context);

		if (nextMotion != currentMotion
				|| (currentParam == null && nextParam != null)
				|| (currentParam != null && !currentParam.equals(nextParam))) {
			// モーションが中断可能であれば中断して次のモーションへ
			// そうでないなら，中断をリクエストする
			if (currentMotion == null || currentMotion.canStop()) {
				if (nextMotion != null)
					nextMotion.setContext(context);
				switchMotion(nextMotion, nextParam);
			} else {
				currentMotion.requestStop();
			}
		}

		if (currentMotion == null) {
			setDefaultPosition();
		} else {
			if (!currentMotion.hasNextStep()) {
				// 次のモーションを連続実行
				switchMotion(currentMotion, currentParam);
			}
			log.trace("step motion" + currentMotion.getName());
			// モーションを継続
			currentMotion.step();

			updateOdometry();
		}

		if (hasHeadCommand) {
			effector.setJoint(HeadYaw, clipping(robot.get(Frames
					.valueOf(HeadYaw)), headYaw), headDuration);
			effector.setJoint(HeadPitch, clipping(robot.get(Frames
					.valueOf(HeadPitch)), headPitch), headDuration);
			hasHeadCommand = false;
		}
	}

	private void switchMotion(Motion next, MotionParam nextParam) {
		// 動作中のモーションを中断する
		if (currentMotion != null) {
			currentMotion.stop();
			fireStopMotion(currentMotion);
		}

		currentMotion = next;
		currentParam = nextParam;

		if (currentMotion == null)
			return;

		// モーションを開始
		currentMotion.start(currentParam);
		fireStartMotion(currentMotion);
	}

	public void makemotion(Motion motion, MotionParam param) {
		assert motion != null;
		log.trace("makemotion " + (motion != null ? motion.getName() : "NULL"));
		nextMotion = motion;
		nextParam = param;
	}

	public void makemotion(int motion) {
		assert motions.containsKey(motion) : motion;
		makemotion(motions.get(motion), null);
	}

	/**
	 *
	 * @param motion
	 * @param param1
	 */
	public void makemotion(int motion, MotionParam param) {
		assert motions.containsKey(motion);
		makemotion(motions.get(motion), param);
	}

	public void makemotion_head(float headYaw, float headPitch, int duration) {
		log.trace("makemotion_head:" + headYaw + ", " + headPitch + ", "
				+ duration);
		this.headYaw = headYaw;
		this.headPitch = headPitch;
		this.headDuration = duration;
		hasHeadCommand = true;
	}

	/**
	 * モーションの動作情報に基づいてオドメトリを更新する.
	 *
	 * いまのところやる気なし実装.
	 *
	 */
	private void updateOdometry() {
		// quick hack
		int df = 0, dl = 0;
		float dh = 0;
		switch (currentMotion.getId()) {
		// このへん全部妄想値．だれか計測してちょ．
		// 精度とキャストに注意
		case Motions.MOTION_LEFT_YY_TURN:
			assert currentMotion.totalFrames != 0;
			dh = 21.0f / currentMotion.totalFrames;
			break;
		case Motions.MOTION_RIGHT_YY_TURN:
			assert currentMotion.totalFrames != 0;
			dh = -21.0f / currentMotion.totalFrames;
			break;
		case Motions.MOTION_W_FORWARD:
			df = (int) (4.0f + Math.random());
			break;
		case Motions.MOTION_YY_FORWARD1:
		case Motions.MOTION_YY_FORWARD2:
		case Motions.MOTION_YY_FORWARD_STEP:
			assert currentMotion.totalFrames != 0;
			// xとyは1フレームあたり1.0mm以下の変位はそのまま伝達できないので，
			// ディザリング処理をしてごまかす
			df = (int) (350.0f / currentMotion.totalFrames + Math.random());
			break;
		case Motions.MOTION_YY_FORWARD:
			df = (int) (4.5f + Math.random());
			break;
		case Motions.MOTION_W_BACKWARD:
			df = (int) (-2.0f + Math.random());
			break;
		case Motions.MOTION_CIRCLE_RIGHT:
			assert currentMotion.totalFrames != 0;
			dl = (int) (-75.0f / currentMotion.totalFrames + Math.random());
			dh = 8f / currentMotion.totalFrames;
			break;
		case Motions.MOTION_CIRCLE_LEFT:
			assert currentMotion.totalFrames != 0;
			dl = (int) (75.0f / currentMotion.totalFrames + Math.random());
			dh = -8f / currentMotion.totalFrames;
			break;
		case Motions.MOTION_W_RIGHT_SIDESTEP:
			dl = (int) (-2.0f + Math.random());
			break;
		case Motions.MOTION_W_LEFT_SIDESTEP:
			dl = (int) (2.0f + Math.random());
			break;
		case Motions.NAOJI_WALKER:
			if (currentParam == null)
				break;
			WalkParam walkp = (WalkParam) currentParam;
			if (walkp.getForward() > 0.125f) {
				df = (int) (4.0f + Math.random());
			} else if (walkp.getForward() < -0.125f) {
				df = (int) (-4.0f + Math.random());
			}
			if (walkp.getLeft() > 0.125f) {
				dl = (int) (3.0f + Math.random());
			} else if (walkp.getLeft() < -0.125f) {
				dl = (int) (-3.0f + Math.random());
			}
			if (walkp.getTurn() > 0) {
				dh = 0.5f;
			} else if (walkp.getTurn() < 0) {
				dh = -0.5f;
			}

			break;
		default:
		}
		fireUpdateOdometry(df, dl, dh);
	}

	public Motion getCurrentMotion() {
		return currentMotion;
	}

	public Motion getMotion(int motionId) {
		return motions.get(motionId);
	}

	public void registMotion(Motion motion) {
		motions.put(motion.getId(), motion);
		motion.init(robotContext);
	}

	public boolean hasMotion(int motionId) {
		return motions.containsKey(motionId);
	}

	public void addEventListener(MotionEventListener listener) {
		listeners.add(listener);
	}

	public void removeEventListener(MotionEventListener listener) {
		listeners.remove(listener);
	}

	private void fireUpdateOdometry(int forward, int left, float turn) {
		if (log.isTraceEnabled())
			log
					.trace("update odometry id:" + currentMotion.getId()
							+ " forward:" + forward + " left:" + left
							+ " turn:" + turn);
		for (MotionEventListener l : listeners)
			l.updateOdometry(forward, left, turn);
	}

	private void fireStopMotion(Motion motion) {
		log.debug("MC: stop motion " + motion.getName());
		for (MotionEventListener l : listeners)
			l.stopMotion(motion);
	}

	private void fireStartMotion(Motion motion) {
		log.debug("MC: start motion " + motion.getName());
		for (MotionEventListener l : listeners)
			l.startMotion(motion);
	}
}
