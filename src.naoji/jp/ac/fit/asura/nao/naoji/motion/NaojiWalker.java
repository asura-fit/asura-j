/*
 * 作成日: 2009/04/11
 */
package jp.ac.fit.asura.nao.naoji.motion;

import java.util.EnumMap;

import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.MotionParam;
import jp.ac.fit.asura.nao.motion.MotionParam.WalkParam;
import jp.ac.fit.asura.naoji.jal.JALMotion;
import jp.ac.fit.asura.naoji.robots.NaoV3R.Joint;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 *
 * @version $Id: $
 *
 */
public class NaojiWalker extends Motion {
	private static final Logger log = Logger.getLogger(NaojiWalker.class);
	private JALMotion jalmotion;
	private int samples;
	private int taskId;
	private EnumMap<Joint, Float> jointStiffnesses;

	public NaojiWalker(JALMotion motion) {
		this.jalmotion = motion;
		this.samples = 38;
		taskId = -1;

		jointStiffnesses = new EnumMap<Joint, Float>(Joint.class);
		setJointStiffness(Joint.RHipPitch, 0.8f);
		setJointStiffness(Joint.LHipPitch, 0.8f);
		setJointStiffness(Joint.RHipYawPitch, 0.6f);
		setJointStiffness(Joint.LHipYawPitch, 0.6f);
		setJointStiffness(Joint.RHipRoll, 0.7f);
		setJointStiffness(Joint.LHipRoll, 0.7f);
		setJointStiffness(Joint.RAnkleRoll, 0.9f);
		setJointStiffness(Joint.LAnkleRoll, 0.9f);
		setJointStiffness(Joint.RKneePitch, 0.7f);
		setJointStiffness(Joint.LKneePitch, 0.7f);
		setJointStiffness(Joint.RAnklePitch, 0.5f);
		setJointStiffness(Joint.LAnklePitch, 0.5f);
	}

	@Override
	public boolean canAccept(MotionParam param) {
		return param instanceof MotionParam.WalkParam;
	}

	@Override
	public void start(MotionParam param) {
		if (isRunning()) {
			log.warn("Motion " + taskId + " is running.");
			return;
		}
		taskId = -1;

		assert param instanceof MotionParam.WalkParam;
		WalkParam walkp = (WalkParam) param;

		float forward = MathUtils.clipAbs(walkp.getForward(), 5.0f);
		float left = MathUtils.clipAbs(walkp.getLeft(), 5.0f);
		float turn = MathUtils.clipAbs(walkp.getTurn(), MathUtils.PIf);

		if (forward == 0 && turn == 0 && left == 0) {
			jalmotion.setJointStiffness(Joint.RHipPitch.getId(), 1.0f);
			jalmotion.setJointStiffness(Joint.LHipPitch.getId(), 1.0f);
			jalmotion.setJointStiffness(Joint.RHipYawPitch.getId(), 1.0f);
			jalmotion.setJointStiffness(Joint.LHipYawPitch.getId(), 1.0f);
			jalmotion.setJointStiffness(Joint.RHipRoll.getId(), 1.0f);
			jalmotion.setJointStiffness(Joint.LHipRoll.getId(), 1.0f);
			jalmotion.setJointStiffness(Joint.RAnkleRoll.getId(), 1.0f);
			jalmotion.setJointStiffness(Joint.LAnkleRoll.getId(), 1.0f);
			jalmotion.setJointStiffness(Joint.RKneePitch.getId(), 1.0f);
			jalmotion.setJointStiffness(Joint.LKneePitch.getId(), 1.0f);
			jalmotion.setJointStiffness(Joint.RAnklePitch.getId(), 1.0f);
			jalmotion.setJointStiffness(Joint.LAnklePitch.getId(), 1.0f);
		} else {
			jalmotion.setJointStiffness(Joint.RHipPitch.getId(), jointStiffnesses.get(Joint.RHipPitch));
			jalmotion.setJointStiffness(Joint.LHipPitch.getId(), jointStiffnesses.get(Joint.LHipPitch));
			jalmotion.setJointStiffness(Joint.RHipYawPitch.getId(), jointStiffnesses.get(Joint.RHipYawPitch));
			jalmotion.setJointStiffness(Joint.LHipYawPitch.getId(), jointStiffnesses.get(Joint.LHipYawPitch));
			jalmotion.setJointStiffness(Joint.RHipRoll.getId(), jointStiffnesses.get(Joint.RHipRoll));
			jalmotion.setJointStiffness(Joint.LHipRoll.getId(), jointStiffnesses.get(Joint.LHipRoll));
			jalmotion.setJointStiffness(Joint.RAnkleRoll.getId(), jointStiffnesses.get(Joint.RAnkleRoll));
			jalmotion.setJointStiffness(Joint.LAnkleRoll.getId(), jointStiffnesses.get(Joint.LAnkleRoll));
			jalmotion.setJointStiffness(Joint.RKneePitch.getId(), jointStiffnesses.get(Joint.RKneePitch));
			jalmotion.setJointStiffness(Joint.LKneePitch.getId(), jointStiffnesses.get(Joint.LKneePitch));
			jalmotion.setJointStiffness(Joint.RAnklePitch.getId(), jointStiffnesses.get(Joint.RAnklePitch));
			jalmotion.setJointStiffness(Joint.LAnklePitch.getId(), jointStiffnesses.get(Joint.LAnklePitch));
		}
		if (forward != 0 && turn != 0) {
			log.debug("walkArc with:" + param);
			taskId = jalmotion.walkArc(turn, forward, samples);
		} else if (forward != 0) {
			log.debug("walkForward with:" + param);
			taskId = jalmotion.walkStraight(forward, samples);
		} else if (left != 0) {
			log.debug("walkSideways with:" + param);
			taskId = jalmotion.walkSideways(left, samples);
		} else if (turn != 0) {
			log.debug("turn with:" + param);
			taskId = jalmotion.turn(turn, samples);
		}
	}

	@Override
	public void step() {
	}

	@Override
	public void stop() {
		log.debug("stop() called.");
		// jalmotion.clearFootsteps();
	}

	@Override
	public boolean canStop() {
		boolean active = jalmotion.walkIsActive();
		log.trace("canStop? " + active);
		return !active && !isRunning();
	}

	@Override
	public String getName() {
		return "NaojiWalker";
	}

	@Override
	public void requestStop() {
		log.debug("requestStop is called.");
		// jalmotion.clearFootsteps();
	}

	@Override
	public boolean hasNextStep() {
		boolean active = jalmotion.walkIsActive();
		log.trace("hasNextStep? " + active);
		return active || isRunning();
	}

	public boolean isRunning() {
		if (taskId < 0)
			return false;
		boolean isRunning = jalmotion.isRunning(taskId);
		log.trace("isRunning? " + isRunning);
		if (!isRunning)
			taskId = -1;
		return isRunning;
	}

	/**
	 * 関節jointの歩く時のStiffnessを設定する.
	 * @param joint
	 * @param value
	 */
	public void setJointStiffness(Joint joint, float value) {
		jointStiffnesses.put(joint, value);
	}

	/**
	 * 関節jointの歩く時のStiffnessを設定する.(schemeから呼び出す用)
	 * @param joint
	 * @param value
	 */
	public void setJointStiffness(String joint, float value) {
		Joint j = Joint.valueOf(joint);
		if (j == null) {
			log.error("setJointStiffness: Invalid Joint " + j);
			return;
		}
		setJointStiffness(j, value);
	}

	public void setWalkSamples(int samples) {
		this.samples = samples;
	}
}
