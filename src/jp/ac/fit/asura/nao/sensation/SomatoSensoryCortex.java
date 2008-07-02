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

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.Sensor;

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
public class SomatoSensoryCortex implements RobotLifecycle {
	private Sensor sensor;

	public void init(RobotContext rctx) {
		sensor = rctx.getSensor();
	}

	public void start() {
	}

	public void step() {
	}

	public void stop() {
	}

	/**
	 * 現在の体勢から，カメラ座標系での位置を接地座標系に変換します.
	 * 
	 * 両足が接地していることが前提.
	 * 
	 * @return 接地座標系でのカメラの位置(mm)
	 */
	public Vector3f getCameraPosition(Vector3f camera) {
		Vector3f body = camera2bodyCoord(camera, -sensor.getJoint(HeadPitch),
				-sensor.getJoint(HeadYaw));

		Vector3f lSole = body2lSoleCoord(body, -sensor.getJoint(LHipYawPitch),
				-sensor.getJoint(LHipRoll), -sensor.getJoint(LHipPitch),- sensor
						.getJoint(LKneePitch), -sensor.getJoint(LAnklePitch),
				-sensor.getJoint(LAnkleRoll));
		Vector3f rSole = body2rSoleCoord(body, -sensor.getJoint(RHipYawPitch),
			-	sensor.getJoint(RHipRoll),- sensor.getJoint(RHipPitch), -sensor
						.getJoint(RKneePitch),- sensor.getJoint(RAnklePitch),
				-sensor.getJoint(RAnkleRoll));

		lSole.add(rSole);
		lSole.scale(0.5f);
		return lSole;
	}
}
