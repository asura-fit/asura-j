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

import java.util.HashMap;
import java.util.Map;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.Sensor;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class MotorCortex implements RobotLifecycle {
	private Map<Integer, Motion> motions;
	private Effector effector;
	private Sensor sensor;
	private int time;
	private Motion currentMotion;
	private float headYaw;
	private float headPitch;

	private float[] sensorJoints;

	private Motion nextMotion;

	/**
	 * 
	 */
	public MotorCortex() {
		motions = new HashMap<Integer, Motion>();
		sensorJoints = new float[Joint.values().length];
	}

	public void init(RobotContext context) {
		effector = context.getEffector();
		sensor = context.getSensor();
		currentMotion = null;
		nextMotion = null;
		time = 0;
	}

	public void start() {
		currentMotion = null;
		nextMotion = null;
		time = 0;
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

	public void step(int ts) {
		Joint[] joints = Joint.values();
		for (int i = 0; i < joints.length; i++) {
			sensorJoints[i] = sensor.getJoint(joints[i]);
		}

		if (nextMotion != currentMotion) {
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
		} else if (currentMotion.hasNextStep()) {
			float[] frame = currentMotion.stepNextFrame(sensorJoints);
			for (int i = 2; i < joints.length; i++) {
				effector.setJoint(joints[i], frame[i]);
			}
		} else {
			// モーションを終了
			currentMotion.stop();
			// currentMotion = null;
			// 次のモーションを連続実行
			currentMotion.start();
		}
		effector.setJoint(HeadYaw, headYaw);
		effector.setJoint(HeadPitch, headPitch);

		time += ts;
	}

	public void makemotion(int motion, Object param) {
		assert motions.containsKey(motion);
		nextMotion = motions.get(motion);
	}

	public void makemotion_head(float headYaw, float headPitch) {
		this.headYaw = headYaw;
		this.headPitch = headPitch;
	}

	public void makemotion_head_rel(float headYaw, float headPitch) {
		this.headYaw += headYaw;
		this.headPitch += headPitch;
	}

	public void registMotion(int id, Motion motion) {
		motions.put(id, motion);
	}
}
