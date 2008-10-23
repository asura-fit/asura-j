/*
 * 作成日: 2008/07/02
 */
package jp.ac.fit.asura.nao.misc;

import static jp.ac.fit.asura.nao.misc.MatrixUtils.inverseTransform;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.Camera;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.LAnklePitch;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.LAnkleRoll;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.LHipPitch;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.LHipRoll;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.LKneePitch;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RAnklePitch;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RAnkleRoll;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RHipPitch;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RHipRoll;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RHipYawPitch;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RKneePitch;

import javax.vecmath.Matrix3f;
import javax.vecmath.Point2d;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.physical.Nao;
import jp.ac.fit.asura.nao.physical.Nao.Frames;
import jp.ac.fit.asura.nao.sensation.FrameState;
import jp.ac.fit.asura.nao.sensation.SomaticContext;
import jp.ac.fit.asura.nao.vision.VisualContext;

/**
 * 座標系と変換に関するクラス.
 * 
 * ***X-Y-Z座標系(横，高さ，奥行き) 三次元の運動に関するもの. 基本的には右手座標系.
 * すなわち、右手でx軸を親指、y軸を人差し指とするとz軸は中指の方向. 数学では左手座標系が多いので注意.
 * 
 * (三次元) イメージ座標系(image): カメラを中心とする座標系. 左手座標系.
 * 
 * カメラ座標系(camera): カメラを中心とする座標系. 右手座標系.
 * 
 * ボディ座標系(body): 腰の真ん中を中心とする座標系. 右手座標系.
 * 
 * 足裏座標系(lSole,rSole): 各足の足裏の中心(Ankleの直下)を中心とする座標系. 右手座標系.
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
 * (二次元) イメージ座標系(image): 画像平面の中央を原点として、右に+x、上に+yをとる座標系(左手座標系). x-yのみ.
 * 
 * 画像平面座標系(plane): 画像平面の<b>左上</b>を原点として、<b>右に+x</b>、<b>下に+y</b>をとる座標系. x-yのみ.
 * 
 * リンク座標系: 各関節を原点とする座標系. 各関節に一つある. 右手座標系.
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

	/**
	 * 画像平面座標系(plane)の点から二次元イメージ座標系(image)の点に変換します.
	 * 
	 * @param context
	 * @param plane
	 * @param image
	 */
	public static void plane2imageCoord(VisualContext context, Point2d plane,
			Point2d image) {
		image.x = plane.getX() - context.camera.width / 2;
		image.y = -plane.getY() + context.camera.height / 2;
	}

	/**
	 * (三次元)イメージ座標系からカメラ座標系へ変換します. z軸を反転させることで、左手座標から右手座標系へ変換します.
	 * 
	 * Camera座標系では、y軸方向に180°回転しているため、z軸はロボットの背側が+、腹側が-になっています.
	 * x軸はそれに合わせて右肩のほうを向いているので右手座標系ではありますが、直感的にわかりにくい変換となっています..
	 * 
	 * @param imageCoord
	 */
	public static void image2cameraCoord(Vector3f imageCoord,
			Vector3f cameraCoord) {
		cameraCoord.z = -imageCoord.z;
		if (imageCoord != cameraCoord) {
			cameraCoord.x = imageCoord.x;
			cameraCoord.y = imageCoord.y;
		}
	}

	/**
	 * Body座標系への回転変換をします.
	 * 
	 * @param from
	 * @param src
	 * @param dest
	 * @param context
	 */
	public static void toBodyRotation(SomaticContext context, Frames from,
			Vector3f src, Vector3f dest) {
		FrameState fs = context.get(from);
		Matrix3f mat = new Matrix3f();
		mat.transpose(fs.getRobotRotation());
		mat.transform(src, dest);
	}

	public static void fromBodyRotation(SomaticContext context, Frames to,
			Vector3f src, Vector3f dest) {
		FrameState fs = context.get(to);
		fs.getRobotRotation().transform(src, dest);
	}

	/**
	 * カメラ座標系からBody座標系に座標変換をします.
	 * 
	 * @param camera2body
	 * @param context
	 */
	public static void camera2bodyCoord(SomaticContext context,
			Vector3f camera2body) {
		FrameState fs = context.get(Camera);
		toBodyRotation(context, Camera, camera2body, camera2body);
		camera2body.add(fs.getRobotPosition());
	}

	public static void body2rSoleCoord(SomaticContext context,
			Vector3f body2sole) {
		inverseTransform(body2sole, Nao.get(Frames.RHipYawPitch), context.get(
				RHipYawPitch).getAngle());
		inverseTransform(body2sole, Nao.get(Frames.RHipRoll), context.get(
				RHipRoll).getAngle());
		inverseTransform(body2sole, Nao.get(Frames.RHipPitch), context.get(
				RHipPitch).getAngle());
		inverseTransform(body2sole, Nao.get(Frames.RKneePitch), context.get(
				RKneePitch).getAngle());
		inverseTransform(body2sole, Nao.get(Frames.RAnklePitch), context.get(
				RAnklePitch).getAngle());
		inverseTransform(body2sole, Nao.get(Frames.RAnkleRoll), context.get(
				RAnkleRoll).getAngle());
		inverseTransform(body2sole, Nao.get(Frames.RSole), 0.0f);
	}

	public static void body2lSoleCoord(SomaticContext context,
			Vector3f body2sole) {
		inverseTransform(body2sole, Nao.get(Frames.LHipYawPitch), context.get(
				RHipYawPitch).getAngle());
		inverseTransform(body2sole, Nao.get(Frames.LHipRoll), context.get(
				LHipRoll).getAngle());
		inverseTransform(body2sole, Nao.get(Frames.LHipPitch), context.get(
				LHipPitch).getAngle());
		inverseTransform(body2sole, Nao.get(Frames.LKneePitch), context.get(
				LKneePitch).getAngle());
		inverseTransform(body2sole, Nao.get(Frames.LAnklePitch), context.get(
				LAnklePitch).getAngle());
		inverseTransform(body2sole, Nao.get(Frames.LAnkleRoll), context.get(
				LAnkleRoll).getAngle());
		inverseTransform(body2sole, Nao.get(Frames.LSole), 0.0f);
	}
}
