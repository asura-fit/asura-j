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
		time = 0;
	}

	public void start() {
		currentMotion = null;
		time = 0;
		// set default position
		setDefaultPosition();
	}

	public void stop() {
	}

	public void registerMotion(Integer id, Motion motion) {
		assert motion != null;
		motions.put(id, motion);
	}

	private void setDefaultPosition() {
		effector.setJointDegree(HeadPitch, 0);
		effector.setJointDegree(HeadYaw, 0);
		effector.setJointDegree(LShoulderPitch, 110);
		effector.setJointDegree(LShoulderRoll, 20);
		effector.setJointDegree(LElbowYaw, -80);
		effector.setJointDegree(LElbowRoll, -90);
		effector.setJointDegree(RShoulderPitch, 110);
		effector.setJointDegree(RShoulderRoll, -20);
		effector.setJointDegree(RElbowYaw, 80);
		effector.setJointDegree(RElbowRoll, 90);
		effector.setJointDegree(LHipYawPitch, 0);
		effector.setJointDegree(RHipYawPitch, 0);
		effector.setJointDegree(LHipPitch, -25);
		effector.setJointDegree(LHipRoll, 0);
		effector.setJointDegree(LKneePitch, 40);
		effector.setJointDegree(LAnklePitch, -20);
		effector.setJointDegree(LAnkleRoll, 0);
		effector.setJointDegree(RHipPitch, -25);
		effector.setJointDegree(RHipRoll, 0);
		effector.setJointDegree(RKneePitch, 40);
		effector.setJointDegree(RAnklePitch, -20);
		effector.setJointDegree(RAnkleRoll, 0);
	}

	public void step(int ts) {
		Motion m = currentMotion;

		sensorJoints[0] = sensor.getJoint(HeadPitch);
		sensorJoints[1] = sensor.getJoint(HeadYaw);
		sensorJoints[2] = sensor.getJoint(LShoulderPitch);
		sensorJoints[3] = sensor.getJoint(LShoulderRoll);
		sensorJoints[4] = sensor.getJoint(LElbowYaw);
		sensorJoints[5] = sensor.getJoint(LElbowRoll);
		sensorJoints[6] = sensor.getJoint(LHipYawPitch);
		sensorJoints[7] = sensor.getJoint(LHipPitch);
		sensorJoints[8] = sensor.getJoint(LHipRoll);
		sensorJoints[9] = sensor.getJoint(LKneePitch);
		sensorJoints[10] = sensor.getJoint(LAnklePitch);
		sensorJoints[11] = sensor.getJoint(LAnkleRoll);
		sensorJoints[12] = sensor.getJoint(RHipYawPitch);
		sensorJoints[13] = sensor.getJoint(RHipPitch);
		sensorJoints[14] = sensor.getJoint(RHipRoll);
		sensorJoints[15] = sensor.getJoint(RKneePitch);
		sensorJoints[16] = sensor.getJoint(RAnklePitch);
		sensorJoints[17] = sensor.getJoint(RAnkleRoll);
		sensorJoints[18] = sensor.getJoint(RShoulderPitch);
		sensorJoints[19] = sensor.getJoint(RShoulderRoll);
		sensorJoints[20] = sensor.getJoint(RElbowYaw);
		sensorJoints[21] = sensor.getJoint(RElbowRoll);

		if (m == null) {
			setDefaultPosition();
		} else if (m.hasNextStep()) {
			float[] frame = m.stepNextFrame(sensorJoints);
			// effector.setJoint(HeadYaw, frame[0]);
			// effector.setJoint(HeadPitch, frame[1]);
			effector.setJoint(LShoulderPitch, frame[2]);
			effector.setJoint(LShoulderRoll, frame[3]);
			effector.setJoint(LElbowYaw, frame[4]);
			effector.setJoint(LElbowRoll, frame[5]);
			effector.setJoint(LHipYawPitch, frame[6]);
			effector.setJoint(LHipPitch, frame[7]);
			effector.setJoint(LHipRoll, frame[8]);
			effector.setJoint(LKneePitch, frame[9]);
			effector.setJoint(LAnklePitch, frame[10]);
			effector.setJoint(LAnkleRoll, frame[11]);
			effector.setJoint(RHipYawPitch, frame[12]);
			effector.setJoint(RHipPitch, frame[13]);
			effector.setJoint(RHipRoll, frame[14]);
			effector.setJoint(RKneePitch, frame[15]);
			effector.setJoint(RAnklePitch, frame[16]);
			effector.setJoint(RAnkleRoll, frame[17]);
			effector.setJoint(RShoulderPitch, frame[18]);
			effector.setJoint(RShoulderRoll, frame[19]);
			effector.setJoint(RElbowYaw, frame[20]);
			effector.setJoint(RElbowRoll, frame[21]);
		} else {
			currentMotion = null;
		}
		effector.setJoint(HeadYaw, headYaw);
		effector.setJoint(HeadPitch, headPitch);

		time += ts;
	}

	public void makemotion(Integer motion, Object param) {
		assert motions.containsKey(motion);
		Motion m = motions.get(motion);
		if (m != currentMotion) {
			// 動作中のモーションを終了
			if (currentMotion != null)
				currentMotion.stop();

			currentMotion = m;

			// モーションを開始
			currentMotion.start();
		}
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
