/*
 * 作成日: 2008/06/18
 */
package jp.ac.fit.asura.nao.vision.perception;

import java.awt.Polygon;
import java.awt.Rectangle;

import javax.vecmath.Point2f;

import jp.ac.fit.asura.nao.misc.Coordinates;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.sensation.SomaticContext;
import jp.ac.fit.asura.nao.vision.perception.BlobVision.Blob;

import org.apache.log4j.Logger;

/**
 * @author sey
 *
 * @version $Id: GeneralVision.java 704 2008-10-23 17:25:51Z sey $
 *
 */
public class GeneralVision extends AbstractVision {
	private static final Logger log = Logger.getLogger(GeneralVision.class);

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
			if (blob.ymax == getContext().image.getHeight() - 1
					|| blob.ymax == getContext().image.getHeight() - 2)
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
		log.trace(obj.angle + " : " + obj.robotAngle);
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
			// すごくやる気のないWebots対応.
			// schemeから定数を設定できるようにすべき.
			if (getVisualFrame().getImage().getWidth() == 320)
				cf = obj.getBlobs().iterator().next().mass * 4;
			else
				cf = obj.getBlobs().iterator().next().mass * 64;
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
		Blob first = obj.getBlobs().iterator().next();
		rect.x = first.xmin;
		rect.y = first.ymin;
		rect.width = first.xmax - first.xmin + 1;
		rect.height = first.ymax - first.ymin + 1;
		for (Blob blob : obj.getBlobs()) {
			int xmax = Math.max(rect.x + rect.width - 1, blob.xmax);
			int ymax = Math.max(rect.y + rect.height - 1, blob.ymax);
			rect.x = Math.min(rect.x, blob.xmin);
			rect.y = Math.min(rect.y, blob.ymin);
			rect.width = xmax - rect.x + 1;
			rect.height = ymax - rect.y + 1;
		}
	}

	/**
	 * VisualObjectを取り囲む多角形(Polygon)を計算する.
	 *
	 * 凸多角形(Convex hull)を計算するわけではないので注意.
	 *
	 * Objectの一部として判定するのは指定されたcolorのみ.
	 *
	 * @param obj
	 * @param color
	 */
	protected void calcBoundary(VisualObject obj, byte color) {
		Polygon p = obj.polygon;
		byte[] plane = getContext().gcdPlane;
		int xmin = obj.area.x;
		int xmax = obj.area.x + obj.area.width - 1;
		int ymin = obj.area.y;
		int ymax = obj.area.y + obj.area.height - 1;

		int w = getContext().image.getWidth();

		int begin = 0;

		// 上面 左, 右
		int yidx = ymin * w;
		for (int x = xmin; x <= xmax; x++)
			if (plane[yidx + x] == color) {
				p.addPoint(x, ymin);
				begin = x;
				break;
			}
		for (int x = xmax; x >= xmin; x--)
			if (plane[yidx + x] == color) {
				if (begin != x)
					p.addPoint(x, ymin);
				break;
			}

		// 右面 上, 下
		for (int i = ymin * w + xmax; i <= ymax * w + xmax; i += w)
			if (plane[i] == color) {
				p.addPoint(xmax, i / w);
				begin = i;
				break;
			}
		for (int i = ymax * w + xmax; i >= ymin * w + xmax; i -= w)
			if (plane[i] == color) {
				if (begin != i)
					p.addPoint(xmax, i / w);
				break;
			}

		// 下面 右, 左
		yidx = ymax * w;
		for (int x = xmax; x >= xmin; x--)
			if (plane[yidx + x] == color) {
				p.addPoint(x, ymax);
				begin = x;
				break;
			}
		for (int x = xmin; x <= xmax; x++)
			if (plane[yidx + x] == color) {
				if (begin != x)
					p.addPoint(x, ymax);
				break;
			}

		// 左面 下, 上
		for (int i = ymax * w + xmin; i >= ymin * w + xmin; i -= w)
			if (plane[i] == color) {
				p.addPoint(xmin, i / w);
				begin = i;
				break;
			}
		for (int i = ymin * w + xmin; i <= ymax * w + xmin; i += w)
			if (plane[i] == color) {
				if (begin != i)
					p.addPoint(xmin, i / w);
				break;
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
		Coordinates.image2cameraAngle(sc, imageAngle, robotAngle);
		Coordinates.camera2bodyAngle(sc, robotAngle, robotAngle);
		Coordinates.body2robotAngle(sc, robotAngle, robotAngle);
	}

}
