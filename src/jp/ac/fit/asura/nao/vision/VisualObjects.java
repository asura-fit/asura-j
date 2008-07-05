/*
 * 作成日: 2008/06/11
 */
package jp.ac.fit.asura.nao.vision;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public enum VisualObjects {
	Ball, BlueGoal, YellowGoal;

	public static enum Properties {
		Distance, // 距離(Integer, mm)
		DistanceUsable, // 距離が使えるか,quick hack (Boolean)
		Confidence, // 信頼度(Integer, ?)
		Center, // 中心の座標(Point2D, px)
		Angle, // 画像中心からの角度(Point2D, rad)
		Area, // 画像上の領域(Rectangle, px)
		TopTouched, BottomTouched, LeftTouched, RightTouched,
		// 画像の端に付いているか(Boolean)

		// 接地座標系での角度(Float, rad)
		// ロボット座標系ではない(基準は足下，腰ではない)ので注意
		// ほんとはLocalizationでやるべき
		RobotAngle,

		// ゴールポストかどうか. ポストだがどちらか判断できないなら，trueになる.
		IsRightPost, IsLeftPost,
		
		// 接地座標系での位置ベクトル
		Position
	}
}
