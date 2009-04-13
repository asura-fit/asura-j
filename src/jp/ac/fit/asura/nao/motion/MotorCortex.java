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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.event.MotionEventListener;
import jp.ac.fit.asura.nao.motion.parameterized.ParameterizedAction;
import jp.ac.fit.asura.nao.motion.parameterized.ShootAction;
import jp.ac.fit.asura.nao.physical.Robot;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.SomaticContext;
import jp.ac.fit.asura.nao.sensation.SomatoSensoryCortex;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 *
 * @version $Id: MotorCortex.java 717 2008-12-31 18:16:20Z sey $
 *
 */
public class MotorCortex implements RobotLifecycle {
	private Logger log = Logger.getLogger(MotorCortex.class);

	private Map<Integer, Motion> motions;
	private Map<Integer, ParameterizedAction> actions;
	private RobotContext robotContext;
	private Effector effector;
	private Sensor sensor;
	private SomatoSensoryCortex sensoryCortex;
	private float headYaw;
	private float headPitch;

	private float[] sensorJoints;

	private Motion currentMotion;
	private MotionParam currentParam;
	private Motion nextMotion;
	private MotionParam nextParam;

	private List<MotionEventListener> listeners;

	/**
	 *
	 */
	public MotorCortex() {
		listeners = new ArrayList<MotionEventListener>();
		motions = new HashMap<Integer, Motion>();
		actions = new HashMap<Integer, ParameterizedAction>();
		sensorJoints = new float[Joint.values().length];
	}

	public void init(RobotContext context) {
		robotContext = context;
		effector = context.getEffector();
		sensor = context.getSensor();
		sensoryCortex = context.getSensoryCortex();
		currentMotion = null;
		nextMotion = null;

		registAction(new ShootAction.LeftShootAction());
		registAction(new ShootAction.RightShootAction());
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
		effector.setJointDegree(HeadPitch, 0);
		effector.setJointDegree(HeadYaw, 0);
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

	public void step() {
		SomaticContext sc = sensoryCortex.getContext();
		Robot robot = sc.getRobot();

		Joint[] joints = Joint.values();
		for (int i = 0; i < joints.length; i++) {
			sensorJoints[i] = sensor.getJoint(joints[i]);
		}

		if (nextMotion != currentMotion) {
			// モーションが中断可能であれば中断して次のモーションへ
			// そうでないなら，中断をリクエストする
			if (currentMotion == null || currentMotion.canStop()) {
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
			// モーションを継続
			currentMotion.stepNextFrame(sensor,effector);
//			for (int i = 2; i < joints.length; i++) {
//				float value = clipping(robot.get(Frames.valueOf(joints[i])),
//						frame[i]);
//				effector.setJoint(joints[i], value);
//			}

			// quick hack
			updateOdometry();
		}

		effector.setJoint(HeadYaw, clipping(robot.get(Frames.valueOf(HeadYaw)),
				headYaw));
		effector.setJoint(HeadPitch, clipping(robot.get(Frames
				.valueOf(HeadPitch)), headPitch));
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
		assert motions.containsKey(motion);
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

	public void makemotion_head(float headYawInDeg, float headPitchInDeg) {
		this.headYaw = (float) Math.toRadians(headYawInDeg);
		this.headPitch = (float) Math.toRadians(headPitchInDeg);
	}

	public void makemotion_head_rel(float headYawInDeg, float headPitchInDeg) {
		this.headYaw += (float) Math.toRadians(headYawInDeg);
		this.headPitch += (float) Math.toRadians(headPitchInDeg);
	}

	private void updateOdometry(){
		int df = 0, dl = 0;
		float dh = 0;
		switch (currentMotion.getId()) {
		// このへん全部妄想値．だれか計測してちょ．
		// 精度とキャストに注意
		case Motions.MOTION_LEFT_YY_TURN:
			dh = 21.0f / currentMotion.totalFrames;
			break;
		case Motions.MOTION_RIGHT_YY_TURN:
			dh = -21.0f / currentMotion.totalFrames;
			break;
		case Motions.MOTION_W_FORWARD:
		case Motions.MOTION_YY_FORWARD1:
		case Motions.MOTION_YY_FORWARD2:
		case Motions.MOTION_YY_FORWARD_STEP:
			// xとyは1フレームあたり1.0mm以下の変位はそのまま伝達できないので，
			// ディザリング処理をしてごまかす
			df = (int) (350.0f / currentMotion.totalFrames + Math.random());
			break;
		case Motions.MOTION_YY_FORWARD:
			df = (int) (4.5f + Math.random());
			break;
		case Motions.MOTION_W_BACKWARD:
			df = (int) (-100.0f / currentMotion.totalFrames + Math.random());
			break;
		case Motions.MOTION_CIRCLE_RIGHT:
			dl = (int) (-75.0f / currentMotion.totalFrames + Math.random());
			dh = 8f / currentMotion.totalFrames;
			break;
		case Motions.MOTION_CIRCLE_LEFT:
			dl = (int) (75.0f / currentMotion.totalFrames + Math.random());
			dh = -8f / currentMotion.totalFrames;
			break;
		case Motions.MOTION_W_RIGHT_SIDESTEP:
			dl = (int) (-75.0f / currentMotion.totalFrames + Math.random());
			break;
		case Motions.MOTION_W_LEFT_SIDESTEP:
			dl = (int) (75.0f / currentMotion.totalFrames + Math.random());
			break;
		default:
		}
		fireUpdateOdometry(df, dl, dh);
	}

	public Motion getCurrentMotion() {
		return currentMotion;
	}

	public ParameterizedAction getParaAction(int paraId) {
		return actions.get(paraId);
	}

	public Motion getMotion(int motionId) {
		return motions.get(motionId);
	}

	public void registMotion(Motion motion) {
		motions.put(motion.getId(), motion);
		motion.init(robotContext);
	}

	public void registAction(ParameterizedAction action) {
		actions.put(action.getId(), action);
		action.init(robotContext);
	}

	public boolean hasMotion(int motionId){
		return motions.containsKey(motionId);
	}

	public void addEventListener(MotionEventListener listener) {
		listeners.add(listener);
	}

	public void removeEventListener(MotionEventListener listener) {
		listeners.remove(listener);
	}

	private void fireUpdateOdometry(int forward, int left, float turn) {
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
