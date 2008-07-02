/*
 * 作成日: 2008/07/02
 */
package jp.ac.fit.asura.nao.misc;

import static jp.ac.fit.asura.nao.misc.MatrixUtils.inverseTransform;
import static jp.ac.fit.asura.nao.misc.MatrixUtils.transform;

import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.misc.PhysicalConstants.Nao;

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
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class Coordinates {
	/**
	 * 極座標系からデカルト座標系に変換します.
	 * 
	 * @param polar
	 * @return
	 */
	public static Vector3f polar2carthesian(Vector3f polar) {
		// polar.x = x-z平面の角度
		// polar.y = z-y平面の角度
		// polar.z = ベクトルの長さ
		return new Vector3f(
				(float) (Math.cos(polar.y) * Math.sin(polar.x) * polar.z),
				(float) (Math.sin(polar.y)) * polar.z, (float) (Math
						.cos(polar.y)
						* Math.cos(polar.x) * polar.z));
	}

	/**
	 * デカルト座標系から極座標系に変換します.
	 * 
	 * @param polar
	 * @return
	 */
	public static Vector3f carthesian2polar(Vector3f cart) {
		Vector3f polar = new Vector3f();
		polar.x = (float) Math.atan2(cart.x, cart.z);
		double temp1 = MathUtils.square(cart.x) + MathUtils.square(cart.z);
		double temp2 = MathUtils.square(cart.x) + MathUtils.square(cart.y)
				+ MathUtils.square(cart.z);

		double temp3 = Math.acos(Math.sqrt(temp1 / temp2));

		if (cart.y >= 0)
			polar.y = (float) temp3;
		else
			polar.y = (float) -temp3;
		polar.z = (float) Math.sqrt(temp2);
		return polar;
	}

	public static Vector3f camera2bodyCoord(Vector3f head, float pitchAngle,
			float yawAngle) {
		Vector3f pitch = transform(head, Nao.rCamera2headPitch, 0.0f,
				Nao.tCamera2headPitch);
		Vector3f yaw = transform(pitch, Nao.rHeadPitch2yaw, pitchAngle,
				Nao.tHeadPitch2yaw);
		Vector3f body = transform(yaw, Nao.rHeadYaw2body, yawAngle,
				Nao.tHeadYaw2body);
		return body;
	}

	public static Vector3f body2rSoleCoord(Vector3f body, float rHipYawPitch,
			float rHipRoll, float rHipPitch, float rKneePitch,
			float rAnklePitch, float rAnkleRoll) {
		Vector3f hyp = inverseTransform(body, Nao.rRhipYawPitch2body,
				rHipYawPitch, Nao.tRhipYawPitch2body);
		Vector3f hr = inverseTransform(hyp, Nao.rRhipRoll2yawPitch, rHipRoll,
				Nao.tRhipRoll2yawPitch);
		Vector3f hp = inverseTransform(hr, Nao.rRhipPitch2roll, rHipPitch,
				Nao.tRhipPitch2roll);
		Vector3f kp = inverseTransform(hp, Nao.rRkneePitch2hipPitch,
				rKneePitch, Nao.tRkneePitch2hipPitch);
		Vector3f ap = inverseTransform(kp, Nao.rRanklePitch2kneePitch,
				rAnklePitch, Nao.tRanklePitch2kneePitch);
		Vector3f ar = inverseTransform(ap, Nao.rRankleRoll2pitch, rAnkleRoll,
				Nao.tRankleRoll2pitch);
		Vector3f sole = inverseTransform(ar, Nao.rRsole2ankleRoll, 0.0f,
				Nao.tRsole2ankleRoll);
		return sole;
	}

	public static Vector3f body2lSoleCoord(Vector3f body, float lHipYawPitch,
			float lHipRoll, float lHipPitch, float lKneePitch,
			float lAnklePitch, float lAnkleRoll) {
		Vector3f hyp = inverseTransform(body, Nao.rLhipYawPitch2body,
				lHipYawPitch, Nao.tLhipYawPitch2body);
		Vector3f hr = inverseTransform(hyp, Nao.rLhipRoll2yawPitch, lHipRoll,
				Nao.tLhipRoll2yawPitch);
		Vector3f hp = inverseTransform(hr, Nao.rLhipPitch2roll, lHipPitch,
				Nao.tLhipPitch2roll);
		Vector3f kp = inverseTransform(hp, Nao.rLkneePitch2hipPitch,
				lKneePitch, Nao.tLkneePitch2hipPitch);
		Vector3f ap = inverseTransform(kp, Nao.rLanklePitch2kneePitch,
				lAnklePitch, Nao.tLanklePitch2kneePitch);
		Vector3f ar = inverseTransform(ap, Nao.rLankleRoll2pitch, lAnkleRoll,
				Nao.tLankleRoll2pitch);
		Vector3f sole = inverseTransform(ar, Nao.rLsole2ankleRoll, 0.0f,
				Nao.tLsole2ankleRoll);
		return sole;
	}

}
