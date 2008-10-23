/*
 * 作成日: 2008/06/18
 */
package jp.ac.fit.asura.nao.vision.perception;

import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.vision.VisualObjects;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class BallVisualObject extends VisualObject {
	// 接地座標系での位置ベクトル
	public Vector3f robotPosition;
	
	// 距離(mm)
	public int distance;
	
	// 距離が使えるか,quick hack
	public boolean distanceUsable;
	
	// 接地座標系での角度(rad)
	// ロボット座標系ではない(基準は足下，腰ではない)ので注意
	// ほんとはLocalizationでやるべき
	public float robotAngle;

	public BallVisualObject() {
		super(VisualObjects.Ball);
		robotPosition = new Vector3f();
	}
}
