/*
 * 作成日: 2008/06/11
 */
package jp.ac.fit.asura.nao.localization;

import javax.vecmath.Point2i;

import jp.ac.fit.asura.nao.misc.AverageFilter;
import jp.ac.fit.asura.nao.misc.Filter.FloatFilter;
import jp.ac.fit.asura.nao.misc.Filter.IntFilter;
import jp.ac.fit.asura.nao.vision.perception.VisualObject;

/**
 * @author sey
 *
 * @version $Id: WorldObject.java 704 2008-10-23 17:25:51Z sey $
 *
 */
public class WorldObject {
	private WorldObjects type;

	private VisualObject vision;

	// 信頼度(0 ～ 999)
	protected int cf;

	// ワールド座標系での座標
	protected Point2i world;

	// チーム座標系での座標
	protected Point2i team;

	// ワールド座標系での角度
	// 使わないかも
	protected float worldAngle;
	protected float teamAngle;

	// ワールド座標系での姿勢(今のところSelf以外は意味なし)
	// protected float roll;
	// protected float pitch;
	protected float worldYaw;
	protected float teamYaw;

	// ロボット座標系での距離(ロボット中心からの距離,y軸を除く)
	protected int dist;

	// ロボット座標系での角度(ロボット中心からの相対角度,x-z軸)
	protected float heading;

	// 最後に認識した時間.
	protected long lasttime;

	// 最後に認識した時間からの経過時間
	protected long difftime;

	protected IntFilter distFilter;
	protected FloatFilter headingFilter;

	public WorldObject(WorldObjects type) {
		this.type = type;
		world = new Point2i();
		team = new Point2i();
		distFilter = new AverageFilter.Int(8);
		headingFilter = new AverageFilter.Float(8);
	}

	public VisualObject getVision() {
		return vision;
	}

	protected void setVision(VisualObject vision) {
		this.vision = vision;
	}

	/**
	 * チーム座標系でのX座標を返します[mm].
	 *
	 * @return
	 */
	public int getX() {
		return team.x;
	}

	/**
	 * チーム座標系でのY座標を返します[mm].
	 *
	 * @return
	 */
	public int getY() {
		return team.y;
	}

	/**
	 * チーム座標系での角度( atan(y/n) as degrees )を返します.
	 *
	 * @return
	 */
	@Deprecated
	public float getAngle() {
		return teamAngle;
	}

	/**
	 * チーム座標系でのYaw姿勢を返します[deg].
	 *
	 * 二次元平面上での方向.
	 *
	 * @return
	 */
	public float getYaw() {
		return teamYaw;
	}

	/**
	 * ロボット座標系でのx-y平面上の距離を返します[mm].
	 *
	 * @return
	 */
	public int getDistance() {
		return dist;
	}

	/**
	 * ロボット座標系でのx-y平面上の角度を返します[deg].
	 *
	 * Selfオブジェクトでは常に0となるので注意(getYaw()を使うこと).
	 *
	 * @return
	 */
	public float getHeading() {
		return heading;
	}

	/**
	 * このオブジェクトの信頼度を返します. 0(信頼度低)から1000(信頼度高)までの値をとります.
	 *
	 * @return
	 */
	public int getConfidence() {
		return cf;
	}

	/**
	 * このオブジェクトのX座標での位置を返します[mm].
	 *
	 * @return
	 */
	public int getWorldX() {
		return world.x;
	}

	/**
	 * このオブジェクトのY座標での位置を返します[mm].
	 *
	 * @return
	 */
	public int getWorldY() {
		return world.y;
	}

	/**
	 * このオブジェクトの位置のX/Y平面上での角度を返します. Atan2(x,y)と同じです.
	 */
	@Deprecated
	public float getWorldAngle() {
		return worldAngle;
	}

	public WorldObjects getType() {
		return type;
	}

	/**
	 * そのWorldObjectを最後に認識してからの経過時間を取得する[ms]
	 *
	 * @return
	 */
	public long getDifftime() {
		return difftime;
	}

	public void invalidate() {
		cf = 0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("WorldObject " + type);
		sb.append(" Dist:" + getDistance() + " Heading:" + getHeading());
		sb.append(" X:" + getX() + " Y:" + getY());
		sb.append(" conf:" + getConfidence());
		return sb.toString();
	}

}
