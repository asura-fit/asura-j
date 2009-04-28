/*
 * 作成日: 2008/06/18
 */
package jp.ac.fit.asura.nao.vision.perception;

import static jp.ac.fit.asura.nao.vision.GCD.cORANGE;

import java.util.List;
import java.util.Set;

import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.misc.Coordinates;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.physical.Ball;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.SomaticContext;
import jp.ac.fit.asura.nao.vision.VisualObjects;
import jp.ac.fit.asura.nao.vision.perception.BlobVision.Blob;

import org.apache.log4j.Logger;

/**
 * @author sey
 *
 * @version $Id: BallVision.java 717 2008-12-31 18:16:20Z sey $
 *
 */
public class BallVision extends AbstractVision {
	private Logger log = Logger.getLogger(BallVision.class);

	public void findBall() {
		List<Blob> blobs = getContext().blobVision.findBlobs(cORANGE, 10, 100);

		if (!blobs.isEmpty()) {
			log.debug("Ball blob found." + blobs.get(0));
			BallVisualObject ball = (BallVisualObject) getContext().get(
					VisualObjects.Ball);
			ball.clear();
			// for (Blob blob : blobs)
			// ball.addBlob(blob);
			ball.getBlobs().add(blobs.get(0));

			getContext().generalVision.processObject(ball);
			//calculateDistance(ball);
			calculateDistanceByBlobSize(ball);

			checkRobotAngle(ball);
		}
	}

	private void calculateDistance(BallVisualObject ball) {
		SomaticContext sc = getMotionFrame().getSomaticContext();
		Point2f angle = ball.angle;

		// 姿勢が当てにならない
		if (sc.getConfidence() < 100) {
			// log.debug("Invalid posture. set confidence to 0.");
			// obj.confidence = 0;
			// return;
		}

		// image座標系の角度から長さ未知数(100mmとする)とする極座標ベクトルをつくる.
		// ボールはこのベクトルの延長線上にある.
		// カメラの高さを求める
		Vector3f cameraPos = new Vector3f();
		Coordinates.body2robotCoord(sc,
				sc.get(Frames.NaoCam).getBodyPosition(), cameraPos);

		log.trace("cameraPos:" + cameraPos);

		// カメラ座標系での距離を計算
		float dist = (cameraPos.y - Ball.Radius)
				/ (float) Math.tan(-ball.robotAngle.y);

		// **ここから先はLocalizationで実装するべき**
		// 面倒なので，BallのDistanceとRobotAngleはロボット座標系での距離と角度を返すようになっている．

		// 求めた距離をつかって，カメラ座標系からみたボールの位置を極座標表示
		Vector3f polar = new Vector3f(ball.robotAngle.x, ball.robotAngle.y,
				dist);

		log.trace("dist in image coord: " + polar.z);

		Vector3f robotPosition = new Vector3f();
		// ロボット座標系に変換しておわり.
		Coordinates.polar2carthesian(polar, robotPosition);
		log.trace("carthesian: " + robotPosition);
		Coordinates.image2cameraCoord(robotPosition, robotPosition);
		log.trace("i2c: " + robotPosition);
		Coordinates
				.toBodyCoord(sc, Frames.NaoCam, robotPosition, robotPosition);
		log.trace("c2d: " + robotPosition);
		Coordinates.body2robotCoord(sc, robotPosition, robotPosition);

		int d = (int) Math.sqrt(MathUtils.square(robotPosition.x)
				+ MathUtils.square(robotPosition.z));

		float h = MathUtils.normalizeAnglePI((float) (Math.atan2(
				robotPosition.z, robotPosition.x) - Math.PI / 2));

		log.debug(ball + " d:" + d + " h:" + Math.toDegrees(h));

		// 後ろのボールがみえたらおかしい
		if (Math.abs(h) > 2.5f) {
			d = 100;
			h = 0;
		}

		ball.distanceUsable = true;
		ball.distance = d;
		ball.robotPosition.set(robotPosition);
	}

	/*
	 * blobの大きさから距離を求める。
	 * ここでいう距離とは、カメラからボールまでの直線距離
	 * !注意! 今のところ、正方形のみ！
	 *
	 * キャリブレの結果、以下の式で距離を求めてる。
	 * f(x) = a / (x+b) + c
	 * a = 34293.2
	 * b = 2.55062
	 * c = -67.2262
	 */
	private void calculateDistanceByBlobSize(BallVisualObject ball) {
		int size = 30;
		Set<Blob> blobs =  ball.getBlobs();
		float dist  = 0;

		for (Blob b : blobs) {
			int t = b.xmax - b.xmin;
			if (t < b.ymax - b.ymin)
				t = b.ymax - b.ymin;

			log.debug("BallVision: t=" + t);
			if (size < t)
				size = t;

			log.debug("BallVision: size=" + size);
		}

		dist = Math.round(34293.2f / (size + 2.55062f) -67.2262f);


	}

	private void checkRobotAngle(BallVisualObject ball) {
		// log.trace("ImageAngle x:" + MathUtils.toDegrees(ball.angle.x) + " y:"
		// + MathUtils.toDegrees(ball.angle.y));
		log.trace("WorldAngle x:" + MathUtils.toDegrees(ball.robotAngle.x)
				+ " y:" + MathUtils.toDegrees(ball.robotAngle.y));

		SomaticContext sc = getMotionFrame().getSomaticContext();
		Point2f wa = new Point2f();
		Coordinates.image2bodyAngle(sc, ball.angle, wa);
		Coordinates.body2robotAngle(sc, wa, wa);
		if (wa.y > 0) {
			ball.confidence = 0;
		}

		// wa = new Point2f();
		// Coordinates.body2robotAngle(sc, wa, wa);
		// log.info("WorldAngle x:" + MathUtils.toDegrees(wa.x) + " y:"
		// + MathUtils.toDegrees(wa.y));

	}
}
