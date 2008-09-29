/*
 * 作成日: 2008/07/01
 */
package jp.ac.fit.asura.nao.sensation;

import static jp.ac.fit.asura.nao.TouchSensor.LFsrBL;
import static jp.ac.fit.asura.nao.TouchSensor.LFsrBR;
import static jp.ac.fit.asura.nao.TouchSensor.LFsrFL;
import static jp.ac.fit.asura.nao.TouchSensor.LFsrFR;
import static jp.ac.fit.asura.nao.TouchSensor.RFsrBL;
import static jp.ac.fit.asura.nao.TouchSensor.RFsrBR;
import static jp.ac.fit.asura.nao.TouchSensor.RFsrFL;
import static jp.ac.fit.asura.nao.TouchSensor.RFsrFR;

import java.awt.Point;
import java.util.EnumMap;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.event.MotionEventListener;
import jp.ac.fit.asura.nao.event.VisualEventListener;
import jp.ac.fit.asura.nao.misc.Coordinates;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.physical.Nao;
import jp.ac.fit.asura.nao.physical.Nao.Frames;
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
 * @version $Id$
 * 
 */
public class SomatoSensoryCortex implements RobotLifecycle,
		MotionEventListener, VisualEventListener {
	private Logger log = Logger.getLogger(SomatoSensoryCortex.class);

	private Sensor sensor;

	private Vector4f ball;

	private EnumMap<Joint, JointState> joints;

	private Boolean leftOnGround;
	private Boolean rightOnGround;

	public void init(RobotContext rctx) {
		sensor = rctx.getSensor();
		rctx.getVision().addEventListener(this);
		rctx.getMotor().addEventListener(this);

		ball = new Vector4f();

		joints = new EnumMap<Joint, JointState>(Joint.class);
		for (Joint joint : Joint.values())
			joints.put(joint, new JointState(joint));
	}

	public void start() {
	}

	public void step() {
		leftOnGround = null;
		rightOnGround = null;

		for (JointState joint : joints.values()) {
			joint.updateValue(sensor.getJoint(joint.getId()));
			joint.updateForce(sensor.getForce(joint.getId()));
		}
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
		camera2bodyCoord(body);

		Vector3f lSole = new Vector3f(body);

		if (isLeftOnGround()) {
			body2lSoleCoord(lSole);
			lSole.x -= (int) (Nao.get(Frames.LHipYawPitch).translate.x);
		}

		Vector3f rSole = new Vector3f(body);
		if (isRightOnGround()) {
			body2rSoleCoord(rSole);
			rSole.x -= (int) (Nao.get(Frames.RHipYawPitch).translate.x);
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

		p.x += forces[0] * Nao.get(Frames.LSoleFL).translate.x;
		p.y += forces[0] * Nao.get(Frames.LSoleFL).translate.z;
		force += forces[0];

		p.x += forces[1] * Nao.get(Frames.LSoleFR).translate.x;
		p.y += forces[1] * Nao.get(Frames.LSoleFR).translate.z;
		force += forces[1];

		p.x += forces[2] * Nao.get(Frames.LSoleBL).translate.x;
		p.y += forces[2] * Nao.get(Frames.LSoleBL).translate.z;
		force += forces[2];

		p.x += forces[3] * Nao.get(Frames.LSoleBR).translate.x;
		p.y += forces[3] * Nao.get(Frames.LSoleBR).translate.z;
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

		p.x += forces[0] * Nao.get(Frames.RSoleFL).translate.x;
		p.y += forces[0] * Nao.get(Frames.RSoleFL).translate.z;
		force += forces[0];

		p.x += forces[1] * Nao.get(Frames.RSoleFR).translate.x;
		p.y += forces[1] * Nao.get(Frames.RSoleFR).translate.z;
		force += forces[1];

		p.x += forces[2] * Nao.get(Frames.RSoleBL).translate.x;
		p.y += forces[2] * Nao.get(Frames.RSoleBL).translate.z;
		force += forces[2];

		p.x += forces[3] * Nao.get(Frames.RSoleBR).translate.x;
		p.y += forces[3] * Nao.get(Frames.RSoleBR).translate.z;
		force += forces[3];

		if (force == 0) {
			// not on ground
			p.x = p.y = 0;
		} else {
			p.x /= force;
			p.y /= force;
		}
	}

	public EnumMap<Joint, JointState> getCurrentPosture() {
		return joints;
	}

	public EnumMap<Joint, JointState> copyPosture() {
		EnumMap<Joint, JointState> map = new EnumMap<Joint, JointState>(
				Joint.class);
		for (Joint j : joints.keySet()) {
			map.put(j, joints.get(j).clone());
		}
		return map;
	}

	public void camera2bodyCoord(Vector3f camera2body) {
		Coordinates.camera2bodyCoord(camera2body, getCurrentPosture());
	}

	public void body2rSoleCoord(Vector3f body2sole) {
		Coordinates.body2rSoleCoord(body2sole, getCurrentPosture());
	}

	public void body2lSoleCoord(Vector3f body2sole) {
		Coordinates.body2lSoleCoord(body2sole, getCurrentPosture());
	}
}
