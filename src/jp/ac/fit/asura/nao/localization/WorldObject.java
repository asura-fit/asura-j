/*
 * 作成日: 2008/06/11
 */
package jp.ac.fit.asura.nao.localization;

import javax.vecmath.Point2i;

import jp.ac.fit.asura.nao.misc.MeanFilter;
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

	protected IntFilter distFilter;
	protected FloatFilter headingFilter;

	public WorldObject() {
		world = new Point2i();
		team = new Point2i();
		distFilter = new MeanFilter.Int(4);
		headingFilter = new MeanFilter.Float(4);
	}

	public VisualObject getVision() {
		return vision;
	}

	protected void setVision(VisualObject vision) {
		this.vision = vision;
	}

	/**
	 * チーム座標系でのX座標を返します.
	 *
	 * @return
	 */
	public int getX() {
		return team.x;
	}

	/**
	 * チーム座標系でのY座標を返します.
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
	 * チーム座標系でのYaw姿勢を返します.
	 *
	 * 二次元平面上での方向.
	 *
	 * @return
	 */
	public float getYaw() {
		return teamYaw;
	}

	/**
	 * ロボット座標系でのx-y平面上の距離を返します.
	 *
	 * @return
	 */
	public int getDistance() {
		return dist;
	}

	/**
	 * ロボット座標系でのx-y平面上の角度を返します.
	 *
	 * Selfオブジェクトでは常に0となるので注意(getYaw()を使うこと).
	 *
	 * @return
	 */
	public float getHeading() {
		return heading;
	}

	public int getConfidence() {
		return cf;
	}

	public int getWorldX() {
		return world.x;
	}

	public int getWorldY() {
		return world.y;
	}

	@Deprecated
	public float getWorldAngle() {
		return worldAngle;
	}

	public void invalidate() {
		cf = 0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("WorldObject " + vision.getType());
		sb.append(" Dist:" + getDistance() + " Heading:" + getHeading());
		sb.append(" X:" + getX() + " Y:" + getY());
		sb.append(" conf:" + getConfidence());
		return sb.toString();
	}
}
