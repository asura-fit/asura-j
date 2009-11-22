/*
 * 作成日: 2008/07/02
 */
package jp.ac.fit.asura.nao.misc;

import static jp.ac.fit.asura.nao.physical.Robot.Frames.NaoCam;

import javax.vecmath.Matrix3f;
import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.FrameState;
import jp.ac.fit.asura.nao.sensation.SomaticContext;
import jp.ac.fit.asura.nao.vision.VisualContext;

/**
 * 座標系と変換に関するクラス.
 *
 * <pre>
 * 基本的に長さの単位はmm
 * ***X-Y-Z座標系(横，高さ，奥行き) 三次元の運動に関するもの. 基本的には右手座標系.
 * すなわち、右手でx軸を親指、y軸を人差し指とするとz軸は中指の方向. 数学では左手座標系が多いので注意.
 *  (三次元) イメージ座標系(image): カメラを中心とする座標系. 左手座標系.
 *  カメラ座標系(camera): カメラを中心とする座標系. 右手座標系. ただしNaoCam関節の定義により、Y軸が180度回転している.
 *  ボディ座標系(body): 腰の真ん中を中心とする座標系. 右手座標系.
 *  (三次元)ロボット座標系(robot): ボディ座標系に対し、姿勢をワールド座標系にあわせて回転したもの.
 *  すなわち、腰の真ん中を中心とするワールド座標系. 右手座標系.
 *  (三次元)ワールド座標系(world): フィールドの中心を原点(0,0,0)として、z軸をブルーゴール(0,0,2700)の方向にとった座標系.
 *  右手座標系.
 *  接地座標系: ロボット座標系から腰の高さを引いたもの. ようは腰の真下を原点とする座標系.
 * ***X-Y座標系(横，縦) ローカリとかに関するもの. 基本的には右手座標系、X-Z平面. ただしZ軸はY軸と表記される. 回転は左周りが+になる.
 *  (二次元)ロボット座標系(robot): 三次元ロボット座標系から高さ(y軸)をなくし、Z軸をY軸としたもの. X-Z平面(Z軸はY軸と表記). 右手座標系.
 *  (二次元)ワールド座標系(world): フィールド座標系: 三次元ワールド座標系から高さ(y軸)をなくし、Z軸をY軸としたもの.  X-Z平面(Z軸はY軸と表記). 右手座標系.
 *  チーム座標系(team): フィールドを中心として，自ゴールを0,-2700とする座標系. レッドチームではワールド座標系と一致する.
 * ***その他
 *  (二次元) イメージ座標系(image): 画像平面の中央を原点として、右に+x、上に+yをとる座標系(左手座標系). x-yのみ.
 *  画像平面座標系(plane): 画像平面の左上を原点として、右に+x、下に+yをとる座標系. x-yのみ.
 *  リンク座標系: 各関節を原点とする座標系. 各関節に一つある. 右手座標系.
 *  Angle: Atan2(x,z)とAtan2(y,z)
 *  極座標系: @see http://mathworld.wolfram.com/SphericalCoordinates.html
 *  デカルト座標系: ぐぐれ.
 * </pre>
 *
 * @author $Author: sey $
 *
 * @version $Id: Coordinates.java 717 2008-12-31 18:16:20Z sey $
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
		// polar.x = ベクトルの長さ
		// polar.y = z軸を回るx-y平面の角度
		// polar.z = x軸を回るz-y平面の角度
		float cosTheta = (float) Math.cos(polar.y);
		float sinTheta = (float) Math.sin(polar.y);
		float cosPhi = (float) Math.cos(polar.z);
		float sinPhi = (float) Math.sin(polar.z);
		carthesian.z = polar.x * cosPhi;
		carthesian.y = polar.x * sinTheta * sinPhi;
		carthesian.x = polar.x * cosTheta * sinPhi;
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
		polar.y = (float) Math.atan2(carthesian.y, carthesian.x);
		polar.x = (float) Math.sqrt(MathUtils.square(carthesian.x)
				+ MathUtils.square(carthesian.y)
				+ MathUtils.square(carthesian.z));
		polar.z = (float) Math.acos(carthesian.z / polar.x);
	}

	/**
	 * 画像平面座標系(plane)の点から二次元イメージ座標系(image)の点に変換します.
	 *
	 * @param context
	 * @param plane
	 * @param image
	 */
	public static void plane2imageCoord(VisualContext context, Point2f plane,
			Point2f image) {
		image.x = plane.getX() - context.image.getWidth() / 2;
		image.y = -plane.getY() + context.image.getHeight() / 2;
	}

	/**
	 * (三次元)イメージ座標系からカメラ座標系へ変換します. z軸を反転させることで、左手座標から右手座標系へ変換します.
	 *
	 *<pre>
	 * Camera座標系では、y軸方向に180°回転しているため、z軸はロボットの背側が+、腹側が-になっています.
	 * x軸はそれに合わせて右肩のほうを向いているので右手座標系ではありますが、直感的にわかりにくい変換となっています..
	 * </pre>
	 *
	 * @param imageCoord
	 */
	public static void image2cameraCoord(Vector3f imageCoord,
			Vector3f cameraCoord) {
		cameraCoord.x = imageCoord.x;
		cameraCoord.y = imageCoord.y;
		cameraCoord.z = -imageCoord.z;
	}

	/**
	 * from座標系のベクトルsrcを回転し、Body座標系からみたベクトルの向きを返します.
	 *
	 * @param context
	 *            計算に使用するSomaticContext
	 * @param from
	 *            変換元の座標系
	 * @param src
	 *            変換元座標系内のベクトル
	 * @param dest
	 *            Body座標系に変換した結果のベクトル
	 */
	public static void toBodyRotation(SomaticContext context, Frames from,
			Vector3f src, Vector3f dest) {
		FrameState fs = context.get(from);
		fs.getBodyRotation().transform(src, dest);
	}

	/**
	 * from座標系のベクトルsrcを同次変換し、 Body座標系でのベクトルの位置を返します.
	 *
	 * @param context
	 *            計算に使用するSomaticContext
	 * @param from
	 *            変換元の座標系
	 * @param src
	 *            変換元座標系内のベクトル
	 * @param dest
	 *            Body座標系に変換した結果のベクトル
	 */
	public static void toBodyCoord(SomaticContext context, Frames from,
			Vector3f src, Vector3f dest) {
		FrameState fs = context.get(from);
		toBodyRotation(context, from, src, dest);
		dest.add(fs.getBodyPosition());
	}

	/**
	 * Body座標系のベクトルsrcを回転し、to座標系からみたベクトルの向きを返します.
	 *
	 * @param context
	 *            計算に使用するSomaticContext
	 * @param to
	 *            変換先の座標系
	 * @param src
	 *            Body座標系内のベクトル
	 * @param dest
	 *            to座標系に変換した結果のベクトル
	 */
	public static void toFrameRotation(SomaticContext context, Frames to,
			Vector3f src, Vector3f dest) {
		FrameState fs = context.get(to);
		Matrix3f mat = new Matrix3f();
		mat.transpose(fs.getBodyRotation());
		mat.transform(src, dest);
	}

	/**
	 * Body座標系のベクトルsrcを同次変換し、 to座標系でのベクトルの位置を返します.
	 *
	 * @param context
	 *            計算に使用するSomaticContext
	 * @param from
	 *            変換先の座標系
	 * @param src
	 *            Body座標系内のベクトル
	 * @param dest
	 *            to座標系に変換した結果のベクトル
	 */
	public static void toFrameCoord(SomaticContext context, Frames to,
			Vector3f src, Vector3f dest) {
		FrameState fs = context.get(to);
		dest.sub(fs.getBodyPosition());
		toFrameRotation(context, to, src, dest);
	}

	/**
	 *
	 * @param frame
	 * @param imageAngle
	 * @param bodyAngle
	 */
	public static void image2cameraAngle(SomaticContext context,
			Point2f imageAngle, Point2f cameraAngle) {
		// one, convert image angle to camera angle.
		// image座標系ではx軸の向きが逆.
		cameraAngle.x = -imageAngle.x + MathUtils.PIf;
		cameraAngle.y = imageAngle.y;
	}

	public static void camera2bodyAngle(SomaticContext context,
			Point2f cameraAngle, Point2f bodyAngle) {
		Vector3f vec = new Vector3f();
		angle2carthesian(1, cameraAngle, vec);
		toBodyRotation(context, NaoCam, vec, vec);
		carthesian2angle(vec, bodyAngle);
	}

	/**
	 *
	 * @param frame
	 * @param imageAngle
	 * @param bodyAngle
	 */
	public static void body2robotAngle(SomaticContext context,
			Point2f bodyAngle, Point2f robotAngle) {
		Vector3f vec = new Vector3f();
		Coordinates.angle2carthesian(1, bodyAngle, vec);
		body2robotCoord(context, vec, vec);
		Coordinates.carthesian2angle(vec, robotAngle);
	}

	public static void angle2carthesian(float r, Point2f angle,
			Vector3f carthesian) {
		float cosTheta = (float) Math.cos(angle.x);
		float sinTheta = (float) Math.sin(angle.x);
		float cosPhi = (float) Math.cos(angle.y);
		float sinPhi = (float) Math.sin(angle.y);
		carthesian.x = r * sinTheta * cosPhi;
		carthesian.y = r * sinPhi;
		carthesian.z = r * cosTheta * cosPhi;
	}

	public static void carthesian2angle(Vector3f carthesian, Point2f angle) {
		float d = (float) Math.sqrt(MathUtils.square(carthesian.x)
				+ MathUtils.square(carthesian.y)
				+ MathUtils.square(carthesian.z));
		angle.x = (float) Math.atan2(carthesian.x, carthesian.z);
		angle.y = (float) Math.asin(carthesian.y / d);
	}

	public static void body2robotCoord(SomaticContext context, Vector3f src,
			Vector3f dest) {
		Matrix3f rot = new Matrix3f();
		rot.transpose(context.getBodyPosture());
		rot.transform(src, dest);
	}

	public static void robot2bodyCoord(SomaticContext context, Vector3f src,
			Vector3f dest) {
		Matrix3f rot = context.getBodyPosture();
		rot.transform(src, dest);
	}
}
