/*
 * 作成日: 2008/06/18
 */
package jp.ac.fit.asura.nao.vision.perception;

import static jp.ac.fit.asura.nao.vision.GCD.cORANGE;

import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.misc.Coordinates;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.physical.Ball;
import jp.ac.fit.asura.nao.physical.Nao.Frames;
import jp.ac.fit.asura.nao.sensation.SomaticContext;
import jp.ac.fit.asura.nao.sensation.SomatoSensoryCortex;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualObjects;
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
			BallVisualObject ball = (BallVisualObject) context
					.get(VisualObjects.Ball);
			ball.clear();
			// for (Blob blob : blobs)
			// ball.addBlob(blob);
			ball.getBlobs().add(blobs.get(0));

			context.generalVision.processObject(ball);
			calculateDistance(ball);
		}
	}

	private void calculateDistance(BallVisualObject obj) {
		SomatoSensoryCortex ssc = context.getSuperContext().getSensoryCortex();
		SomaticContext ctx = ssc.getContext();
		Point2d angle = obj.angle;

		// 姿勢が当てにならない
		if (ssc.getConfidence() < 100) {
			obj.confidence = 0;
			return;
		}

		// image座標系の角度から長さ未知数(100mmとする)とする極座標ベクトルをつくる.
		// ボールはこのベクトルの延長線上にある.
		Vector3f polar = new Vector3f((float) angle.x, (float) angle.y, 100.0f);
		log.trace("polar(Camera):" + polar);

		// 
		// 直交座標に変換
		Vector3f ballAngle = new Vector3f();
		Coordinates.polar2carthesian(polar, ballAngle);

		// Image座標系からロボット座標系へ変換する(回転だけ).
		Coordinates.image2cameraCoord(ballAngle, ballAngle);
		log.trace("ballAngle(Camera):" + ballAngle);
		Coordinates.toBodyRotation(ctx, Frames.Camera, ballAngle, ballAngle);
		log.trace("ballAngle(Body):" + ballAngle);

		ssc.calculateBodyRotation().transform(ballAngle);

		// ballAngle:ロボット座標系でのボールの方向(ベクトル)
		// 具体的な角度を求める
		// x-z平面で回転させてから、z-y平面の角度を求める
		double rad = Math.atan2(ballAngle.x, ballAngle.z);
		double nz = Math.sin(rad) * ballAngle.x + Math.cos(rad) * ballAngle.z;

		double ballElev = Math.atan2(nz, -ballAngle.y);

		log.trace("ballAngle:" + ballAngle + " nz:" + nz);

		// カメラの高さを求める
		Vector3f cameraPos = new Vector3f();
		ssc.body2robotCoord(ctx.get(Frames.Camera).getRobotPosition(),
				cameraPos);

		log.trace("cameraPos:" + cameraPos);

		// カメラ座標系のx-z平面での距離を計算
		double dist = (cameraPos.y - Ball.Radius) * Math.tan(ballElev);

		// **ここから先はLocalizationで実装するべき**
		// 面倒なので，BallのDistanceとRobotAngleはロボット座標系での距離と角度を返すようになっている．

		// 求めた距離をつかって，カメラ座標系からみたボールの位置を極座標表示
		polar.z = (float) (dist / Math.sin(ballElev));

		log.trace("dist in image coord: " + polar.z);

		// ロボット座標系に変換しておわり.
		Vector3f ball = new Vector3f();
		Coordinates.polar2carthesian(polar, ball);
		log.trace("carthesian: " + ball);
		Coordinates.image2cameraCoord(ball, ball);
		log.trace("i2c: " + ball);
		Coordinates.camera2bodyCoord(ctx, ball);
		log.trace("c2d: " + ball);
		ssc.body2robotCoord(ball, ball);

		int d = (int) Math.sqrt(MathUtils.square(ball.x)
				+ MathUtils.square(ball.z));

		float h = MathUtils.normalizeAnglePI((float) (Math
				.atan2(ball.z, ball.x) - Math.PI / 2));

		log.debug(ball + " d:" + d + " h:" + Math.toDegrees(h) + " elev:"
				+ Math.toDegrees(ballElev));

		// 後ろのボールがみえたらおかしい
		if (Math.abs(h) > 2.5f) {
			d = 100;
			h = 0;
		}

		obj.distanceUsable = true;
		obj.distance = d;
		obj.robotAngle = h;
		obj.robotPosition.set(ball);
	}

	/**
	 * @param context
	 *            the context to set
	 */
	public void setContext(VisualContext context) {
		this.context = context;
	}
}
