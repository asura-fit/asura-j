package jp.ac.fit.asura.nao.motion;

import static jp.ac.fit.asura.nao.TouchSensor.LFsrBL;
import static jp.ac.fit.asura.nao.TouchSensor.LFsrBR;
import static jp.ac.fit.asura.nao.TouchSensor.LFsrFL;
import static jp.ac.fit.asura.nao.TouchSensor.LFsrFR;
import static jp.ac.fit.asura.nao.TouchSensor.RFsrBL;
import static jp.ac.fit.asura.nao.TouchSensor.RFsrBR;
import static jp.ac.fit.asura.nao.TouchSensor.RFsrFL;
import static jp.ac.fit.asura.nao.TouchSensor.RFsrFR;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.motion.MotionFactory.Compatible;
import jp.ac.fit.asura.nao.motion.MotionFactory.Compatible.CompatibleMotion;
import jp.ac.fit.asura.nao.motion.MotionFactory.Liner.LinerMotion;

import org.apache.log4j.Logger;

public class ForwardMotion extends CompatibleMotion {
	private Logger log = Logger.getLogger(ForwardMotion.class);
	private RobotContext robotContext;

	public boolean canStop() {
		if (currentStep == totalFrames || currentStep <= 0) {
			// ストップするときは、フレームが終わるか、モーションが始まってないとき
			return true;
		}
		Sensor s = robotContext.getSensor();

		int leftOnGround = 0;
		if (s.getForce(LFsrFL) > 10)
			leftOnGround++;
		if (s.getForce(LFsrFR) > 10)
			leftOnGround++;
		if (s.getForce(LFsrBL) > 10)
			leftOnGround++;
		if (s.getForce(LFsrBR) > 10)
			leftOnGround++;

		int rightOnGround = 0;
		if (s.getForce(RFsrFL) > 10)
			rightOnGround++;
		if (s.getForce(RFsrFR) > 10)
			rightOnGround++;
		if (s.getForce(RFsrBL) > 10)
			rightOnGround++;
		if (s.getForce(RFsrBR) > 10)
			rightOnGround++;

		if (rightOnGround >= 2 && leftOnGround >= 2) {
			// 片足で二点ずつ接地していれば停止できる
			log.debug("touchSensor=true");
			return true;
		}
		
		if(rightOnGround == 0 && leftOnGround == 0){
			log.debug("no grounded.");
			return true;
		}
		log.debug("can't stop. l:" + leftOnGround + " r:" + rightOnGround);
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
}
