package jp.ac.fit.asura.nao.motion.motions;

import static jp.ac.fit.asura.nao.PressureSensor.LFsrBL;
import static jp.ac.fit.asura.nao.PressureSensor.LFsrBR;
import static jp.ac.fit.asura.nao.PressureSensor.LFsrFL;
import static jp.ac.fit.asura.nao.PressureSensor.LFsrFR;
import static jp.ac.fit.asura.nao.PressureSensor.RFsrBL;
import static jp.ac.fit.asura.nao.PressureSensor.RFsrBR;
import static jp.ac.fit.asura.nao.PressureSensor.RFsrFL;
import static jp.ac.fit.asura.nao.PressureSensor.RFsrFR;
import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.motion.MotionParam;

import org.apache.log4j.Logger;

@Deprecated
public class ForwardMotion extends CompatibleMotion {
	private Logger log = Logger.getLogger(ForwardMotion.class);
	private RobotContext robotContext;

	private boolean canStop = false;
	private boolean stopRequested = false;

	public boolean canStop() {
		if (currentStep <= 0) {
			// ストップするときは、フレームが終わるか、モーションが始まってないとき
			return true;
		}

		if (sequence >= 30 && sequenceStep >= steps[sequence])
			return true;

		Sensor s = robotContext.getSensor();

		int leftOnGround = 0;
		if (s.getForce(LFsrFL) > 1.5f)
			leftOnGround++;
		if (s.getForce(LFsrFR) > 1.5f)
			leftOnGround++;
		if (s.getForce(LFsrBL) > 1.5f)
			leftOnGround++;
		if (s.getForce(LFsrBR) > 1.5f)
			leftOnGround++;

		int rightOnGround = 0;
		if (s.getForce(RFsrFL) > 1.5f)
			rightOnGround++;
		if (s.getForce(RFsrFR) > 1.5f)
			rightOnGround++;
		if (s.getForce(RFsrBL) > 1.5f)
			rightOnGround++;
		if (s.getForce(RFsrBR) > 1.5f)
			rightOnGround++;

		if (rightOnGround >= 2 && leftOnGround >= 2) {
			// 片足で二点ずつ接地していれば停止できる
			log.debug("touchSensor=true");
			return true;
		}

		if (rightOnGround == 0 && leftOnGround == 0) {
			log.debug("no grounded.");
			return true;
		}
		log.trace("can't stop. l:" + leftOnGround + " r:" + rightOnGround);
		return false;
	}

	@Override
	public void init(RobotContext context) {
		robotContext = context;
		super.init(context);
	}

	public ForwardMotion(float[] frames, int[] steps) {
		super(frames, steps);
	}

	@Override
	public void stepNextFrame(Sensor sensor, Effector effector) {
		if (currentStep == 0) {
			float[] current = sensor.getJointAngles();
			System.arraycopy(current, 0, ip, 0, ip.length);
			sequence = 0;
			sequenceStep = 0;
			interpolateFrame();
		} else if (sequenceStep >= steps[sequence]) {
			// 切り替え時
			nextSequence();
			interpolateFrame();
		}
		currentStep++;
		sequenceStep++;
		for (int j = 0; j < ip.length; j++) {
			ip[j] += dp[j];
		}

		if (Math.random() > 0.55 && hasNextStep()) {
			if (sequenceStep >= steps[sequence]) {
				nextSequence();
				interpolateFrame();
			}
			sequenceStep++;
		}
		for (Joint j : Joint.values())
			effector.setJoint(j, ip[j.ordinal()]);
	}

	private void nextSequence() {
		assert sequenceStep >= steps[sequence];
		if (sequenceStep < steps[sequence])
			return;
		if (sequence == 23 || sequence == 6) {
			if (stopRequested)
				sequence = 24;
			else
				sequence = 7;
		} else {
			sequence++;
		}
		sequenceStep = 0;
	}

	@Override
	public void requestStop() {
		stopRequested = true;
	}

	@Override
	public void continueMotion() {
		stopRequested = false;
	}

	@Override
	public boolean hasNextStep() {
		return sequence < 30 || sequenceStep < steps[sequence];
	}

	@Override
	public void start(MotionParam param) {
		stopRequested = false;
		super.start(param);
	}
}
