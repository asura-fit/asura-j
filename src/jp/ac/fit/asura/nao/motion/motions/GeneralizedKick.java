package jp.ac.fit.asura.nao.motion.motions;

import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.misc.Coordinates;
import jp.ac.fit.asura.nao.misc.Kinematics;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.misc.SingularPostureException;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.MotionParam;
import jp.ac.fit.asura.nao.motion.MotionParam.ShotParam;
import jp.ac.fit.asura.nao.physical.Robot;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.FrameState;
import jp.ac.fit.asura.nao.sensation.SomaticContext;

import org.apache.log4j.Logger;

/**
 *
 * @author sey
 *
 */
public class GeneralizedKick extends Motion {
	private static final Logger log = Logger.getLogger(GeneralizedKick.class);

	float[] frames;
	int[] times;
	int sequence;
	int sequenceStep;

	boolean isStarted;
	long startTime;
	int totalTimes;

	public GeneralizedKick() {
		setName("GeneralizedKick");
	}

	@Override
	public void start(MotionParam param) throws IllegalArgumentException {
		isStarted = false;
		startTime = context.getTime();
		log.debug("start" + startTime);

		ShotParam sp;
		if (param instanceof ShotParam) {
			sp = (ShotParam) param;
		} else {
			sp = new ShotParam();
		}

		Robot robot = context.getSomaticContext().getRobot();
		SomaticContext sc = new SomaticContext(context.getSomaticContext());

		int len = 9;
		int joints = Joint.values().length - 2;
		frames = new float[len * joints];
		int[] timesJointSpace = new int[len];
		try {
			log.debug("ball: " + sp.getBall());
			for (int i = 0; i < len; i++) {
				log.trace("Frame " + i);
				FrameState ls = new FrameState(robot.get(Frames.LSole));
				FrameState rs = new FrameState(robot.get(Frames.RSole));
				ls.getBodyRotation().setIdentity();
				rs.getBodyRotation().setIdentity();
				switch (i) {
				case 0:
					// 腰を落とす
					ls.getBodyPosition().set(50, -300, 0);
					rs.getBodyPosition().set(-50, -300, 0);
					Kinematics.calculateInverse(sc, ls);
					Kinematics.calculateInverse(sc, rs);
					timesJointSpace[i] = 1000;
					break;
				case 1:
					// 腰を落とす
					ls.getBodyPosition().set(50, -300, 0);
					rs.getBodyPosition().set(-50, -300, 0);
					Kinematics.calculateInverse(sc, ls);
					Kinematics.calculateInverse(sc, rs);
					timesJointSpace[i] = 1000;
					break;
				case 2:
					// 支持脚(左足)に体重を移す
					ls.getBodyPosition().set(-10, -280, 0);
					rs.getBodyPosition().set(-120, -280, 0);
					Kinematics.calculateInverse(sc, ls);
					Kinematics.calculateInverse(sc, rs);
					timesJointSpace[i] = 1500;
					break;
				case 3:
					// 脚を上げる
					ls.getBodyPosition().set(-10, -280, 0);
					rs.getBodyPosition().set(-130, -270, 0);
					Kinematics.calculateInverse(sc, ls);
					Kinematics.calculateInverse(sc, rs);
					timesJointSpace[i] = 500;
					break;
				case 4:
					// 蹴る
					ls.getBodyPosition().set(-10, -280, 0);
					Vector3f ball = new Vector3f();
					Coordinates.robot2bodyCoord(sc, sp.getBall(), ball);
					sp.getBall().z = MathUtils.clipAbs(sp.getBall().z, 130);
					sp.getBall().y = MathUtils.clipAbs(sp.getBall().y, 250);
					rs.getBodyPosition().set(sp.getBall());
					Kinematics.calculateInverse(sc, ls);
					Kinematics.calculateInverse(sc, rs);
					timesJointSpace[i] = 500;
					break;
				case 5:
					// 脚を戻す
					ls.getBodyPosition().set(-10, -280, 0);
					rs.getBodyPosition().set(-130, -270, 0);
					Kinematics.calculateInverse(sc, ls);
					Kinematics.calculateInverse(sc, rs);
					timesJointSpace[i] = 1000;
					break;
				case 6:
					// 脚をおろす
					ls.getBodyPosition().set(-10, -280, 0);
					rs.getBodyPosition().set(-130, -280, 0);
					Kinematics.calculateInverse(sc, ls);
					Kinematics.calculateInverse(sc, rs);
					timesJointSpace[i] = 500;
					break;
				case 7:
					// 腰を戻す
					ls.getBodyPosition().set(50, -290, 0);
					rs.getBodyPosition().set(-50, -290, 0);
					Kinematics.calculateInverse(sc, ls);
					Kinematics.calculateInverse(sc, rs);
					timesJointSpace[i] = 2000;
					break;
				case 8:
					// 腰を戻す
					ls.getBodyPosition().set(50, -290, 0);
					rs.getBodyPosition().set(-50, -290, 0);
					Kinematics.calculateInverse(sc, ls);
					Kinematics.calculateInverse(sc, rs);
					timesJointSpace[i] = 1500;
					break;
				}
				// 計算結果をモーションデータにセット
				for (Frames f : Frames.values())
					if (f.isJoint() && f != Frames.HeadYaw
							&& f != Frames.HeadPitch) {
						int idx = i * joints + f.toJoint().ordinal() - 2;
						if (idx >= 0)
							frames[idx] = sc.get(f).getAngle();
					}
			}
		} catch (SingularPostureException e) {
			totalTimes = 0;
			e.printStackTrace();
			return;
		}

		int time = 500;
		this.times = new int[timesJointSpace.length];
		for (int i = 0; i < timesJointSpace.length; i++) {
			time += timesJointSpace[i];
			this.times[i] = time;
		}
		totalTimes = time + 1000;
		log.debug("new GeneralizedKick with matrix:" + frames.length
				+ " totalTimes:" + totalTimes);
	}

	@Override
	public boolean canAccept(MotionParam param) {
		ShotParam sp;
		if (param instanceof ShotParam) {
			sp = (ShotParam) param;
		} else {
			return false;
		}
		try {
			SomaticContext sc = new SomaticContext(context.getSomaticContext());
			FrameState ls = new FrameState(sc.getRobot().get(Frames.LSole));
			FrameState rs = new FrameState(sc.getRobot().get(Frames.RSole));
			ls.getBodyRotation().setIdentity();
			rs.getBodyRotation().setIdentity();
			ls.getBodyPosition().set(-10, -280, 0);
			Vector3f ball = new Vector3f();
			Coordinates.robot2bodyCoord(sc, sp.getBall(), ball);
			sp.getBall().z = MathUtils.clipAbs(sp.getBall().z, 130);
			sp.getBall().y = MathUtils.clipAbs(sp.getBall().y, 250);
			rs.getBodyPosition().set(sp.getBall());
			Kinematics.calculateInverse(sc, ls);
			Kinematics.calculateInverse(sc, rs);
			return true;
		} catch (SingularPostureException spe) {
			return false;
		}
	}

	@Override
	public void stop() {
		log.debug("stop" + System.currentTimeMillis());
	}

	@Override
	public void step() {
		if (isStarted)
			return;
		context.getRobotContext().getEffector().setBodyJoints(frames, times);
		isStarted = true;
	}

	@Override
	public boolean hasNextStep() {
		long current = context.getTime();
		return current - startTime < totalTimes;
	}
}
