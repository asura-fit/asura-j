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

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class MotorCortex implements RobotLifecycle {
	private Map<Integer, Motion> motions;
	private Effector effector;
	private Sensor sensor;
	private Motion currentMotion;
	private float headYaw;
	private float headPitch;

	private float[] sensorJoints;

	private Motion nextMotion;

	private List<MotionEventListener> listeners;

	/**
	 * 
	 */
	public MotorCortex() {
		listeners = new ArrayList<MotionEventListener>();
		motions = new HashMap<Integer, Motion>();
		sensorJoints = new float[Joint.values().length];
	}

	public void init(RobotContext context) {
		effector = context.getEffector();
		sensor = context.getSensor();
		currentMotion = null;
		nextMotion = null;
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
		Joint[] joints = Joint.values();
		for (int i = 0; i < joints.length; i++) {
			sensorJoints[i] = sensor.getJoint(joints[i]);
		}

		if (nextMotion != currentMotion) {
			// モーションが中断可能であれば中断して次のモーションへ
			if (currentMotion == null || currentMotion.canStop()) {
				// 動作中のモーションを中断する
				if (currentMotion != null) {
					currentMotion.stop();
				}

				currentMotion = nextMotion;
				// モーションを開始
				currentMotion.start();
			}
		}

		if (currentMotion == null) {
			setDefaultPosition();
		} else {
			if (!currentMotion.hasNextStep()) {
				// モーションを終了
				currentMotion.stop();
				// currentMotion = null;
				// 次のモーションを連続実行
				currentMotion.start();
			}
			// モーションを継続
			float[] frame = currentMotion.stepNextFrame(sensorJoints);
			for (int i = 2; i < joints.length; i++) {
				effector.setJoint(joints[i], frame[i]);
			}

			// quick hack
			switch (currentMotion.getId()) {
			default:
				fireUpdateOdometry(0, 0, 0.0f);
			}
		}

		effector.setJoint(HeadYaw, headYaw);
		effector.setJoint(HeadPitch, headPitch);
	}

	private void fireUpdateOdometry(int forward, int left, float turn) {
		for (MotionEventListener l : listeners)
			l.updateOdometry(forward, left, turn);
	}

	public void makemotion(int motion, Object param) {
		assert motions.containsKey(motion);
		nextMotion = motions.get(motion);
	}

	public void makemotion_head(float headYawInDeg, float headPitchInDeg) {
		this.headYaw = (float) Math.toRadians(headYawInDeg);
		this.headPitch = (float) Math.toRadians(headPitchInDeg);
	}

	public void makemotion_head_rel(float headYawInDeg, float headPitchInDeg) {
		this.headYaw += (float) Math.toRadians(headYawInDeg);
		this.headPitch += (float) Math.toRadians(headPitchInDeg);
	}

	public void registMotion(Motion motion) {
		motions.put(motion.getId(), motion);
	}

	public void addEventListener(MotionEventListener listener) {
		listeners.add(listener);
	}

	public void removeEventListener(MotionEventListener listener) {
		listeners.remove(listener);
	}
}
