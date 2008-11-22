package jp.ac.fit.asura.nao.motion;

import static jp.ac.fit.asura.nao.PressureSensor.LSoleBL;
import static jp.ac.fit.asura.nao.PressureSensor.LSoleBR;
import static jp.ac.fit.asura.nao.PressureSensor.LSoleFL;
import static jp.ac.fit.asura.nao.PressureSensor.LSoleFR;
import static jp.ac.fit.asura.nao.PressureSensor.RSoleBL;
import static jp.ac.fit.asura.nao.PressureSensor.RSoleBR;
import static jp.ac.fit.asura.nao.PressureSensor.RSoleFL;
import static jp.ac.fit.asura.nao.PressureSensor.RSoleFR;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.motion.MotionFactory.Compatible.CompatibleMotion;

import org.apache.log4j.Logger;

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
		if (s.getForce(LSoleFL) > 15)
			leftOnGround++;
		if (s.getForce(LSoleFR) > 15)
			leftOnGround++;
		if (s.getForce(LSoleBL) > 15)
			leftOnGround++;
		if (s.getForce(LSoleBR) > 15)
			leftOnGround++;

		int rightOnGround = 0;
		if (s.getForce(RSoleFL) > 15)
			rightOnGround++;
		if (s.getForce(RSoleFR) > 15)
			rightOnGround++;
		if (s.getForce(RSoleBL) > 15)
			rightOnGround++;
		if (s.getForce(RSoleBR) > 15)
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

	public ForwardMotion(float[][] frames, int[] steps) {
		super(frames, steps);
	}

	public float[] stepNextFrame(float[] current) {
		if (currentStep == 0) {
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

		return ip;
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

	public void requestStop() {
		stopRequested = true;
	}

	public void continueMotion() {
		stopRequested = false;
	}

	public boolean hasNextStep() {
		return sequence < 30 || sequenceStep < steps[sequence];
	}

	public void start() {
		stopRequested = false;
		super.start();
	}
}
