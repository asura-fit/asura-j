/*
 * 作成日: 2008/06/18
 */
package jp.ac.fit.asura.nao.vision.perception;

import static jp.ac.fit.asura.nao.vision.VisualObjects.Properties.Angle;
import static jp.ac.fit.asura.nao.vision.VisualObjects.Properties.BottomTouched;
import static jp.ac.fit.asura.nao.vision.VisualObjects.Properties.Center;
import static jp.ac.fit.asura.nao.vision.VisualObjects.Properties.Confidence;
import static jp.ac.fit.asura.nao.vision.VisualObjects.Properties.LeftTouched;
import static jp.ac.fit.asura.nao.vision.VisualObjects.Properties.RightTouched;
import static jp.ac.fit.asura.nao.vision.VisualObjects.Properties.TopTouched;

import java.awt.Rectangle;
import java.awt.geom.Point2D;

import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualObjects.Properties;
import jp.ac.fit.asura.nao.vision.objects.VisualObject;
import jp.ac.fit.asura.nao.vision.perception.BlobVision.Blob;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class GeneralVision {
	private VisualContext context;

	public void checkTouched(VisualObject obj) {
		obj.setProperty(TopTouched, Boolean.FALSE);
		obj.setProperty(BottomTouched, Boolean.FALSE);
		obj.setProperty(RightTouched, Boolean.FALSE);
		obj.setProperty(LeftTouched, Boolean.FALSE);
		for (Blob blob : obj.getBlobs()) {
			if (blob.xmin == 0)
				obj.setProperty(LeftTouched, Boolean.TRUE);
			if (blob.xmax == context.camera.width - 1)
				obj.setProperty(RightTouched, Boolean.TRUE);
			if (blob.ymin == 0)
				obj.setProperty(TopTouched, Boolean.TRUE);
			if (blob.ymax == context.camera.height - 1)
				obj.setProperty(BottomTouched, Boolean.TRUE);
		}
	}

	public void calcAngle(VisualObject obj) {
		Point2D cp = obj.get(Point2D.class, Center);
		obj.setProperty(Angle, calculateAngle(cp));
	}

	public void calcCenter(VisualObject obj) {
		Blob blob = obj.getBlobs().iterator().next();
		Point2D.Double cp = new Point2D.Double();
		cp.x = (blob.xmin + blob.xmax) / 2;
		cp.y = (blob.ymin + blob.ymax) / 2;
		obj.setProperty(Center, cp);
	}

	public void calcConfidence(VisualObject obj) {
		int cf = 0;
		if (!obj.getBlobs().isEmpty()) {
			cf = obj.getBlobs().iterator().next().mass * 4;
			cf = MathUtils.clipping(cf, 0, 1000);
			if (obj.getBoolean(Properties.LeftTouched))
				cf /= 2;
			if (obj.getBoolean(Properties.RightTouched))
				cf /= 2;
			if (obj.getBoolean(Properties.TopTouched))
				cf /= 2;
			if (obj.getBoolean(Properties.BottomTouched))
				cf /= 2;
		}
		obj.setProperty(Confidence, Integer.valueOf(cf));
	}

	public void calcArea(VisualObject obj) {
		Rectangle rect = new Rectangle();
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
		obj.setProperty(Properties.Area, rect);
	}

	private Point2D calculateAngle(Point2D point) {
		double hFov = context.camera.horizontalFieldOfView;
		double vFov = context.camera.verticalFieldOfView;

		double rx = point.getX() - context.camera.width / 2;
		double ry = point.getY() - context.camera.height / 2;

		double angleX = rx / context.camera.width * hFov;
		double angleY = ry / context.camera.height * vFov;
		return new Point2D.Double(angleX, angleY);
	}

	/**
	 * @param context
	 *            the context to set
	 */
	public void setContext(VisualContext context) {
		this.context = context;
	}
}
