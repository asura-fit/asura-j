/*
 * 作成日: 2008/06/18
 */
package jp.ac.fit.asura.nao.vision.perception;

import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.vision.VisualObjects;

/**
 * @author sey
 *
 * @version $Id: BallVisualObject.java 704 2008-10-23 17:25:51Z sey $
 *
 */
public class BallVisualObject extends VisualObject {
	// 接地座標系での位置ベクトル
	public Vector3f robotPosition;

	// 距離(mm)
	public int distance;

	// 距離が使えるか,quick hack
	public boolean distanceUsable;

	public BallVisualObject() {
		super(VisualObjects.Ball);
		robotPosition = new Vector3f();
	}
}
