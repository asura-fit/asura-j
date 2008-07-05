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

import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.misc.Coordinates;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.misc.PhysicalConstants;
import jp.ac.fit.asura.nao.sensation.SomatoSensoryCortex;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualObjects;
import jp.ac.fit.asura.nao.vision.VisualObjects.Properties;
import jp.ac.fit.asura.nao.vision.objects.BallVisualObject;
import jp.ac.fit.asura.nao.vision.objects.VisualObject;
import jp.ac.fit.asura.nao.vision.perception.BlobVision.Blob;

import org.apache.log4j.Logger;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class BallVision {
	private Logger log = Logger.getLogger(BallVision.class);
	private VisualContext context;

	public void findBall() {
		List<Blob> blobs = context.blobVision.findBlobs(cORANGE, 10, 25);

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

		SomatoSensoryCortex ssc = context.getSuperContext().getSensoryCortex();

		// まず接地座標系でのカメラの位置を求める
		Vector3f camera = ssc.getCameraPosition(new Vector3f());

		// カメラ座標系で長さを1とするボールの方向への極座標ベクトルをつくり，直交座標に変換
		Vector3f carthesian = new Vector3f();

		Coordinates.polar2carthesian(new Vector3f((float) angle.getX(),
				(float) angle.getY(), 100.0f), carthesian);

		// 接地座標系でのそのベクトルの位置を求める
		Vector3f ballAngle = ssc.getCameraPosition(carthesian);

		// 接地座標系での，カメラの位置からボール方向を求める. このベクトルの延長線上にボールがあるはず.
		ballAngle.sub(camera);

		// ベクトルの傾きを求める.
		double rad = Math.atan2(ballAngle.x, ballAngle.z);
		double nz = Math.sin(rad) * ballAngle.x + Math.cos(rad) * ballAngle.z;

		double ballElev = Math.atan2(nz, ballAngle.getY());

		// カメラ座標系のx-z平面での距離を計算
		double dist = (camera.y - PhysicalConstants.Ball.Radius)
				* Math.tan(ballElev);

		// **ここから先はLocalizationで実装するべき**
		// 面倒なので，BallのDistanceとRobotAngleはロボット座標系での距離と角度を返すようになっている．

		// 求めた距離をつかって，カメラ座標系からみたボールの位置を極座標表示
		Coordinates.polar2carthesian(new Vector3f((float) angle.getX(),
				(float) angle.getY(), (float) (dist / Math.sin(ballElev))),
				carthesian);
		
		// 接地座標系に変換
		Vector3f ball = ssc.getCameraPosition(carthesian);

		// ロボット座標系に変換しておわり.
		int d = (int) Math.sqrt(MathUtils.square(ball.x)
				+ MathUtils.square(ball.z));
		float h = (float) (Math.atan2(ball.z, ball.x) - Math.PI / 2);

		log.debug(ball + " d:" + d + " h:" + Math.toDegrees(h) + " elev:"
				+ Math.toDegrees(ballElev));

		obj.setProperty(Distance, Integer.valueOf(d));
		obj.setProperty(Properties.RobotAngle, Float.valueOf(h));
		obj.setProperty(Properties.Position, ball);
	}

	/**
	 * @param context
	 *            the context to set
	 */
	public void setContext(VisualContext context) {
		this.context = context;
	}
}
