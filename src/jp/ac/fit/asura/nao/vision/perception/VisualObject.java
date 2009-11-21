/*
 * 作成日: 2008/04/24
 */
package jp.ac.fit.asura.nao.vision.perception;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2f;

import jp.ac.fit.asura.nao.vision.VisualObjects;
import jp.ac.fit.asura.nao.vision.perception.BlobVision.Blob;

/**
 * @author sey
 *
 * @version $Id: VisualObject.java 704 2008-10-23 17:25:51Z sey $
 *
 */
public abstract class VisualObject {
	protected VisualObjects type;

	private List<Blob> blobs;

	// 信頼度(?)
	public int confidence;

	// 画像の端に付いているか
	protected boolean isTopTouched;
	protected boolean isBottomTouched;
	protected boolean isLeftTouched;
	protected boolean isRightTouched;

	// 中心の座標(px)
	public Point2f center;

	// 画像中心からの角度(rad)
	public Point2f angle;

	// ロボット座標系での角度(rad)
	public Point2f robotAngle;

	// 画像上の領域(px)
	protected Rectangle area;

	public Polygon polygon;

	public VisualObject(VisualObjects type) {
		blobs = new ArrayList<Blob>();
		this.type = type;

		center = new Point2f();
		angle = new Point2f();
		robotAngle = new Point2f();
		area = new Rectangle();
		polygon = new Polygon();
	}

	public void clear() {
		blobs.clear();
		confidence = 0;
		polygon.reset();
	}

	public VisualObjects getType() {
		return type;
	}

	public List<Blob> getBlobs() {
		return blobs;
	}

	public boolean isTopTouched() {
		return isTopTouched;
	}

	public boolean isBottomTouched() {
		return isBottomTouched;
	}

	public boolean isLeftTouched() {
		return isLeftTouched;
	}

	public boolean isRightTouched() {
		return isRightTouched;
	}
}
