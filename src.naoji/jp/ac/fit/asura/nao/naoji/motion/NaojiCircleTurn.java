package jp.ac.fit.asura.nao.naoji.motion;

import java.util.EnumMap;
import java.util.Map;

import org.apache.log4j.Logger;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.misc.MathUtils;
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

	private int ss;
	private int ts;

	private int taskId;

	private boolean setJointFlg;

	private Map<Joint, Float> jointStiffnesses;

	private float[] pose;

	public NaojiCircleTurn(JALMotion motion, NaojiWalker walker) {
		this.jalmotion = motion;
		this.walker = walker;
		samples = ss = ts = 38;
		angle = 0.28f;
		sideDist = 0.06f;

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

		pose = new float[] { MathUtils.toRadians(85), MathUtils.toRadians(13),
				MathUtils.toRadians(-7), MathUtils.toRadians(-79),
				MathUtils.toRadians(0), MathUtils.toRadians(-3),
				MathUtils.toRadians(-19), MathUtils.toRadians(33),
				MathUtils.toRadians(-15), MathUtils.toRadians(1),
				MathUtils.toRadians(0), MathUtils.toRadians(1),
				MathUtils.toRadians(-19), MathUtils.toRadians(33),
				MathUtils.toRadians(-15), MathUtils.toRadians(-1),
				MathUtils.toRadians(87), MathUtils.toRadians(-14),
				MathUtils.toRadians(7), MathUtils.toRadians(78) };

//		pose = new float[] { MathUtils.toRadians(85), MathUtils.toRadians(13),
//				MathUtils.toRadians(-7), MathUtils.toRadians(-79),
//
//				MathUtils.toRadians(0),MathUtils.toRadians(0),
//				MathUtils.toRadians(-31), MathUtils.toRadians(54),
//				MathUtils.toRadians(-27), MathUtils.toRadians(0),
//
//				MathUtils.toRadians(0), MathUtils.toRadians(0),
//				MathUtils.toRadians(-30), MathUtils.toRadians(54),
//				MathUtils.toRadians(-27), MathUtils.toRadians(0),
//
//				MathUtils.toRadians(87), MathUtils.toRadians(-14),
//				MathUtils.toRadians(7), MathUtils.toRadians(78) };
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

		if (!setJointFlg) {
			setJointStiffness(Joint.RHipPitch, walker.getJointStiffnesses()
					.get(Joint.RHipPitch));
			setJointStiffness(Joint.LHipPitch, walker.getJointStiffnesses()
					.get(Joint.LHipPitch));
			setJointStiffness(Joint.RHipYawPitch, walker.getJointStiffnesses()
					.get(Joint.RHipYawPitch));
			setJointStiffness(Joint.LHipYawPitch, walker.getJointStiffnesses()
					.get(Joint.LHipYawPitch));
			setJointStiffness(Joint.RHipRoll,
					walker.getJointStiffnesses().get(Joint.RHipRoll));
			setJointStiffness(Joint.LHipRoll,
					walker.getJointStiffnesses().get(Joint.LHipRoll));
			setJointStiffness(Joint.RAnkleRoll, walker.getJointStiffnesses()
					.get(Joint.RAnkleRoll));
			setJointStiffness(Joint.LAnkleRoll, walker.getJointStiffnesses()
					.get(Joint.LAnkleRoll));
			setJointStiffness(Joint.RKneePitch, walker.getJointStiffnesses()
					.get(Joint.RKneePitch));
			setJointStiffness(Joint.LKneePitch, walker.getJointStiffnesses()
					.get(Joint.LKneePitch));
			setJointStiffness(Joint.RAnklePitch, walker.getJointStiffnesses()
					.get(Joint.RAnklePitch));
			setJointStiffness(Joint.LAnklePitch, walker.getJointStiffnesses()
					.get(Joint.LAnklePitch));
		}

		jalmotion.setJointStiffness(Joint.RHipPitch.getId(),
				jointStiffnesses.get(Joint.RHipPitch));
		jalmotion.setJointStiffness(Joint.LHipPitch.getId(),
				jointStiffnesses.get(Joint.LHipPitch));
		jalmotion.setJointStiffness(Joint.RHipYawPitch.getId(),
				jointStiffnesses.get(Joint.RHipYawPitch));
		jalmotion.setJointStiffness(Joint.LHipYawPitch.getId(),
				jointStiffnesses.get(Joint.LHipYawPitch));
		jalmotion.setJointStiffness(Joint.RHipRoll.getId(),
				jointStiffnesses.get(Joint.RHipRoll));
		jalmotion.setJointStiffness(Joint.LHipRoll.getId(),
				jointStiffnesses.get(Joint.LHipRoll));
		jalmotion.setJointStiffness(Joint.RAnkleRoll.getId(),
				jointStiffnesses.get(Joint.RAnkleRoll));
		jalmotion.setJointStiffness(Joint.LAnkleRoll.getId(),
				jointStiffnesses.get(Joint.LAnkleRoll));
		jalmotion.setJointStiffness(Joint.RKneePitch.getId(),
				jointStiffnesses.get(Joint.RKneePitch));
		jalmotion.setJointStiffness(Joint.LKneePitch.getId(),
				jointStiffnesses.get(Joint.LKneePitch));
		jalmotion.setJointStiffness(Joint.RAnklePitch.getId(),
				jointStiffnesses.get(Joint.RAnklePitch));
		jalmotion.setJointStiffness(Joint.LAnklePitch.getId(),
				jointStiffnesses.get(Joint.LAnklePitch));

		if (turnp.getSide() == Side.Left) {
			turn = angle * -1.0f;
			left = sideDist;
		} else {
			turn = angle;
			left = sideDist * -1.0f;
		}

		jalmotion.turn(turn, ts);
		while (jalmotion.walkIsActive())
			;
		gotoBodyAngles(pose);
		while (jalmotion.isRunning(taskId))
			;
		log.debug("iiatai");
		jalmotion.walkSideways(left, ss);

		// taskId = jalmotion.walk();
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
		log.info("set circle turn's side dist: " + this.sideDist);
	}

	public void setAngle(float angle) {
		this.angle = angle;
		log.info("set circle turn's angle: " + this.angle);
	}

	public void setJointStiffness(Joint joint, float value) {
		jointStiffnesses.put(joint, value);

		setJointFlg = true;
	}

	/**
	 * 関節jointのCircleTurn時のStiffnessを設定する.(schemeから呼び出す用)
	 *
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

	public void setSamples(int samples) {
		this.samples = ss = ts = samples;
		log.info("set circle turn's samples: " + this.samples);
	}

	public void setSideSamples(int ssamples) {
		ss = ssamples;
	}

	public void setTurnSamples(int tsamples) {
		ts = tsamples;
	}

	private void gotoBodyAngles(float[] pose) {
		jalmotion.gotoAngleWithSpeed(Joint.LShoulderPitch.getId(), pose[0], 5,
				1);
		jalmotion.gotoAngleWithSpeed(Joint.LShoulderRoll.getId(), pose[1], 5,
				1);
		jalmotion.gotoAngleWithSpeed(Joint.LElbowRoll.getId(), pose[2], 5, 1);
		jalmotion.gotoAngleWithSpeed(Joint.LElbowYaw.getId(), pose[3], 5, 1);

		jalmotion
				.gotoAngleWithSpeed(Joint.LHipYawPitch.getId(), pose[4], 5, 1);
		jalmotion.gotoAngleWithSpeed(Joint.LHipRoll.getId(), pose[5], 5, 1);
		jalmotion.gotoAngleWithSpeed(Joint.LHipPitch.getId(), pose[6], 5, 1);
		jalmotion.gotoAngleWithSpeed(Joint.LKneePitch.getId(), pose[7], 5, 1);
		jalmotion.gotoAngleWithSpeed(Joint.LAnklePitch.getId(), pose[8], 5, 1);
		jalmotion.gotoAngleWithSpeed(Joint.LAnkleRoll.getId(), pose[9], 5, 1);

		jalmotion.gotoAngleWithSpeed(Joint.RHipYawPitch.getId(), pose[10], 5,
				1);
		jalmotion.gotoAngleWithSpeed(Joint.RHipRoll.getId(), pose[11], 5, 1);
		jalmotion.gotoAngleWithSpeed(Joint.RHipPitch.getId(), pose[12], 5, 1);
		jalmotion.gotoAngleWithSpeed(Joint.RKneePitch.getId(), pose[13], 5, 1);
		jalmotion
				.gotoAngleWithSpeed(Joint.RAnklePitch.getId(), pose[14], 5, 1);
		jalmotion.gotoAngleWithSpeed(Joint.RAnkleRoll.getId(), pose[15], 5, 1);

		jalmotion.gotoAngleWithSpeed(Joint.RShoulderPitch.getId(), pose[16],
				5, 1);
		jalmotion.gotoAngleWithSpeed(Joint.RShoulderRoll.getId(), pose[17], 5,
				1);
		jalmotion.gotoAngleWithSpeed(Joint.RElbowRoll.getId(), pose[18], 5, 1);
		taskId = jalmotion.gotoAngleWithSpeed(Joint.RElbowYaw.getId(),
				pose[19], 5, 1);
	}

	public void setPose(Object pose) {
		Object[] row = (Object[]) pose;
		int cols = row.length;
		float[] p = new float[cols];
		try {
		for (int i=0; i<cols; i++) {
			p[i] = Float.parseFloat(row[i].toString());
		}
		} catch (NumberFormatException e) {
			log.error(e);
		}

		for (int i=0; i<this.pose.length; i++) {
			this.pose[i] = MathUtils.toRadians(p[i]);
		}
	}
}
