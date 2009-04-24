/*
 * 作成日: 2008/06/18
 */
package jp.ac.fit.asura.nao.vision.perception;

import java.awt.Rectangle;

import javax.vecmath.Point2f;

import jp.ac.fit.asura.nao.misc.Coordinates;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.sensation.SomaticContext;
import jp.ac.fit.asura.nao.vision.perception.BlobVision.Blob;

/**
 * @author sey
 *
 * @version $Id: GeneralVision.java 704 2008-10-23 17:25:51Z sey $
 *
 */
public class GeneralVision extends AbstractVision {
	public void processObject(VisualObject obj) {
		calcCenter(obj);
		calcAngle(obj);
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
			if (blob.xmax == getContext().image.getWidth() - 1)
				obj.isRightTouched = true;
			if (blob.ymin == 0)
				obj.isTopTouched = true;
			if (blob.ymax == getContext().image.getHeight() - 1)
				obj.isBottomTouched = true;
		}
	}

	/**
	 * 引数で与えられたオブジェクトの中心位置のイメージ座標系での角度を求めます.
	 *
	 * @param obj
	 */
	private void calcAngle(VisualObject obj) {
		calculateImageAngle(obj.center, obj.angle);
		calculateRobotAngle(obj.angle, obj.robotAngle);
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
		Point2f cp = obj.center;
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
	private void calculateImageAngle(Point2f planePoint, Point2f imageAngle) {
		float hFov = getContext().camera.getHorizontalFieldOfView();
		float vFov = getContext().camera.getVerticalFieldOfView();

		//
		Coordinates.plane2imageCoord(getContext(), planePoint, imageAngle);

		imageAngle.x = imageAngle.x / getContext().image.getWidth() * hFov;
		imageAngle.y = imageAngle.y / getContext().image.getHeight() * vFov;
	}

	private void calculateRobotAngle(Point2f imageAngle, Point2f robotAngle) {
		SomaticContext sc = getMotionFrame().getSomaticContext();
		Coordinates.image2bodyAngle(sc, imageAngle, robotAngle);
		Coordinates.body2robotAngle(sc, robotAngle, robotAngle);
	}
}
