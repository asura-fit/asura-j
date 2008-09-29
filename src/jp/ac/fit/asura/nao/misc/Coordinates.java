/*
 * 作成日: 2008/07/02
 */
package jp.ac.fit.asura.nao.misc;

import static jp.ac.fit.asura.nao.Joint.HeadPitch;
import static jp.ac.fit.asura.nao.Joint.HeadYaw;
import static jp.ac.fit.asura.nao.Joint.LAnklePitch;
import static jp.ac.fit.asura.nao.Joint.LAnkleRoll;
import static jp.ac.fit.asura.nao.Joint.LHipPitch;
import static jp.ac.fit.asura.nao.Joint.LHipRoll;
import static jp.ac.fit.asura.nao.Joint.LKneePitch;
import static jp.ac.fit.asura.nao.Joint.RAnklePitch;
import static jp.ac.fit.asura.nao.Joint.RAnkleRoll;
import static jp.ac.fit.asura.nao.Joint.RHipPitch;
import static jp.ac.fit.asura.nao.Joint.RHipRoll;
import static jp.ac.fit.asura.nao.Joint.RHipYawPitch;
import static jp.ac.fit.asura.nao.Joint.RKneePitch;
import static jp.ac.fit.asura.nao.misc.MatrixUtils.inverseTransform;
import static jp.ac.fit.asura.nao.misc.MatrixUtils.transform;

import java.util.EnumMap;

import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.physical.Nao;
import jp.ac.fit.asura.nao.physical.Nao.Frames;
import jp.ac.fit.asura.nao.sensation.JointState;

/**
 * 座標系と変換に関するクラス.
 * 
 * ***X-Y-Z座標系(横，高さ，奥行き) 三次元の運動に関するもの.
 * 
 * カメラ座標系(camera): カメラを中心とする座標系.
 * 
 * ボディ座標系(body): 腰の真ん中を中心とする座標系.
 * 
 * 足裏座標系(lSole,rSole): 各足の足裏の中心(Ankleの直下)を中心とする座標系.
 * 
 * 接地座標系: ボディ座標系の原点(腰の真ん中)から，x-z平面(フィールド)に降ろした線と, フィールドを交わる地点を原点とする座標系.
 * ようは腰の真下を原点とする座標系. 足裏座標系の平均で近似できる?
 * 
 * ***X-Y座標系(横，縦) ローカリとかに関するもの.
 * 
 * ロボット座標系: 接地座標系から高さ方向をなくしたもの. AIBOのものと同じ(たぶん).
 * 
 * ワールド座標系(world),フィールド座標系: フィールドの真ん中を中心として，イエローゴールを0,-2700とする座標系.
 * 高さ(z成分)は考慮されていない. (したほうがいいか?)
 * 
 * チーム座標系(team): フィールドを中心として，自ゴールを0,-2700とする座標系. レッドチームではワールド座標系と一致する.
 * 
 * ***その他
 * 
 * リンク座標系: 各関節を原点とする座標系. 各関節に一つある.
 * 
 * 極座標系: ぐぐれ.
 * 
 * デカルト座標系: ぐぐれ.
 * 
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class Coordinates {
	/**
	 * 極座標系からデカルト座標系に変換します.
	 * 
	 * @param polar
	 * @return
	 */
	public static void polar2carthesian(Vector3f polar, Vector3f carthesian) {
		// もしも同じオブジェクトへの操作なら一時オブジェクトを作成
		if (polar == carthesian)
			polar = new Vector3f(polar);
		// polar.x = x-z平面の角度
		// polar.y = z-y平面の角度
		// polar.z = ベクトルの長さ
		carthesian.x = (float) (Math.cos(polar.y) * Math.sin(polar.x) * polar.z);
		carthesian.y = (float) (Math.sin(polar.y)) * polar.z;
		carthesian.z = (float) (Math.cos(polar.y) * Math.cos(polar.x) * polar.z);
	}

	/**
	 * デカルト座標系から極座標系に変換します.
	 * 
	 * @param polar
	 * @return
	 */
	public static void carthesian2polar(Vector3f carthesian, Vector3f polar) {
		// もしも同じオブジェクトへの操作なら一時オブジェクトを作成
		if (carthesian == polar)
			carthesian = new Vector3f(carthesian);
		polar.x = (float) Math.atan2(carthesian.x, carthesian.z);
		double temp1 = MathUtils.square(carthesian.x)
				+ MathUtils.square(carthesian.z);
		double temp2 = MathUtils.square(carthesian.x)
				+ MathUtils.square(carthesian.y)
				+ MathUtils.square(carthesian.z);

		double temp3 = Math.acos(Math.sqrt(temp1 / temp2));

		if (carthesian.y >= 0)
			polar.y = (float) temp3;
		else
			polar.y = (float) -temp3;
		polar.z = (float) Math.sqrt(temp2);
	}

	public static void camera2bodyCoord(Vector3f camera2body,
			EnumMap<Joint, JointState> joints) {
		transform(camera2body, Nao.get(Frames.Camera), 0.0f);
		transform(camera2body, Nao.get(Frames.HeadPitch), joints.get(HeadPitch)
				.getValue());
		transform(camera2body, Nao.get(Frames.HeadYaw), joints.get(HeadYaw)
				.getValue());
	}

	public static void body2rSoleCoord(Vector3f body2sole,
			EnumMap<Joint, JointState> joints) {
		inverseTransform(body2sole, Nao.get(Frames.RHipYawPitch), joints.get(
				RHipYawPitch).getValue());
		inverseTransform(body2sole, Nao.get(Frames.RHipRoll), joints.get(
				RHipRoll).getValue());
		inverseTransform(body2sole, Nao.get(Frames.RHipPitch), joints.get(
				RHipPitch).getValue());
		inverseTransform(body2sole, Nao.get(Frames.RKneePitch), joints.get(
				RKneePitch).getValue());
		inverseTransform(body2sole, Nao.get(Frames.RAnklePitch), joints.get(
				RAnklePitch).getValue());
		inverseTransform(body2sole, Nao.get(Frames.RAnkleRoll), joints.get(
				RAnkleRoll).getValue());
		inverseTransform(body2sole, Nao.get(Frames.RSole), 0.0f);
	}

	public static void body2lSoleCoord(Vector3f body2sole,
			EnumMap<Joint, JointState> joints) {
		inverseTransform(body2sole, Nao.get(Frames.LHipYawPitch), joints.get(
				RHipYawPitch).getValue());
		inverseTransform(body2sole, Nao.get(Frames.LHipRoll), joints.get(
				LHipRoll).getValue());
		inverseTransform(body2sole, Nao.get(Frames.LHipPitch), joints.get(
				LHipPitch).getValue());
		inverseTransform(body2sole, Nao.get(Frames.LKneePitch), joints.get(
				LKneePitch).getValue());
		inverseTransform(body2sole, Nao.get(Frames.LAnklePitch), joints.get(
				LAnklePitch).getValue());
		inverseTransform(body2sole, Nao.get(Frames.LAnkleRoll), joints.get(
				LAnkleRoll).getValue());
		inverseTransform(body2sole, Nao.get(Frames.LSole), 0.0f);
	}
}
