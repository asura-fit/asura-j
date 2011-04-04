package jp.ac.fit.asura.nao.naoji.motion;

import org.apache.log4j.Logger;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.MotionParam;
import jp.ac.fit.asura.nao.motion.MotionParam.CircleTurnParam;
import jp.ac.fit.asura.nao.motion.MotionParam.CircleTurnParam.Side;
import jp.ac.fit.asura.naoji.jal.JALMotion;
import jp.ac.fit.asura.naoji.robots.NaoV3R.Joint;

public class NaojiCircleTurn extends Motion {
	private static final Logger log = Logger.getLogger(NaojiCircleTurn.class);
	private JALMotion jalmotion;
	private NaojiWalker walker;
	private int samples;
	private float sideDist;
	private float angle;
	private int taskId;

	public NaojiCircleTurn(JALMotion motion, NaojiWalker walker) {
		this.jalmotion = motion;
		this.walker = walker;
		samples = 38;
		angle = 0.21f;
		sideDist = 0.042f;

		taskId = -1;
	}

	@Override
	public String getName() {
		return "NaojiCircleTurn";
	}

	@Override
	public boolean canAccept(MotionParam param) {
		return param instanceof MotionParam.CircleTurnParam;
	}

	@Override
	public void start(MotionParam param) throws IllegalArgumentException {
		float turn;
		float left;

		if (isRunning()) {
			log.warn("Motion " + taskId + " is running.");
			return;
		}
		taskId = -1;

		assert param instanceof MotionParam.CircleTurnParam;
		CircleTurnParam turnp = (CircleTurnParam) param;

		// NaojiWalkerで使用されているJointStiffnessと同じものを使用する
		jalmotion.setJointStiffness(Joint.RHipPitch.getId(), walker
				.getJointStiffnesses().get(Joint.RHipPitch));
		jalmotion.setJointStiffness(Joint.LHipPitch.getId(), walker
				.getJointStiffnesses().get(Joint.LHipPitch));
		jalmotion.setJointStiffness(Joint.RHipYawPitch.getId(), walker
				.getJointStiffnesses().get(Joint.RHipYawPitch));
		jalmotion.setJointStiffness(Joint.LHipYawPitch.getId(), walker
				.getJointStiffnesses().get(Joint.LHipYawPitch));
		jalmotion.setJointStiffness(Joint.RHipRoll.getId(), walker
				.getJointStiffnesses().get(Joint.RHipRoll));
		jalmotion.setJointStiffness(Joint.LHipRoll.getId(), walker
				.getJointStiffnesses().get(Joint.LHipRoll));
		jalmotion.setJointStiffness(Joint.RAnkleRoll.getId(), walker
				.getJointStiffnesses().get(Joint.RAnkleRoll));
		jalmotion.setJointStiffness(Joint.LAnkleRoll.getId(), walker
				.getJointStiffnesses().get(Joint.LAnkleRoll));
		jalmotion.setJointStiffness(Joint.RKneePitch.getId(), walker
				.getJointStiffnesses().get(Joint.RKneePitch));
		jalmotion.setJointStiffness(Joint.LKneePitch.getId(), walker
				.getJointStiffnesses().get(Joint.LKneePitch));
		jalmotion.setJointStiffness(Joint.RAnklePitch.getId(), walker
				.getJointStiffnesses().get(Joint.RAnklePitch));
		jalmotion.setJointStiffness(Joint.LAnklePitch.getId(), walker
				.getJointStiffnesses().get(Joint.LAnklePitch));



		if (turnp.getSide() == Side.Left) {
			turn = angle * -1.0f;
			left = sideDist;
		} else {
			turn = angle;
			left = sideDist * -1.0f;
		}

		jalmotion.addTurn(turn, samples);
		jalmotion.addWalkSideways(left, samples);

		taskId = jalmotion.walk();
	}

	@Override
	public boolean canStop() {
		boolean active = jalmotion.walkIsActive();
		log.trace("canStop? " + active);
		return !active && !isRunning();
	}

	@Override
	public void requestStop() {
		log.debug("requestStop is called.");
	}

	@Override
	public boolean hasNextStep() {
		boolean active = jalmotion.walkIsActive();
		log.trace("hasNextStep? " + active);
		return active || isRunning();
	}

	@Override
	public void stop() {
		log.debug("stop() called.");
	}

	@Override
	public void step() {
	}

	/**
	 * NaojiCircleTurnが実行中かどうかを返す. 実行中でなければtaskId=-1になる.
	 *
	 * @return
	 */
	public boolean isRunning() {
		if (taskId < 0) {
			return false;
		}
		boolean isRunning = jalmotion.isRunning(taskId);
		log.trace("isRunning? " + isRunning);
		if (!isRunning)
			taskId = -1;
		return isRunning;
	}
}
