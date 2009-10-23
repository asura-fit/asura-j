/*
 * 作成日: 2008/04/13
 */
package jp.ac.fit.asura.nao.motion;

import static jp.ac.fit.asura.nao.Joint.HeadPitch;
import static jp.ac.fit.asura.nao.Joint.HeadYaw;
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
	private boolean hasHeadSpeedCommand;

	private int headDuration;

	// target value
	private float tHeadYaw;
	private float tHeadPitch;
	private float tHeadYawSpeed;
	private float tHeadPitchSpeed;

	private Motion currentMotion;
	private MotionParam currentParam;

	private boolean hasNextMotion;
	private Motion nextMotion;
	private MotionParam nextMotionParam;

	private Object switchLock = new Object();

	private KinematicOdometer odometer;

	private MotionEventListener listenerProxy;

	private List<MotionEventListener> listeners;

	/**
	 *
	 */
	public MotorCortex() {
		listeners = new CopyOnWriteArrayList<MotionEventListener>();
		listenerProxy = new MotionEventListener() {
			@Override
			public void startMotion(Motion motion) {
				log.debug("start motion " + motion.getName());
				for (MotionEventListener l : listeners)
					l.startMotion(motion);
			}

			@Override
			public void stopMotion(Motion motion) {
				log.debug("stop motion " + motion.getName());
				for (MotionEventListener l : listeners)
					l.stopMotion(motion);
			}

			@Override
			public void updateOdometry(float forward, float left, float turn) {
				log.trace("update odometry id:" + currentMotion.getId()
						+ " forward:" + forward + " left:" + left + " turn:"
						+ turn);
				for (MotionEventListener l : listeners)
					l.updateOdometry(forward, left, turn);
			}

			@Override
			public void updatePosture() {
				for (MotionEventListener l : listeners)
					l.updatePosture();
			}
		};
		odometer = new KinematicOdometer();
		motions = new HashMap<Integer, Motion>();
		motions.put(Motions.NULL, null);
	}

	public void init(RobotContext context) {
		robotContext = context;
		effector = context.getEffector();
		currentMotion = null;
		nextMotion = null;
	}

	public void start() {
		currentMotion = null;
		nextMotion = null;
	}

	public void stop() {
	}

	@Override
	public void step(MotionFrameContext context) {
		SomaticContext sc = context.getSomaticContext();
		odometer.step(sc);

		Robot robot = sc.getRobot();

		if (currentMotion != null)
			currentMotion.setContext(context);

		// 次のモーションに切り替え
		if (hasNextMotion) {
			Motion next;
			MotionParam nextParam;
			synchronized (switchLock) {
				next = nextMotion;
				nextParam = nextMotionParam;
				hasNextMotion = false;
			}

			// 切替の必要がある
			if (next != currentMotion
					|| (currentParam == null && nextParam != null)
					|| (currentParam != null && !currentParam.equals(nextParam))) {
				// モーションが中断可能であれば中断して次のモーションへ
				// そうでないなら，中断をリクエストする
				if (currentMotion == null || currentMotion.canStop()) {
					if (next != null)
						next.setContext(context);
					switchMotion(next, nextParam);
				} else {
					log.trace("Request stop motion " + currentMotion);
					currentMotion.requestStop();
					hasNextMotion = true;
				}
			}
		}

		// モーションを実行
		if (currentMotion != null) {
			if (!currentMotion.hasNextStep()) {
				// モーションを終了.
				switchMotion(null, null);
			} else {
				log.trace("step motion" + currentMotion.getName());
				currentMotion.step();

				// モーションを継続
				if (currentMotion.hasOdometer())
					currentMotion.updateOdometry(listenerProxy);
				else
					odometer.updateOdometry(listenerProxy);
			}
		}

		// 頭の動作
		if (hasHeadCommand) {
			int pitchTime = this.headDuration;
			int yawTime = this.headDuration;
			if (hasHeadSpeedCommand) {
				// 速度指定
				float sHeadYaw = context.getSensorContext().getJoint(HeadYaw);
				float sHeadPitch = context.getSensorContext().getJoint(
						HeadPitch);
				float dHeadYaw = tHeadYaw - sHeadYaw;
				float dHeadPitch = tHeadPitch - sHeadPitch;
				yawTime = (int) (1000 * dHeadYaw / tHeadYawSpeed);
				pitchTime = (int) (1000 * dHeadPitch / tHeadPitchSpeed);

				hasHeadSpeedCommand = false;
			}

			// Effectorにコマンド発行
			effector.setJoint(HeadYaw, clipping(robot.get(Frames
					.valueOf(HeadYaw)), tHeadYaw), yawTime);
			effector.setJoint(HeadPitch, clipping(robot.get(Frames
					.valueOf(HeadPitch)), tHeadPitch), pitchTime);
			hasHeadCommand = false;
		}
	}

	private void switchMotion(Motion next, MotionParam nextParam) {
		// 動作中のモーションを中断する
		if (currentMotion != null) {
			currentMotion.stop();
			listenerProxy.stopMotion(currentMotion);
		}

		currentMotion = next;
		currentParam = nextParam;

		if (currentMotion == null)
			return;

		// モーションを開始
		currentMotion.start(currentParam);
		listenerProxy.startMotion(currentMotion);
	}

	public void makemotion(Motion motion, MotionParam param) {
		log.trace("makemotion " + (motion != null ? motion.getName() : "NULL"));
		synchronized (switchLock) {
			hasNextMotion = true;
			nextMotion = motion;
			nextMotionParam = param;
		}
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

	/**
	 * 指定した角度へ頭部を動かします. 移動時間はdurationミリ秒を目標に制御されます.
	 *
	 * note. スレッド間の同期に問題あり.
	 *
	 * @param headYaw
	 *            目標関節角度(HeadYaw) [rad]
	 * @param headPitch
	 *            目標関節角度(HeadPitch) [rad]
	 * @param duration
	 *            目標移動時間[ms]
	 */
	public void makemotion_head(float headYaw, float headPitch, int duration) {
		log.trace("makemotion_head:" + headYaw + ", " + headPitch + ", "
				+ duration);
		this.tHeadYaw = headYaw;
		this.tHeadPitch = headPitch;
		this.headDuration = duration;
		hasHeadCommand = true;
	}

	/**
	 * 指定した角度へ頭部を動かします. 移動速度はyawSpeedとpitchSpeedを目標に制御されます.
	 *
	 * note. スレッド間の同期に問題あり.
	 *
	 * @param headYaw
	 *            目標関節角度(HeadYaw) [rad]
	 * @param headPitch
	 *            目標関節角度(HeadPitch) [rad]
	 * @param yawSpeed
	 *            目標関節速度(HeadYaw) [rad/s]
	 * @param pitchSpeed
	 *            目標関節速度(HeadPitch) [rad/s]
	 */
	public void makemotion_head(float headYaw, float headPitch, float yawSpeed,
			float pitchSpeed) {
		this.tHeadYaw = headYaw;
		this.tHeadPitch = headPitch;
		this.tHeadYawSpeed = yawSpeed;
		this.tHeadPitchSpeed = pitchSpeed;
		hasHeadSpeedCommand = true;
		hasHeadCommand = true;
	}

	public Motion getCurrentMotion() {
		return currentMotion;
	}

	public Motion getMotion(int motionId) {
		return motions.get(motionId);
	}

	/**
	 * モーションを登録します.
	 *
	 * Caution: このメソッドは複数のスレッドからアクセスした場合に問題が起きる可能性があります.
	 *
	 * @param motion
	 */
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
}
