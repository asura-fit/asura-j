package jp.ac.fit.asura.nao.motion.motions;

import java.util.List;

import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.misc.Kinematics;
import jp.ac.fit.asura.nao.misc.MatrixUtils;
import jp.ac.fit.asura.nao.misc.SingularPostureException;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.MotionParam;
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
public class CartesianMotion extends Motion {
	private static final Logger log = Logger.getLogger(CartesianMotion.class);

	public static class DataFrame {
		public int time;
		public List<ChainFrame> chains;
	}

	public static class ChainFrame {
		public Frames chainId;
		public Vector3f position;
		public Vector3f postureYpr;
		public Vector3f positionWeight;
		public Vector3f postureWeight;
	}

	float[] frames;
	int[] times;
	int sequence;
	int sequenceStep;

	boolean isStarted;
	long startTime;
	int totalTimes;

	public CartesianMotion(Robot robot, List<DataFrame> args)
			throws SingularPostureException {
		int joints = Joint.values().length - 2;
		frames = new float[args.size() * joints];
		int[] timesJointSpace = new int[args.size()];
		SomaticContext sc = new SomaticContext(robot);
		for (int i = 0; i < args.size(); i++) {
			DataFrame d = args.get(i);
			log.trace("Frame " + i);
			// 前のフレームの値を引き継ぐ
			if (i != 0)
				System.arraycopy(frames, (i - 1) * joints, frames, i * joints,
						joints);

			// フレーム中のチェインの計算をする
			for (ChainFrame c : d.chains) {
				log.trace("Frame " + i + " Chain " + c.chainId + " pos:"
						+ c.position + " ypr:" + c.postureYpr);
				// 逆運動学計算を実施
				FrameState t = new FrameState(robot.get(c.chainId));
				t.getBodyPosition().set(c.position);
				MatrixUtils.pyr2rot(c.postureYpr, t.getBodyRotation());
				GfVector w = new GfVector(new float[] { c.positionWeight.x,
						c.positionWeight.y, c.positionWeight.z,
						c.postureWeight.x, c.postureWeight.y,
						c.postureWeight.z, });
				Kinematics.calculateInverse(sc, Frames.Body, t);

				// 計算結果をモーションデータにセット
				Frames[] route = robot.findRoute(Frames.Body, c.chainId);
				for (Frames f : route) {
					if (f.isJoint()) {
						int idx = i * joints + f.toJoint().ordinal() - 2;
						frames[idx] = sc.get(f).getAngle();
					}
				}
			}
			timesJointSpace[i] = d.time;
		}

		int time = 250;
		this.times = new int[timesJointSpace.length];
		for (int i = 0; i < timesJointSpace.length; i++) {
			time += timesJointSpace[i];
			this.times[i] = time;
		}
		totalTimes = time + 1000;
		log.debug("new CartesianMotion with matrix:" + frames.length
				+ " totalTimes:" + totalTimes);
		log.trace("CartesianMotion created.");
	}

	@Override
	public void start(MotionParam param) throws IllegalArgumentException {
		isStarted = false;
		startTime = context.getTime();
		log.debug("start" + startTime);
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
