/*
 * 作成日: 2008/07/01
 */
package jp.ac.fit.asura.nao.sensation;

import static jp.ac.fit.asura.nao.Joint.HeadPitch;
import static jp.ac.fit.asura.nao.Joint.HeadYaw;
import static jp.ac.fit.asura.nao.Joint.LAnklePitch;
import static jp.ac.fit.asura.nao.Joint.LAnkleRoll;
import static jp.ac.fit.asura.nao.Joint.LHipPitch;
import static jp.ac.fit.asura.nao.Joint.LHipRoll;
import static jp.ac.fit.asura.nao.Joint.LHipYawPitch;
import static jp.ac.fit.asura.nao.Joint.LKneePitch;
import static jp.ac.fit.asura.nao.Joint.RAnklePitch;
import static jp.ac.fit.asura.nao.Joint.RAnkleRoll;
import static jp.ac.fit.asura.nao.Joint.RHipPitch;
import static jp.ac.fit.asura.nao.Joint.RHipRoll;
import static jp.ac.fit.asura.nao.Joint.RHipYawPitch;
import static jp.ac.fit.asura.nao.Joint.RKneePitch;
import static jp.ac.fit.asura.nao.TouchSensor.LFsrBL;
import static jp.ac.fit.asura.nao.TouchSensor.LFsrBR;
import static jp.ac.fit.asura.nao.TouchSensor.LFsrFL;
import static jp.ac.fit.asura.nao.TouchSensor.LFsrFR;
import static jp.ac.fit.asura.nao.TouchSensor.RFsrBL;
import static jp.ac.fit.asura.nao.TouchSensor.RFsrBR;
import static jp.ac.fit.asura.nao.TouchSensor.RFsrFL;
import static jp.ac.fit.asura.nao.TouchSensor.RFsrFR;
import static jp.ac.fit.asura.nao.misc.Coordinates.body2lSoleCoord;
import static jp.ac.fit.asura.nao.misc.Coordinates.body2rSoleCoord;
import static jp.ac.fit.asura.nao.misc.Coordinates.camera2bodyCoord;

import java.awt.Point;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.event.MotionEventListener;
import jp.ac.fit.asura.nao.event.VisualEventListener;
import jp.ac.fit.asura.nao.misc.PhysicalConstants.Nao;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualObjects;
import jp.ac.fit.asura.nao.vision.VisualObjects.Properties;
import jp.ac.fit.asura.nao.vision.objects.VisualObject;

import org.apache.log4j.Logger;

;
/**
 * 体性感覚野.
 * 
 * 姿勢などのセンサー情報を抽象化します.
 * 
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public class SomatoSensoryCortex implements RobotLifecycle,
		MotionEventListener, VisualEventListener {
	private Logger log = Logger.getLogger(SomatoSensoryCortex.class);

	private Sensor sensor;

	private Vector4f ball;

	private Boolean leftOnGround;
	private Boolean rightOnGround;

	public void init(RobotContext rctx) {
		sensor = rctx.getSensor();
		rctx.getVision().addEventListener(this);
		rctx.getMotor().addEventListener(this);

		ball = new Vector4f();
	}

	public void start() {
	}

	public void step() {
		leftOnGround = null;
		rightOnGround = null;
	}

	public void stop() {
	}

	public void startMotion(Motion motion) {
	}

	public void stopMotion(Motion motion) {
	}

	public void updateOdometry(int forward, int left, float turnCCW) {
		float dx = forward + left + 10 * turnCCW;
		if (dx > 10) {
			ball.w *= 0.8;
		}
	}

	public void updatePosture() {
	}

	public void updateVision(VisualContext context) {
		VisualObject vo = context.objects.get(VisualObjects.Ball);
		if (vo.getInt(Properties.Confidence) > 0
				&& vo.getBoolean(Properties.DistanceUsable)) {
			ball.set(vo.get(Vector3f.class, Properties.Position));
			ball.w = ball.w * 0.3f + 0.7f * vo.getInt(Properties.Confidence);
		} else {
			ball.w *= 0.95;
		}
	}

	public boolean isLeftOnGround() {
		if (leftOnGround != null)
			return leftOnGround.booleanValue();

		int count = 0;
		if (sensor.getForce(LFsrFL) > 30)
			count++;
		if (sensor.getForce(LFsrFR) > 30)
			count++;
		if (sensor.getForce(LFsrBL) > 30)
			count++;
		if (sensor.getForce(LFsrBR) > 30)
			count++;
		leftOnGround = Boolean.valueOf(count >= 2);
		log.debug("left on ground.");
		return leftOnGround;
	}

	public boolean isRightOnGround() {
		if (rightOnGround != null)
			return rightOnGround.booleanValue();

		int count = 0;
		if (sensor.getForce(RFsrFL) > 30)
			count++;
		if (sensor.getForce(RFsrFR) > 30)
			count++;
		if (sensor.getForce(RFsrBL) > 30)
			count++;
		if (sensor.getForce(RFsrBR) > 30)
			count++;
		rightOnGround = Boolean.valueOf(count >= 2);
		log.debug("right on ground.");
		return rightOnGround;
	}

	/**
	 * 現在の体勢から，カメラ座標系での位置を接地座標系に変換します.
	 * 
	 * 両足が接地していることが前提.
	 * 
	 * @return 接地座標系でのカメラの位置(mm)
	 */
	public Vector3f getCameraPosition(Vector3f camera) {
		Vector3f body = new Vector3f(camera);
		camera2bodyCoord(body, -sensor.getJoint(HeadPitch), -sensor
				.getJoint(HeadYaw));

		Vector3f lSole = new Vector3f(body);

		if (isLeftOnGround()) {
			body2lSoleCoord(lSole, -sensor.getJoint(LHipYawPitch), -sensor
					.getJoint(LHipRoll), -sensor.getJoint(LHipPitch), -sensor
					.getJoint(LKneePitch), -sensor.getJoint(LAnklePitch),
					-sensor.getJoint(LAnkleRoll));
			lSole.x += (int) (Nao.lHipYawPitch2body.translate.x);
		}

		Vector3f rSole = new Vector3f(body);
		if (isRightOnGround()) {
			body2rSoleCoord(rSole, -sensor.getJoint(RHipYawPitch), -sensor
					.getJoint(RHipRoll), -sensor.getJoint(RHipPitch), -sensor
					.getJoint(RKneePitch), -sensor.getJoint(RAnklePitch),
					-sensor.getJoint(RAnkleRoll));
			rSole.x += (int) (Nao.rHipYawPitch2body.translate.x);
		}

		if (isLeftOnGround() && isRightOnGround()) {
			lSole.add(rSole);
			lSole.scale(0.5f);
			return lSole;
		} else if (isLeftOnGround()) {
			return lSole;
		} else if (isRightOnGround()) {
			return rSole;
		}
		return null;
	}

	public Vector4f getBallPosition() {
		return ball;
	}

	public void getLeftCOP(Point p) {
		int[] forces = new int[4];

		forces[0] = sensor.getForce(LFsrFL);
		forces[1] = sensor.getForce(LFsrFR);
		forces[2] = sensor.getForce(LFsrBL);
		forces[3] = sensor.getForce(LFsrBR);

		int force = 0;
		p.x = 0;
		p.y = 0;

		p.x -= forces[0] * Nao.lFL2Sole.translate.x;
		p.y -= forces[0] * Nao.lFL2Sole.translate.z;
		force += forces[0];

		p.x -= forces[1] * Nao.lFR2Sole.translate.x;
		p.y -= forces[1] * Nao.lFR2Sole.translate.z;
		force += forces[1];

		p.x -= forces[2] * Nao.lBL2Sole.translate.x;
		p.y -= forces[2] * Nao.lBL2Sole.translate.z;
		force += forces[2];

		p.x -= forces[3] * Nao.lBR2Sole.translate.x;
		p.y -= forces[3] * Nao.lBR2Sole.translate.z;
		force += forces[3];

		if (force == 0) {
			p.x = p.y = 0;
		} else {
			p.x /= force;
			p.y /= force;
		}
	}

	public void getRightCOP(Point p) {
		int[] forces = new int[4];

		forces[0] = sensor.getForce(RFsrFL);
		forces[1] = sensor.getForce(RFsrFR);
		forces[2] = sensor.getForce(RFsrBL);
		forces[3] = sensor.getForce(RFsrBR);

		int force = 0;
		p.x = 0;
		p.y = 0;

		p.x -= forces[0] * Nao.rFL2Sole.translate.x;
		p.y -= forces[0] * Nao.rFL2Sole.translate.z;
		force += forces[0];

		p.x -= forces[1] * Nao.rFR2Sole.translate.x;
		p.y -= forces[1] * Nao.rFR2Sole.translate.z;
		force += forces[1];

		p.x -= forces[2] * Nao.rBL2Sole.translate.x;
		p.y -= forces[2] * Nao.rBL2Sole.translate.z;
		force += forces[2];

		p.x -= forces[3] * Nao.rBR2Sole.translate.x;
		p.y -= forces[3] * Nao.rBR2Sole.translate.z;
		force += forces[3];

		if (force == 0) {
			p.x = p.y = 0;
		} else {
			p.x /= force;
			p.y /= force;
		}
	}
}
