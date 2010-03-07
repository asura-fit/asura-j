package jp.ac.fit.asura.nao.motion.motions;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.misc.Coordinates;
import jp.ac.fit.asura.nao.misc.Kinematics;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.misc.MatrixUtils;
import jp.ac.fit.asura.nao.misc.SingularPostureException;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.MotionParam;
import jp.ac.fit.asura.nao.motion.MotionParam.ShotParam;
import jp.ac.fit.asura.nao.physical.Robot;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.FrameState;
import jp.ac.fit.asura.nao.sensation.SomaticContext;
import jp.ac.fit.asura.vecmathx.GfVector;

import org.apache.log4j.Logger;

/**
 *
 * @author sey
 *
 */
public class GeneralizedKick extends Motion {
	private static final Logger log = Logger.getLogger(GeneralizedKick.class);
	ShotParam lastParam;

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

	private void setBodyAngles(SomaticContext sc, float[] framesInDeg) {
		assert framesInDeg.length == 20;
		sc.get(Frames.LShoulderPitch).setAngle(
				MathUtils.toRadians(framesInDeg[0]));
		sc.get(Frames.LShoulderRoll).setAngle(
				MathUtils.toRadians(framesInDeg[1]));
		sc.get(Frames.LElbowYaw).setAngle(MathUtils.toRadians(framesInDeg[2]));
		sc.get(Frames.LElbowRoll).setAngle(MathUtils.toRadians(framesInDeg[3]));
		sc.get(Frames.LHipYawPitch).setAngle(
				MathUtils.toRadians(framesInDeg[4]));
		sc.get(Frames.LHipRoll).setAngle(MathUtils.toRadians(framesInDeg[5]));
		sc.get(Frames.LHipPitch).setAngle(MathUtils.toRadians(framesInDeg[6]));
		sc.get(Frames.LKneePitch).setAngle(MathUtils.toRadians(framesInDeg[7]));
		sc.get(Frames.LAnklePitch)
				.setAngle(MathUtils.toRadians(framesInDeg[8]));
		sc.get(Frames.LAnkleRoll).setAngle(MathUtils.toRadians(framesInDeg[9]));
		sc.get(Frames.RHipYawPitch).setAngle(
				MathUtils.toRadians(framesInDeg[10]));
		sc.get(Frames.RHipRoll).setAngle(MathUtils.toRadians(framesInDeg[11]));
		sc.get(Frames.RHipPitch).setAngle(MathUtils.toRadians(framesInDeg[12]));
		sc.get(Frames.RKneePitch)
				.setAngle(MathUtils.toRadians(framesInDeg[13]));
		sc.get(Frames.RAnklePitch).setAngle(
				MathUtils.toRadians(framesInDeg[14]));
		sc.get(Frames.RAnkleRoll)
				.setAngle(MathUtils.toRadians(framesInDeg[15]));
		sc.get(Frames.RShoulderPitch).setAngle(
				MathUtils.toRadians(framesInDeg[16]));
		sc.get(Frames.RShoulderRoll).setAngle(
				MathUtils.toRadians(framesInDeg[17]));
		sc.get(Frames.RElbowYaw).setAngle(MathUtils.toRadians(framesInDeg[18]));
		sc.get(Frames.RElbowRoll)
				.setAngle(MathUtils.toRadians(framesInDeg[19]));

	}

	private void setDefaultPosition(SomaticContext sc) {
		setBodyAngles(sc, new float[] { 110, 20, -80, -90, 0, 0, -25, 40, -20,
				0, 0, 0, -25, 40, -20, 0, 110, -20, 80, 90 });
	}

	private void setReadyPositionRight(SomaticContext sc) {
		setBodyAngles(sc, new float[] { 110, 20, -80, -90, 0, -15, -25, 40,
				-20, 20, 0, -15, -25, 40, -20, 20, 110, -20, 80, 90 });
	}

	@Override
	public void start(MotionParam param) throws IllegalArgumentException {
		isStarted = false;
		startTime = context.getTime();
		log.debug("start" + startTime);

		ShotParam sp;
		if (lastParam != null && lastParam.equals(param))
			return;
		if (param instanceof ShotParam) {
			sp = (ShotParam) param;
		} else {
			assert false;
			sp = new ShotParam();
		}
		generateMotion(sp);
	}

	private void generateMotion(ShotParam sp) {
		Robot robot = context.getSomaticContext().getRobot();
		SomaticContext sc = new SomaticContext(context.getSomaticContext());

		int len = 9;
		int joints = Joint.values().length - 2;
		frames = new float[len * joints];
		int[] timesJointSpace = new int[len];
		try {
			log.debug("ball: " + sp.getBall());

			// 腕をセット
			sc.get(Frames.LShoulderPitch).setAngle(MathUtils.toRadians(110));
			sc.get(Frames.LShoulderRoll).setAngle(MathUtils.toRadians(20));
			sc.get(Frames.LElbowYaw).setAngle(MathUtils.toRadians(-80));
			sc.get(Frames.LElbowRoll).setAngle(MathUtils.toRadians(-90));
			sc.get(Frames.RShoulderPitch).setAngle(MathUtils.toRadians(110));
			sc.get(Frames.RShoulderRoll).setAngle(MathUtils.toRadians(-20));
			sc.get(Frames.RElbowYaw).setAngle(MathUtils.toRadians(80));
			sc.get(Frames.RElbowRoll).setAngle(MathUtils.toRadians(90));

			Vector3f vec1 = new Vector3f(-4.7f, 1.71f, 4.93f);
			vec1.scale(MathUtils.PIf / 180);
			Matrix3f supportRot = new Matrix3f();
			MatrixUtils.pyr2rot(vec1, supportRot);

			Vector3f vec2 = new Vector3f(0, 0, 5);
			vec2.scale(MathUtils.PIf / 180);
			Matrix3f swingRot = new Matrix3f();
			MatrixUtils.pyr2rot(vec2, swingRot);

			for (int i = 0; i < len; i++) {
				log.trace("Frame " + i);

				FrameState ls = new FrameState(robot.get(Frames.LSole));
				FrameState rs = new FrameState(robot.get(Frames.RSole));
				switch (i) {
				case 0:
					// 腰を落とす
					setDefaultPosition(sc);
					timesJointSpace[i] = 1000;
					break;
				case 1:
					// 腰を落とす
					setDefaultPosition(sc);
					timesJointSpace[i] = 800;
					break;
				case 2:
					// 支持脚(左足)に体重を移す
					setReadyPositionRight(sc);
					timesJointSpace[i] = 800;
					break;
				case 3:
					// 脚を上げる
					ls.getBodyPosition().set(5, -310, 20);
					rs.getBodyPosition().set(-87, -284, 6.9f);
					ls.getBodyRotation().set(supportRot);
					rs.getBodyRotation().set(swingRot);
					Kinematics.calculateInverse(sc, ls);
					// Kinematics.calculateInverse(sc, rs);
					Kinematics.calculateInverse(sc, rs, 1, 1, 1, 0.125f,
							0.125f, 0.125f);

					timesJointSpace[i] = 800;
					break;
				case 4:
					// 蹴る
					ls.getBodyPosition().set(5, -310, 20);
					ls.getBodyRotation().set(supportRot);

					Vector3f ball = new Vector3f();
					Coordinates.robot2bodyCoord(sc, sp.getBall(), ball);
					sp.getBall().z *= 0.75f;
					sp.getBall().z = MathUtils.clipAbs(sp.getBall().z, 180);
					// sp.getBall().y = MathUtils.clipAbs(sp.getBall().y, 250);
					rs.getBodyPosition().set(sp.getBall());

					// rs.getBodyPosition().set(new Vector3f(-78, -252, 158));
					// Vector3f vec3 = new Vector3f(-9.41f, 3.4f, 4.71f);
					// vec3.scale(MathUtils.PIf / 180);
					// MatrixUtils.pyr2rot(vec3, rs.getBodyRotation());
					//
					rs.getBodyRotation().set(swingRot);
					log.debug("Shot target:" + sp.getBall());

					Kinematics.calculateInverse(sc, ls);
					Kinematics.calculateInverse(sc, rs, 0.125f, 0.0001f, 0.25f,
							0, 0, 0);
					timesJointSpace[i] = 400;
					break;
				case 5:
					// 脚を戻す
					ls.getBodyPosition().set(5, -310, 20);
					rs.getBodyPosition().set(-87, -284, 6.9f);
					ls.getBodyRotation().set(supportRot);
					rs.getBodyRotation().set(swingRot);
					Kinematics.calculateInverse(sc, ls);
					Kinematics.calculateInverse(sc, rs, 1, 1, 1, 0.125f,
							0.125f, 0.125f);
					timesJointSpace[i] = 800;
					break;
				case 6:
					// 脚をおろす
					setReadyPositionRight(sc);
					timesJointSpace[i] = 800;
					break;
				case 7:
				case 8:
					// 腰を戻す
					setDefaultPosition(sc);
					timesJointSpace[i] = 500;
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
		if (param != null && param instanceof ShotParam) {
			sp = (ShotParam) param;
		} else {
			return false;
		}
		try {
			generateMotion(sp);
			lastParam = sp;
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
