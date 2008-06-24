/*
 * 作成日: 2008/06/18
 */
package jp.ac.fit.asura.nao.vision.perception;

import static jp.ac.fit.asura.nao.vision.GCD.cORANGE;
import static jp.ac.fit.asura.nao.vision.VisualObjects.Ball;
import static jp.ac.fit.asura.nao.vision.VisualObjects.Properties.Angle;
import static jp.ac.fit.asura.nao.vision.VisualObjects.Properties.Distance;

import java.awt.geom.Point2D;
import java.util.List;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.misc.PhysicalConstants;
import jp.ac.fit.asura.nao.misc.PhysicalConstants.Nao;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.objects.BallVisualObject;
import jp.ac.fit.asura.nao.vision.objects.VisualObject;
import jp.ac.fit.asura.nao.vision.perception.BlobVision.Blob;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class BallVision {
	private VisualContext context;

	public void findBall() {
		List<Blob> blobs = context.blobVision.findBlobs(cORANGE, 10, 10);

		if (!blobs.isEmpty()) {
			VisualObject ball = context.objects.get(Ball);
			// for (Blob blob : blobs)
			// ball.addBlob(blob);
			ball.getBlobs().add(blobs.get(0));
			ball.clearCache();
		}
	}

	public void calculateDistance(BallVisualObject obj) {
		Point2D angle = obj.get(Point2D.class, Angle);

		float hp = context.getSuperContext().getSensor().getJoint(
				Joint.HeadPitch);
		double ballElev = angle.getY() - hp;
		double dist = (Nao.CameraHeight - PhysicalConstants.Ball.Radius)
				/ Math.tan(-ballElev);
		obj.setProperty(Distance, Integer.valueOf((int) dist));
	}

	/**
	 * @param context
	 *            the context to set
	 */
	public void setContext(VisualContext context) {
		this.context = context;
	}
}
