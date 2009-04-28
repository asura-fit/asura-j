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

import jp.ac.fit.asura.nao.MotionCycle;
import jp.ac.fit.asura.nao.MotionFrameContext;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.SensorContext;
import jp.ac.fit.asura.nao.Camera.CameraID;
import jp.ac.fit.asura.nao.Sensor.Function;
import jp.ac.fit.asura.nao.event.MotionEventListener;
import jp.ac.fit.asura.nao.event.VisualEventListener;
import jp.ac.fit.asura.nao.misc.Kinematics;
import jp.ac.fit.asura.nao.misc.MatrixUtils;
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
public class SomatoSensoryCortex implements MotionCycle, MotionEventListener,
		VisualEventListener {
	private static final Logger log = Logger
			.getLogger(SomatoSensoryCortex.class);

	private Vector4f ball;

	private Robot robot;

	private boolean useInertial;

	/**
	 *
	 */
	public SomatoSensoryCortex() {
	}

	@Override
	public void init(RobotContext rctx) {
		rctx.getVision().addEventListener(this);
		rctx.getMotor().addEventListener(this);

		ball = new Vector4f();
		robot = new Robot(new RobotFrame(Frames.Body));

		useInertial = rctx.getSensor().isSupported(Function.INERTIAL);
	}

	@Override
	public void start() {
	}

	@Override
	public void step(MotionFrameContext frameContext) {
		SensorContext sensor = frameContext.getSensorContext();
		SomaticContext context = frameContext.getSomaticContext();
		if (context == null) {
			context = new SomaticContext(robot);
			frameContext.setSomaticContext(context);
		}
		if (context.getRobot() != robot) {
			context = new SomaticContext(robot);
			frameContext.setSomaticContext(context);
		}

		for (FrameState joint : context.getFrames()) {
			if (joint.getId().isJoint()) {
				joint.updateValue(sensor.getJoint(joint.getId().toJoint()));
			}
		}

		// quick hack for bottom camera
		if (frameContext.getRobotContext().getCamera().getSelectedId() == CameraID.TOP) {
			context.get(Frames.CameraSelect).updateValue(0);
		} else {
			// FIXME この処理ではCameraSelect仮想関節を回転させるだけなので、カメラの位置に53.90 -
			// sqrt((67.90-23.81)*(48.8)) = 7.514mmの誤差がでる.
			context.get(Frames.CameraSelect).updateValue(0.6981f);
		}
		Kinematics.calculateForward(context);
		Kinematics.calculateCenterOfMass(context);

		context.setLeftOnGround(checkLeftOnGround(frameContext
				.getSensorContext()));
		context.setRightOnGround(checkRightOnGround(frameContext
				.getSensorContext()));
		int cf = 0;
		if (context.isLeftOnGround())
			cf += 500;
		if (context.isRightOnGround())
			cf += 500;
		context.setConfidence(cf);
		log.trace("Left pressure:"
				+ getLeftPressure(frameContext.getSensorContext()));
		log.trace("Right pressure:"
				+ getRightPressure(frameContext.getSensorContext()));

		context.setBodyHeight(calculateBodyHeight(context));
		if (useInertial) {
			Matrix3f posture = context.getBodyPosture();
			Vector3f tmp = new Vector3f();
			tmp.x = frameContext.getSensorContext().getInertialZ();
			tmp.y = frameContext.getSensorContext().getInertialX();
			MatrixUtils.rpy2rot(tmp, posture);
		} else {
			Matrix3f posture = context.getBodyPosture();
			calculateBodyRotation(context, posture);
		}
	}

	@Override
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
		this.robot = robot;
	}

	private boolean checkLeftOnGround(SensorContext sensor) {
		int count = 0;
		if (sensor.getForce(LFsrFL) > 2.0f)
			count++;
		if (sensor.getForce(LFsrFR) > 2.0f)
			count++;
		if (sensor.getForce(LFsrBL) > 2.0f)
			count++;
		if (sensor.getForce(LFsrBR) > 2.0f)
			count++;
		boolean onGround = count >= 2;
		log.debug("left on ground?" + Boolean.toString(onGround));
		return onGround;
	}

	private boolean checkRightOnGround(SensorContext sensor) {
		int count = 0;
		if (sensor.getForce(RFsrFL) > 2.0f)
			count++;
		if (sensor.getForce(RFsrFR) > 2.0f)
			count++;
		if (sensor.getForce(RFsrBL) > 2.0f)
			count++;
		if (sensor.getForce(RFsrBR) > 2.0f)
			count++;
		boolean onGround = count >= 2;
		log.debug("right on ground?" + Boolean.toString(onGround));
		return onGround;
	}

	public Vector4f getBallPosition() {
		return ball;
	}

	public void getLeftCOP(MotionFrameContext frameContext, Point2f p) {
		SensorContext sensor = frameContext.getSensorContext();
		SomaticContext context = frameContext.getSomaticContext();
		float[] forces = new float[4];

		forces[0] = sensor.getForce(LFsrFL);
		forces[1] = sensor.getForce(LFsrFR);
		forces[2] = sensor.getForce(LFsrBL);
		forces[3] = sensor.getForce(LFsrBR);

		float force = 0;
		p.x = 0;
		p.y = 0;

		p.x += forces[0] * context.get(Frames.LFsrFL).getBodyPosition().x;
		p.y += forces[0] * context.get(Frames.LFsrFL).getBodyPosition().z;
		force += forces[0];

		p.x += forces[1] * context.get(Frames.LFsrFR).getBodyPosition().x;
		p.y += forces[1] * context.get(Frames.LFsrFR).getBodyPosition().z;
		force += forces[1];

		p.x += forces[2] * context.get(Frames.LFsrBL).getBodyPosition().x;
		p.y += forces[2] * context.get(Frames.LFsrBL).getBodyPosition().z;
		force += forces[2];

		p.x += forces[3] * context.get(Frames.LFsrBR).getBodyPosition().x;
		p.y += forces[3] * context.get(Frames.LFsrBR).getBodyPosition().z;
		force += forces[3];

		if (force == 0) {
			// not on ground
			p.x = p.y = 0;
		} else {
			p.x /= force;
			p.y /= force;
		}
	}

	public void getLeftCOP(SensorContext sensor, Point p) {
		float[] forces = new float[4];

		forces[0] = sensor.getForce(LFsrFL);
		forces[1] = sensor.getForce(LFsrFR);
		forces[2] = sensor.getForce(LFsrBL);
		forces[3] = sensor.getForce(LFsrBR);

		float force = 0;
		p.x = 0;
		p.y = 0;

		p.x += forces[0] * robot.get(Frames.LFsrFL).getTranslation().x;
		p.y += forces[0] * robot.get(Frames.LFsrFL).getTranslation().z;
		force += forces[0];

		p.x += forces[1] * robot.get(Frames.LFsrFR).getTranslation().x;
		p.y += forces[1] * robot.get(Frames.LFsrFR).getTranslation().z;
		force += forces[1];

		p.x += forces[2] * robot.get(Frames.LFsrBL).getTranslation().x;
		p.y += forces[2] * robot.get(Frames.LFsrBL).getTranslation().z;
		force += forces[2];

		p.x += forces[3] * robot.get(Frames.LFsrBR).getTranslation().x;
		p.y += forces[3] * robot.get(Frames.LFsrBR).getTranslation().z;
		force += forces[3];

		if (force == 0) {
			// not on ground
			p.x = p.y = 0;
		} else {
			p.x /= force;
			p.y /= force;
		}
	}

	public float getLeftPressure(SensorContext sensor) {
		float force = 0;
		force += sensor.getForce(LFsrFL);
		force += sensor.getForce(LFsrFR);
		force += sensor.getForce(LFsrBL);
		force += sensor.getForce(LFsrBR);
		return force;
	}

	public void getRightCOP(MotionFrameContext frameContext, Point2f p) {
		SensorContext sensor = frameContext.getSensorContext();
		SomaticContext context = frameContext.getSomaticContext();
		float[] forces = new float[4];

		forces[0] = sensor.getForce(RFsrFL);
		forces[1] = sensor.getForce(RFsrFR);
		forces[2] = sensor.getForce(RFsrBL);
		forces[3] = sensor.getForce(RFsrBR);

		int force = 0;
		p.x = 0;
		p.y = 0;

		p.x += forces[0] * context.get(Frames.RFsrFL).getBodyPosition().x;
		p.y += forces[0] * context.get(Frames.RFsrFL).getBodyPosition().z;
		force += forces[0];

		p.x += forces[1] * context.get(Frames.RFsrFR).getBodyPosition().x;
		p.y += forces[1] * context.get(Frames.RFsrFR).getBodyPosition().z;
		force += forces[1];

		p.x += forces[2] * context.get(Frames.RFsrBL).getBodyPosition().x;
		p.y += forces[2] * context.get(Frames.RFsrBL).getBodyPosition().z;
		force += forces[2];

		p.x += forces[3] * context.get(Frames.RFsrBR).getBodyPosition().x;
		p.y += forces[3] * context.get(Frames.RFsrBR).getBodyPosition().z;
		force += forces[3];

		if (force == 0) {
			// not on ground
			p.x = p.y = 0;
		} else {
			p.x /= force;
			p.y /= force;
		}
	}

	public void getRightCOP(SensorContext sensor, Point p) {
		float[] forces = new float[4];

		forces[0] = sensor.getForce(RFsrFL);
		forces[1] = sensor.getForce(RFsrFR);
		forces[2] = sensor.getForce(RFsrBL);
		forces[3] = sensor.getForce(RFsrBR);

		float force = 0;
		p.x = 0;
		p.y = 0;

		p.x += forces[0] * robot.get(Frames.RFsrFL).getTranslation().x;
		p.y += forces[0] * robot.get(Frames.RFsrFL).getTranslation().z;
		force += forces[0];

		p.x += forces[1] * robot.get(Frames.RFsrFR).getTranslation().x;
		p.y += forces[1] * robot.get(Frames.RFsrFR).getTranslation().z;
		force += forces[1];

		p.x += forces[2] * robot.get(Frames.RFsrBL).getTranslation().x;
		p.y += forces[2] * robot.get(Frames.RFsrBL).getTranslation().z;
		force += forces[2];

		p.x += forces[3] * robot.get(Frames.RFsrBR).getTranslation().x;
		p.y += forces[3] * robot.get(Frames.RFsrBR).getTranslation().z;
		force += forces[3];

		if (force == 0) {
			// not on ground
			p.x = p.y = 0;
		} else {
			p.x /= force;
			p.y /= force;
		}
	}

	public float getRightPressure(SensorContext sensor) {
		float force = 0;
		force += sensor.getForce(RFsrFL);
		force += sensor.getForce(RFsrFR);
		force += sensor.getForce(RFsrBL);
		force += sensor.getForce(RFsrBR);
		return force;
	}

	public void getCOP(SensorContext sensor, Point2f p) {
		float[] forces = new float[8];

		forces[0] = sensor.getForce(LFsrFL);
		forces[1] = sensor.getForce(LFsrFR);
		forces[2] = sensor.getForce(LFsrBL);
		forces[3] = sensor.getForce(LFsrBR);
		forces[4] = sensor.getForce(RFsrFL);
		forces[5] = sensor.getForce(RFsrFR);
		forces[6] = sensor.getForce(RFsrBL);
		forces[7] = sensor.getForce(RFsrBR);

		float force = 0;
		p.x = 0;
		p.y = 0;

		p.x += forces[0] * robot.get(Frames.LFsrFL).getTranslation().x;
		p.y += forces[0] * robot.get(Frames.LFsrFL).getTranslation().z;
		force += forces[0];

		p.x += forces[1] * robot.get(Frames.LFsrFR).getTranslation().x;
		p.y += forces[1] * robot.get(Frames.LFsrFR).getTranslation().z;
		force += forces[1];

		p.x += forces[2] * robot.get(Frames.LFsrBL).getTranslation().x;
		p.y += forces[2] * robot.get(Frames.LFsrBL).getTranslation().z;
		force += forces[2];

		p.x += forces[3] * robot.get(Frames.LFsrBR).getTranslation().x;
		p.y += forces[3] * robot.get(Frames.LFsrBR).getTranslation().z;
		force += forces[3];

		p.x += forces[4] * robot.get(Frames.RFsrFL).getTranslation().x;
		p.y += forces[4] * robot.get(Frames.RFsrFL).getTranslation().z;
		force += forces[4];

		p.x += forces[5] * robot.get(Frames.RFsrFR).getTranslation().x;
		p.y += forces[5] * robot.get(Frames.RFsrFR).getTranslation().z;
		force += forces[5];

		p.x += forces[6] * robot.get(Frames.RFsrBL).getTranslation().x;
		p.y += forces[6] * robot.get(Frames.RFsrBL).getTranslation().z;
		force += forces[6];

		p.x += forces[7] * robot.get(Frames.RFsrBR).getTranslation().x;
		p.y += forces[7] * robot.get(Frames.RFsrBR).getTranslation().z;
		force += forces[7];

		if (force == 0) {
			// not on ground
			p.x = p.y = 0;
		} else {
			p.x /= force;
			p.y /= force;
		}
	}

	/**
	 * BodyのWorld座標系での回転行列(pitch, rollのみ)を返します.
	 *
	 * @param context
	 * @param mat
	 */
	private static void calculateBodyRotation(SomaticContext context,
			Matrix3f mat) {
		Matrix3f rot;
		// FIXME HipYawPitchのYawの回転がはいってる.
		if (context.isLeftOnGround())
			rot = context.get(Frames.LSole).getBodyRotation();
		else if (context.isRightOnGround())
			rot = context.get(Frames.RSole).getBodyRotation();
		else {
			mat.setIdentity();
			return;
		}
		Vector3f tmp = new Vector3f();
		MatrixUtils.rot2rpy(rot, tmp);
		tmp.z = 0;
		MatrixUtils.rpy2rot(tmp, mat);
	}

	private static float calculateBodyHeight(SomaticContext context) {
		// FIXME 未実装
		if (context.isLeftOnGround())
			return -context.get(Frames.LSole).getBodyPosition().y;
		if (context.isRightOnGround())
			return -context.get(Frames.RSole).getBodyPosition().y;
		return 320;
	}
}
