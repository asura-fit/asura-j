/*
 * 作成日: 2008/06/18
 */
package jp.ac.fit.asura.nao.vision.perception;

import java.awt.Rectangle;

import javax.vecmath.Point2d;

import jp.ac.fit.asura.nao.misc.Coordinates;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.perception.BlobVision.Blob;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class GeneralVision {
	private VisualContext context;

	public void processObject(VisualObject obj) {
		calcCenter(obj);
		calcImageAngle(obj);
		calcArea(obj);
		checkTouched(obj);
		calcConfidence(obj);
	}

	private void checkTouched(VisualObject obj) {
		obj.isTopTouched = false;
		obj.isBottomTouched = false;
		obj.isLeftTouched = false;
		obj.isRightTouched = false;
		for (Blob blob : obj.getBlobs()) {
			if (blob.xmin == 0)
				obj.isLeftTouched = true;
			if (blob.xmax == context.camera.width - 1)
				obj.isRightTouched = true;
			if (blob.ymin == 0)
				obj.isTopTouched = true;
			if (blob.ymax == context.camera.height - 1)
				obj.isBottomTouched = true;
		}
	}

	/**
	 * 引数で与えられたオブジェクトの中心位置のイメージ座標系での角度を求めます.
	 * 
	 * @param obj
	 */
	private void calcImageAngle(VisualObject obj) {
		calculateImageAngle(obj.center, obj.angle);
	}

	/**
	 * 引数で与えられたオブジェクトの中心位置を求めます.
	 * 
	 * この実装は単純にblobの中心を求めています.
	 * 
	 * @param obj
	 */
	private void calcCenter(VisualObject obj) {
		Blob blob = obj.getBlobs().iterator().next();
		Point2d cp = obj.center;
		cp.x = (blob.xmin + blob.xmax) / 2;
		cp.y = (blob.ymin + blob.ymax) / 2;
	}

	private void calcConfidence(VisualObject obj) {
		int cf = 0;
		if (!obj.getBlobs().isEmpty()) {
			cf = obj.getBlobs().iterator().next().mass * 4;
			cf = MathUtils.clipping(cf, 0, 1000);
			if (obj.isLeftTouched())
				cf /= 2;
			if (obj.isRightTouched())
				cf /= 2;
			if (obj.isTopTouched())
				cf /= 2;
			if (obj.isBottomTouched())
				cf /= 2;
		}
		obj.confidence = cf;
	}

	private void calcArea(VisualObject obj) {
		Rectangle rect = obj.area;
		boolean first = true;
		for (Blob blob : obj.getBlobs()) {
			if (first) {
				rect.x = blob.xmin;
				rect.y = blob.ymin;
				rect.width = blob.xmax - rect.x + 1;
				rect.height = blob.ymax - rect.y + 1;
				first = false;
			} else {
				rect.x = Math.min(rect.x, blob.xmin);
				rect.y = Math.min(rect.y, blob.ymin);
				rect.width = Math.max(rect.width, blob.xmax - rect.x + 1);
				rect.height = Math.max(rect.height, blob.ymax - rect.y + 1);
			}
		}
	}

	/**
	 * 与えられた点point(画像平面座標系)の、イメージ座標系(image)での角度を返します.
	 * 
	 * 返される角度は、画像平面座標系(plane)ではないことに注意してください.
	 * 
	 * @param planePoint
	 * @param imageAngle
	 */
	private void calculateImageAngle(Point2d planePoint, Point2d imageAngle) {
		double hFov = context.camera.horizontalFieldOfView;
		double vFov = context.camera.verticalFieldOfView;

		//
		Coordinates.plane2imageCoord(context, planePoint, imageAngle);

		imageAngle.x = imageAngle.x / context.camera.width * hFov;
		imageAngle.y = imageAngle.y / context.camera.height * vFov;
	}

	/**
	 * @param context
	 *            the context to set
	 */
	public void setContext(VisualContext context) {
		this.context = context;
	}
}
