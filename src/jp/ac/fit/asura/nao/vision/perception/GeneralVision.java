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

import java.awt.geom.Point2D;

import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.objects.VisualObject;
import jp.ac.fit.asura.nao.vision.perception.BlobVision.Blob;

/**
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public class GeneralVision {
	private VisualContext context;

	public void checkTouched(VisualObject obj) {
		obj.setProperty(TopTouched, Boolean.FALSE);
		obj.setProperty(BottomTouched, Boolean.FALSE);
		obj.setProperty(RightTouched, Boolean.FALSE);
		obj.setProperty(LeftTouched, Boolean.FALSE);
	}

	public void calcAngle(VisualObject obj) {
		Point2D cp = obj.get(Point2D.class, Center);
		obj.setProperty(Angle, calculateAngle(cp));
	}

	public void calcCenter(VisualObject obj) {
		Blob blob = obj.getBlobs().next();
		Point2D.Double cp = new Point2D.Double();
		cp.x = (blob.xmin + blob.xmax) / 2 - context.width / 2;
		cp.y = (blob.ymin + blob.ymax) / 2 - context.height / 2;
		obj.setProperty(Center, cp);
	}

	public void calcConfidence(VisualObject obj) {
		int cf = 0;
		if (obj.getBlobs().hasNext()) {
			cf = obj.getBlobs().next().mass;
		}
		obj.setProperty(Confidence, Integer.valueOf(cf));
	}

	private Point2D calculateAngle(Point2D point) {
		double angle1 = Math.toDegrees(0.8);
		double angle2 = angle1 * context.height / context.width;

		double angleX = point.getX() / context.width * angle1;
		double angleY = point.getY() / context.height * angle2;
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
