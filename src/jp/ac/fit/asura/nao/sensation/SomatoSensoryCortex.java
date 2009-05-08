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

import javax.vecmath.Matrix3f;
import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.MotionCycle;
import jp.ac.fit.asura.nao.MotionFrameContext;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.SensorContext;
import jp.ac.fit.asura.nao.Camera.CameraID;
import jp.ac.fit.asura.nao.Sensor.Function;
import jp.ac.fit.asura.nao.misc.Kinematics;
import jp.ac.fit.asura.nao.misc.MatrixUtils;
import jp.ac.fit.asura.nao.physical.Robot;
import jp.ac.fit.asura.nao.physical.RobotFrame;
import jp.ac.fit.asura.nao.physical.Robot.Frames;

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
public class SomatoSensoryCortex implements MotionCycle {
	private static final Logger log = Logger
			.getLogger(SomatoSensoryCortex.class);

	private Robot robot;

	private boolean useInertial;

	private float lastBodyHeight;

	/**
	 *
	 */
	public SomatoSensoryCortex() {
	}

	@Override
	public void init(RobotContext rctx) {
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

		// contextを初期化.
		if (context == null) {
			context = new SomaticContext(robot);
			frameContext.setSomaticContext(context);
		}

		// Robotが更新されていれば再作成する.
		if (context.getRobot() != robot) {
			context = new SomaticContext(robot);
			frameContext.setSomaticContext(context);
		}
		context.setTime(frameContext.getTime());

		// quick hack for bottom camera
		if (frameContext.getRobotContext().getCamera().getSelectedId() == CameraID.TOP) {
			context.get(Frames.CameraSelect).updateValue(0);
		} else {
			// FIXME この処理ではCameraSelect仮想関節を回転させるだけなので、カメラの位置に53.90 -
			// sqrt((67.90-23.81)*(48.8)) = 7.514mmの誤差がでる.
			context.get(Frames.CameraSelect).updateValue(0.6981f);
		}

		updateJoints(context, sensor);
		updateFSR(context, sensor);
		updateBodyPosture(context, sensor);
	}

	@Override
	public void stop() {
	}

	public void updateRobot(Robot robot) {
		this.robot = robot;
	}

	/**
	 * 関節角度を更新し運動学計算をする.
	 *
	 * @param context
	 * @param sensor
	 */
	private void updateJoints(SomaticContext context, SensorContext sensor) {
		for (FrameState joint : context.getFrames()) {
			if (joint.getId().isJoint()) {
				joint.updateValue(sensor.getJoint(joint.getId().toJoint()));
			}
		}

		Kinematics.calculateForward(context);
		Kinematics.calculateCenterOfMass(context);
	}

	/**
	 * 圧力センサーの情報を処理し、接地しているかどうかを判断する.
	 *
	 * @param context
	 * @param sensor
	 */
	private void updateFSR(SomaticContext context, SensorContext sensor) {
		float rfl = sensor.getForce(RFsrFL);
		float rfr = sensor.getForce(RFsrFR);
		float rbl = sensor.getForce(RFsrBL);
		float rbr = sensor.getForce(RFsrBR);
		float lfl = sensor.getForce(LFsrFL);
		float lfr = sensor.getForce(LFsrFR);
		float lbl = sensor.getForce(LFsrBL);
		float lbr = sensor.getForce(LFsrBR);
		float leftp = lfl + lfr + lbl + lbr;
		float rightp = rfl + rfr + rbl + rbr;
		float pressure = leftp + rightp;

		int leftCount = 0;
		final float TOUCH_THRESH = 0.5f;
		if (lfl > TOUCH_THRESH)
			leftCount++;
		if (lfr > TOUCH_THRESH)
			leftCount++;
		if (lbl > TOUCH_THRESH)
			leftCount++;
		if (lbr > TOUCH_THRESH)
			leftCount++;
		context.setLeftOnGround(leftCount >= 2);

		int rightCount = 0;
		if (rfl > TOUCH_THRESH)
			rightCount++;
		if (rfr > TOUCH_THRESH)
			rightCount++;
		if (rbl > TOUCH_THRESH)
			rightCount++;
		if (rbr > TOUCH_THRESH)
			rightCount++;
		context.setRightOnGround(rightCount >= 2);

		int cf = 0;
		if (context.isLeftOnGround())
			cf += 500;
		if (context.isRightOnGround())
			cf += 500;
		context.setConfidence(cf);

		Point2f left = context.getLeftCOP();
		left.x = 0;
		left.y = 0;
		left.x += lfl * context.get(Frames.LFsrFL).getBodyPosition().x;
		left.y += lfl * context.get(Frames.LFsrFL).getBodyPosition().z;
		left.x += lfr * context.get(Frames.LFsrFR).getBodyPosition().x;
		left.y += lfr * context.get(Frames.LFsrFR).getBodyPosition().z;
		left.x += lbl * context.get(Frames.LFsrBL).getBodyPosition().x;
		left.y += lbl * context.get(Frames.LFsrBL).getBodyPosition().z;
		left.x += lbr * context.get(Frames.LFsrBR).getBodyPosition().x;
		left.y += lbr * context.get(Frames.LFsrBR).getBodyPosition().z;

		Point2f right = context.getLeftCOP();
		right.x = 0;
		right.y = 0;
		right.x += rfl * context.get(Frames.RFsrFL).getBodyPosition().x;
		right.y += rfl * context.get(Frames.RFsrFL).getBodyPosition().z;
		right.x += rfr * context.get(Frames.RFsrFR).getBodyPosition().x;
		right.y += rfr * context.get(Frames.RFsrFR).getBodyPosition().z;
		right.x += rbl * context.get(Frames.RFsrBL).getBodyPosition().x;
		right.y += rbl * context.get(Frames.RFsrBL).getBodyPosition().z;
		right.x += rbr * context.get(Frames.RFsrBR).getBodyPosition().x;
		right.y += rbr * context.get(Frames.RFsrBR).getBodyPosition().z;

		Point2f cop = context.getCenterOfPressure();
		cop.x = left.x + right.x;
		cop.y = left.y + right.y;

		if (leftp != 0) {
			left.x /= leftp;
			left.y /= leftp;
		}

		if (rightp != 0) {
			right.x /= rightp;
			right.y /= rightp;
		}

		if (pressure != 0) {
			cop.x /= pressure;
			cop.y /= pressure;
		}

		context.setLeftPressure(leftp);
		context.setRightPressure(rightp);

		log.trace("Left pressure:" + leftp);
		log.trace("Right pressure:" + rightp);
	}

	/**
	 * Bodyの地面からの高さと傾きを計算する.
	 *
	 * @param context
	 * @param sensor
	 */
	private void updateBodyPosture(SomaticContext context, SensorContext sensor) {
		context.setBodyHeight(calculateBodyHeight(context));
		if (context.isOnGround())
			lastBodyHeight = context.getBodyHeight();
		if (useInertial) {
			Matrix3f posture = context.getBodyPosture();
			Vector3f tmp = new Vector3f();
			tmp.x = sensor.getInertialX();
			tmp.z = sensor.getInertialZ();
			MatrixUtils.pyr2rot(tmp, posture);
		} else {
			Matrix3f posture = context.getBodyPosture();
			calculateBodyRotation(context, posture);
		}
	}

	/**
	 * BodyのWorld座標系での回転行列(pitch, rollのみ)を返します.
	 *
	 * @param context
	 * @param mat
	 */
	private void calculateBodyRotation(SomaticContext context, Matrix3f mat) {
		// FIXME 未テスト
		Matrix3f rot;
		if (context.isLeftOnGround())
			rot = context.get(Frames.LSole).getBodyRotation();
		else if (context.isRightOnGround())
			rot = context.get(Frames.RSole).getBodyRotation();
		else {
			mat.setIdentity();
			return;
		}
		Vector3f tmp = new Vector3f();
		MatrixUtils.rot2pyr(rot, tmp);
		tmp.y = 0;
		MatrixUtils.pyr2rot(tmp, mat);
	}

	private float calculateBodyHeight(SomaticContext context) {
		// FIXME 未実装
		if (context.isLeftOnGround()) {
			return -context.get(Frames.LSole).getBodyPosition().y;
		}
		if (context.isRightOnGround())
			return -context.get(Frames.RSole).getBodyPosition().y;
		log.warn("Can't estimate body height. Use " + lastBodyHeight
				+ " [mm] Left:" + context.getLeftPressure() + " Right:"
				+ context.getRightPressure());
		return lastBodyHeight;
	}
}
