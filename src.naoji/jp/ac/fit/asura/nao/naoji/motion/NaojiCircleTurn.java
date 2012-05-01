package jp.ac.fit.asura.nao.naoji.motion;

import org.apache.log4j.Logger;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.MotionParam;
import jp.ac.fit.asura.nao.motion.MotionParam.CircleTurnParam;
import jp.ac.fit.asura.nao.motion.MotionParam.CircleTurnParam.Side;
import jp.ac.fit.asura.naoji.jal.JALMotion;
import jp.ac.fit.asura.naoji.robots.NaoV3R.Joint;

/**
 * 実機用のサークルターン.ターンとサイドステップを連続で実行する.
 *
 * @author takata
 */

public class NaojiCircleTurn extends Motion {
	private static final Logger log = Logger.getLogger(NaojiCircleTurn.class);
	private JALMotion jalmotion;
	private NaojiWalker walker;
	private int samples;
	private float sideDist;
	private float angle;
	private int taskId;

	private float[] turnConfig;

	public NaojiCircleTurn(JALMotion motion, NaojiWalker walker) {
		this.jalmotion = motion;
		this.walker = walker;
		samples = 38;
		angle = 0.28f;
		sideDist = 0.06f;

		taskId = -1;

		turnConfig = new float[] {0.035f, 0.01f, 0.025f, 0.25f, 0.22f, 3.3f};
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

		jalmotion.setWalkConfig(turnConfig[0], turnConfig[1], turnConfig[2], turnConfig[3], turnConfig[4], turnConfig[5]);

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

	public void setSideDist(float dist) {
		this.sideDist = dist;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	public void setSamples(int samples) {
		this.samples = samples;
	}

	public void setWalkConfig(float pMaxStepLength, float pMaxStepHeight, float pMaxStepSide, float pMaxStepTurn, float pHipHeight, float pTorsoYOrientation) {
		turnConfig[0] = pMaxStepLength;
		turnConfig[1] = pMaxStepHeight;
		turnConfig[2] = pMaxStepSide;
		turnConfig[3] = pMaxStepTurn;
		turnConfig[4] = pHipHeight;
		turnConfig[5] = pTorsoYOrientation;

		log.info("set turn's walk config.");
	}
}
