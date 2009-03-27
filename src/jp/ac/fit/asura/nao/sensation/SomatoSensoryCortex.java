/*
 * 作成日: 2008/07/01
 */
package jp.ac.fit.asura.nao.sensation;

import static jp.ac.fit.asura.nao.PressureSensor.LFsrBL;
import static jp.ac.fit.asura.nao.PressureSensor.LFsrBR;
import static jp.ac.fit.asura.nao.PressureSensor.LFsrFL;
import static jp.ac.fit.asura.nao.PressureSensor.LFsrFR;
import static jp.ac.fit.asura.nao.PressureSensor.RFsrBL;
import static jp.ac.fit.asura.nao.PressureSensor.RFsrBR;
import static jp.ac.fit.asura.nao.PressureSensor.RFsrFL;
import static jp.ac.fit.asura.nao.PressureSensor.RFsrFR;

import java.awt.Point;

import javax.vecmath.Matrix3f;
import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.event.MotionEventListener;
import jp.ac.fit.asura.nao.event.RobotFrameEventListener;
import jp.ac.fit.asura.nao.event.VisualEventListener;
import jp.ac.fit.asura.nao.misc.Coordinates;
import jp.ac.fit.asura.nao.misc.Kinematics;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.physical.Robot;
import jp.ac.fit.asura.nao.physical.RobotFrame;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualObjects;
import jp.ac.fit.asura.nao.vision.perception.BallVisualObject;

import org.apache.log4j.Logger;

;
/**
 * 体性感覚野.
 *
 * 姿勢などのセンサー情報を抽象化します.
 *
 * @author sey
 *
 * @version $Id: SomatoSensoryCortex.java 721 2009-02-18 03:40:44Z sey $
 *
 */
public class SomatoSensoryCortex implements RobotLifecycle,
		MotionEventListener, VisualEventListener {
	private static final Logger log = Logger.getLogger(SomatoSensoryCortex.class);

	private RobotFrameEventListener listeners;

	private Sensor sensor;

	private Vector4f ball;

	private Robot robot;
	private Robot nextRobot = null;

	private SomaticContext context;

	private boolean leftOnGround;
	private boolean rightOnGround;

	private int confidence;

	public void init(RobotContext rctx) {
		sensor = rctx.getSensor();
		rctx.getVision().addEventListener(this);
		rctx.getMotor().addEventListener(this);

		ball = new Vector4f();
		robot = new Robot(new RobotFrame(Frames.Body));
		context = new SomaticContext(robot);
	}

	public void start() {
	}

	public void step() {
		if (nextRobot != null) {
			robot = nextRobot;
			context = new SomaticContext(robot);
			nextRobot = null;
		}

		for (FrameState joint : context.getFrames()) {
			if (joint.getId().isJoint()) {
				joint.updateValue(sensor.getJoint(joint.getId().toJoint()));
				joint.updateForce(sensor.getForce(joint.getId().toJoint()));
			}
		}
		Kinematics.calculateForward(context);
		Kinematics.calculateCenterOfMass(context);

		leftOnGround = checkLeftOnGround();
		rightOnGround = checkRightOnGround();
		confidence = 0;
		if (leftOnGround)
			confidence += 500;
		if (rightOnGround)
			confidence += 500;
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
		BallVisualObject vo = (BallVisualObject) context
				.get(VisualObjects.Ball);
		if (vo.confidence > 0 && vo.distanceUsable) {
			ball.set(vo.robotPosition);
			ball.w = ball.w * 0.3f + 0.7f * vo.confidence;
		} else {
			ball.w *= 0.95;
		}
	}

	public void updateRobot(Robot robot) {
		nextRobot = robot;
	}

	private boolean checkLeftOnGround() {
		int count = 0;
		if (sensor.getForce(LFsrFL) > 30)
			count++;
		if (sensor.getForce(LFsrFR) > 30)
			count++;
		if (sensor.getForce(LFsrBL) > 30)
			count++;
		if (sensor.getForce(LFsrBR) > 30)
			count++;
		boolean onGround = count >= 2;
		log.debug("left on ground?" + Boolean.toString(onGround));
		return onGround;
	}

	private boolean checkRightOnGround() {
		int count = 0;
		if (sensor.getForce(RFsrFL) > 30)
			count++;
		if (sensor.getForce(RFsrFR) > 30)
			count++;
		if (sensor.getForce(RFsrBL) > 30)
			count++;
		if (sensor.getForce(RFsrBR) > 30)
			count++;
		boolean onGround = count >= 2;
		log.debug("right on ground?" + Boolean.toString(onGround));
		return onGround;
	}

	@Deprecated
	/*
	 * 現在の体勢から，カメラ座標系での位置を接地座標系に変換します.
	 *
	 * 両足が接地していることが前提.
	 *
	 * @return 接地座標系でのカメラの位置(mm)
	 */
	public Vector3f getCameraPosition(Vector3f camera) {
		Vector3f body = new Vector3f(camera);
		Coordinates.camera2bodyCoord(getContext(), body);

		Vector3f lSole = new Vector3f(body);

		if (isLeftOnGround()) {
			Coordinates.body2lSoleCoord(getContext(), lSole);
			lSole.x -= (int) (robot.get(Frames.LHipYawPitch).getTranslation().x);
		}

		Vector3f rSole = new Vector3f(body);
		if (isRightOnGround()) {
			Coordinates.body2rSoleCoord(getContext(), rSole);
			rSole.x -= (int) (robot.get(Frames.RHipYawPitch).getTranslation().x);
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

	public void getLeftCOP(Point2f p ) {
		int[] forces = new int[4];

		forces[0] = sensor.getForce(LFsrFL);
		forces[1] = sensor.getForce(LFsrFR);
		forces[2] = sensor.getForce(LFsrBL);
		forces[3] = sensor.getForce(LFsrBR);

		int force = 0;
		p.x = 0;
		p.y = 0;

		p.x += forces[0] * context.get(Frames.LSoleFL).getBodyPosition().x;
		p.y += forces[0] * context.get(Frames.LSoleFL).getBodyPosition().z;
		force += forces[0];

		p.x += forces[1] * context.get(Frames.LSoleFR).getBodyPosition().x;
		p.y += forces[1] * context.get(Frames.LSoleFR).getBodyPosition().z;
		force += forces[1];

		p.x += forces[2] * context.get(Frames.LSoleBL).getBodyPosition().x;
		p.y += forces[2] * context.get(Frames.LSoleBL).getBodyPosition().z;
		force += forces[2];

		p.x += forces[3] * context.get(Frames.LSoleBR).getBodyPosition().x;
		p.y += forces[3] * context.get(Frames.LSoleBR).getBodyPosition().z;
		force += forces[3];

		if (force == 0) {
			// not on ground
			p.x = p.y = 0;
		} else {
			p.x /= force;
			p.y /= force;
		}
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

		p.x += forces[0] * robot.get(Frames.LSoleFL).getTranslation().x;
		p.y += forces[0] * robot.get(Frames.LSoleFL).getTranslation().z;
		force += forces[0];

		p.x += forces[1] * robot.get(Frames.LSoleFR).getTranslation().x;
		p.y += forces[1] * robot.get(Frames.LSoleFR).getTranslation().z;
		force += forces[1];

		p.x += forces[2] * robot.get(Frames.LSoleBL).getTranslation().x;
		p.y += forces[2] * robot.get(Frames.LSoleBL).getTranslation().z;
		force += forces[2];

		p.x += forces[3] * robot.get(Frames.LSoleBR).getTranslation().x;
		p.y += forces[3] * robot.get(Frames.LSoleBR).getTranslation().z;
		force += forces[3];

		if (force == 0) {
			// not on ground
			p.x = p.y = 0;
		} else {
			p.x /= force;
			p.y /= force;
		}
	}

	public int getLeftPressure() {
		int force = 0;
		force += sensor.getForce(LFsrFL);
		force += sensor.getForce(LFsrFR);
		force += sensor.getForce(LFsrBL);
		force += sensor.getForce(LFsrBR);
		return force;
	}

	public void getRightCOP(Point2f p) {
		int[] forces = new int[4];

		forces[0] = sensor.getForce(RFsrFL);
		forces[1] = sensor.getForce(RFsrFR);
		forces[2] = sensor.getForce(RFsrBL);
		forces[3] = sensor.getForce(RFsrBR);

		int force = 0;
		p.x = 0;
		p.y = 0;

		p.x += forces[0] * context.get(Frames.RSoleFL).getBodyPosition().x;
		p.y += forces[0] * context.get(Frames.RSoleFL).getBodyPosition().z;
		force += forces[0];

		p.x += forces[1] * context.get(Frames.RSoleFR).getBodyPosition().x;
		p.y += forces[1] * context.get(Frames.RSoleFR).getBodyPosition().z;
		force += forces[1];

		p.x += forces[2] * context.get(Frames.RSoleBL).getBodyPosition().x;
		p.y += forces[2] * context.get(Frames.RSoleBL).getBodyPosition().z;
		force += forces[2];

		p.x += forces[3] * context.get(Frames.RSoleBR).getBodyPosition().x;
		p.y += forces[3] * context.get(Frames.RSoleBR).getBodyPosition().z;
		force += forces[3];

		if (force == 0) {
			// not on ground
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

		p.x += forces[0] * robot.get(Frames.RSoleFL).getTranslation().x;
		p.y += forces[0] * robot.get(Frames.RSoleFL).getTranslation().z;
		force += forces[0];

		p.x += forces[1] * robot.get(Frames.RSoleFR).getTranslation().x;
		p.y += forces[1] * robot.get(Frames.RSoleFR).getTranslation().z;
		force += forces[1];

		p.x += forces[2] * robot.get(Frames.RSoleBL).getTranslation().x;
		p.y += forces[2] * robot.get(Frames.RSoleBL).getTranslation().z;
		force += forces[2];

		p.x += forces[3] * robot.get(Frames.RSoleBR).getTranslation().x;
		p.y += forces[3] * robot.get(Frames.RSoleBR).getTranslation().z;
		force += forces[3];

		if (force == 0) {
			// not on ground
			p.x = p.y = 0;
		} else {
			p.x /= force;
			p.y /= force;
		}
	}

	public int getRightPressure() {
		int force = 0;
		force += sensor.getForce(RFsrFL);
		force += sensor.getForce(RFsrFR);
		force += sensor.getForce(RFsrBL);
		force += sensor.getForce(RFsrBR);
		return force;
	}

	public void getCOP(Point2f p){
		int[] forces = new int[8];

		forces[0] = sensor.getForce(LFsrFL);
		forces[1] = sensor.getForce(LFsrFR);
		forces[2] = sensor.getForce(LFsrBL);
		forces[3] = sensor.getForce(LFsrBR);
		forces[4] = sensor.getForce(RFsrFL);
		forces[5] = sensor.getForce(RFsrFR);
		forces[6] = sensor.getForce(RFsrBL);
		forces[7] = sensor.getForce(RFsrBR);

		int force = 0;
		p.x = 0;
		p.y = 0;

		p.x += forces[0] * robot.get(Frames.LSoleFL).getTranslation().x;
		p.y += forces[0] * robot.get(Frames.LSoleFL).getTranslation().z;
		force += forces[0];

		p.x += forces[1] * robot.get(Frames.LSoleFR).getTranslation().x;
		p.y += forces[1] * robot.get(Frames.LSoleFR).getTranslation().z;
		force += forces[1];

		p.x += forces[2] * robot.get(Frames.LSoleBL).getTranslation().x;
		p.y += forces[2] * robot.get(Frames.LSoleBL).getTranslation().z;
		force += forces[2];

		p.x += forces[3] * robot.get(Frames.LSoleBR).getTranslation().x;
		p.y += forces[3] * robot.get(Frames.LSoleBR).getTranslation().z;
		force += forces[3];

		p.x += forces[4] * robot.get(Frames.RSoleFL).getTranslation().x;
		p.y += forces[4] * robot.get(Frames.RSoleFL).getTranslation().z;
		force += forces[4];

		p.x += forces[5] * robot.get(Frames.RSoleFR).getTranslation().x;
		p.y += forces[5] * robot.get(Frames.RSoleFR).getTranslation().z;
		force += forces[5];

		p.x += forces[6] * robot.get(Frames.RSoleBL).getTranslation().x;
		p.y += forces[6] * robot.get(Frames.RSoleBL).getTranslation().z;
		force += forces[6];

		p.x += forces[7] * robot.get(Frames.RSoleBR).getTranslation().x;
		p.y += forces[7] * robot.get(Frames.RSoleBR).getTranslation().z;
		force += forces[7];

		if (force == 0) {
			// not on ground
			p.x = p.y = 0;
		} else {
			p.x /= force;
			p.y /= force;
		}
	}

	public void body2robotCoord(Vector3f src, Vector3f dest) {
		Matrix3f rot = new Matrix3f();
		calculateBodyRotation(rot);
		rot.transpose();
		rot.transform(src, dest);
		dest.y += calculateBodyHeight();
	}

	public void robot2bodyCoord(Vector3f src, Vector3f dest) {
		Matrix3f rot = new Matrix3f();
		calculateBodyRotation(rot);
		rot.transform(src, dest);
		dest.y -= calculateBodyHeight();
	}

	public void calculateBodyRotation(Matrix3f mat) {
		// FIXME 未実装
		if (isLeftOnGround())
			mat.set(context.get(Frames.LSole).getBodyRotation());
		else if (isRightOnGround())
			mat.set(context.get(Frames.RSole).getBodyRotation());
		else
			mat.setIdentity();
	}

	public float calculateBodyHeight() {
		// FIXME 未実装
		if (isLeftOnGround())
			return -context.get(Frames.LSole).getBodyPosition().y;
		if (isRightOnGround())
			return -context.get(Frames.RSole).getBodyPosition().y;
		return 320;
	}

	public SomaticContext getContext() {
		return context;
	}

	/**
	 * 現在の姿勢情報がどれくらい信頼できるのかを返します.
	 *
	 * @return the confidence
	 */
	public int getConfidence() {
		return confidence;
	}

	/**
	 * @return the leftOnGround
	 */
	public boolean isLeftOnGround() {
		return leftOnGround;
	}

	/**
	 * @return the rightOnGround
	 */
	public boolean isRightOnGround() {
		return rightOnGround;
	}
}
