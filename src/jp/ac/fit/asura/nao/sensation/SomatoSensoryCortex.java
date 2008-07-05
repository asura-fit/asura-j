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
import static jp.ac.fit.asura.nao.misc.Coordinates.body2lSoleCoord;
import static jp.ac.fit.asura.nao.misc.Coordinates.body2rSoleCoord;
import static jp.ac.fit.asura.nao.misc.Coordinates.camera2bodyCoord;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.event.MotionEventListener;
import jp.ac.fit.asura.nao.event.VisualEventListener;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualObjects;
import jp.ac.fit.asura.nao.vision.VisualObjects.Properties;
import jp.ac.fit.asura.nao.vision.objects.VisualObject;

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
		if (vo.getInt(Properties.Confidence) > 0) {
			ball.set(vo.get(Vector3f.class, Properties.Position));
			ball.w = ball.w * 0.3f + 0.7f * vo.getInt(Properties.Confidence);
		} else {
			ball.w *= 0.95;
		}
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
		body2lSoleCoord(lSole, -sensor.getJoint(LHipYawPitch), -sensor
				.getJoint(LHipRoll), -sensor.getJoint(LHipPitch), -sensor
				.getJoint(LKneePitch), -sensor.getJoint(LAnklePitch), -sensor
				.getJoint(LAnkleRoll));
		
		Vector3f rSole = new Vector3f(body);
		body2rSoleCoord(rSole, -sensor.getJoint(RHipYawPitch), -sensor
				.getJoint(RHipRoll), -sensor.getJoint(RHipPitch), -sensor
				.getJoint(RKneePitch), -sensor.getJoint(RAnklePitch), -sensor
				.getJoint(RAnkleRoll));

		lSole.add(rSole);
		lSole.scale(0.5f);
		return lSole;
	}

	public Vector4f getBallPosition() {
		return ball;
	}
}
