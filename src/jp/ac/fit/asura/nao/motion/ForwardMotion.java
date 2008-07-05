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
import jp.ac.fit.asura.nao.motion.MotionFactory.Compatible.CompatibleMotion;

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

		if (s.getForce(LFsrBL) >= 0 && s.getForce(LFsrBR) >= 0
				&& s.getForce(LFsrFL) >= 0 && s.getForce(LFsrFR) >= 0
				&& s.getForce(RFsrBL) >= 0 && s.getForce(RFsrBR) >= 0
				&& s.getForce(RFsrFL) >= 0 && s.getForce(RFsrFR) >= 0) {
			// ApproachTaskでこれがtrueのときcutMotion
			log.debug("touchSensor=true");
			return true;
		} else {
			return false;
		}
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
