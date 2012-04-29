/*
 * 作成日: 2008/06/18
 */
package jp.ac.fit.asura.nao.vision.perception;

import static jp.ac.fit.asura.nao.vision.GCD.cORANGE;

import java.util.List;

import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.Camera;
import jp.ac.fit.asura.nao.Context;
import jp.ac.fit.asura.nao.Camera.CameraID;
import jp.ac.fit.asura.nao.misc.Coordinates;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.physical.Ball;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.SomaticContext;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualObjects;
import jp.ac.fit.asura.nao.vision.VisualParam.Float;
import jp.ac.fit.asura.nao.vision.VisualParam.Int;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
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
	private VisualContext context;
	
	public void init(){
		context = getContext();
	}
	
	
	

	public void findBall() {
		int threshold = getContext().getParam(Int.BALL_BLOB_THRESHOLD);
		List<Blob> blobs = getContext().blobVision.findBlobs(cORANGE, 10,
				threshold);

		// 編集中↓ 編集： aqua & hachi
		BallVisualObject ball = (BallVisualObject) getContext().get(
				VisualObjects.Ball);

		int i = 0;
		while (i <= 2) {

			if (!blobs.isEmpty()) {
				log.debug("Ball blob found." + blobs.get(i));

				ball.clear();
				ball.getBlobs().add(blobs.get(i));

				getContext().generalVision.processObject(ball);

				if (!checkCameraedge(ball))
					if (!checkRobotAngle(ball))
						if (!checkBlobSize(ball))
							if (!checkBlobCompare(ball))
								break;

				ball.confidence = 0;

				i++;

				if (blobs.size() <= i)
					break;

			} else {
				break;
			}
		}

		float dist;
		// カメラからボールの距離を計算する. どちらか選んで.
		// 角度ベースの距離計算
		// dist = calculateCameraDistanceByAngle(ball);
		// 画像の大きさベースの距離計算.
		dist = calculateCameraDistanceBySize(ball);
		calculateDistance(ball, dist);
	}

	private float calculateCameraDistanceByAngle(BallVisualObject ball) {
		SomaticContext sc = getMotionFrame().getSomaticContext();

		// 姿勢が当てにならない
		if (sc.getConfidence() < 100) {
//			log.debug("Invalid posture. set confidence to 0.");
			// obj.confidence = 0;
			// return;
		}

		// image座標系の角度から長さ未知数(100mmとする)とする極座標ベクトルをつくる.
		// ボールはこのベクトルの延長線上にある.
		// カメラの高さを求める
		Vector3f cameraPos = new Vector3f();
		Coordinates.body2robotCoord(sc,
				sc.get(Frames.NaoCam).getBodyPosition(), cameraPos);
		cameraPos.y += sc.getBodyHeight();
		// log.trace("cameraPos:" + cameraPos);

		// カメラ座標系での距離を計算
		return (cameraPos.y - Ball.Radius)
				/ (float) Math.sin(-ball.robotAngle.y);
	}

	/**
	 * blobの大きさから距離を求める。
	 *
	 * ここでいう距離とは、カメラからボールまでの直線距離 !注意! 今のところ、正方形のみ！
	 *
	 * 以下の式で距離を求めてる。
	 *
	 * f(x) = a / (x) + b, a = BALL_DIST_CALIBa, b = BALL_DIST_CALIBb
	 */
	private float calculateCameraDistanceBySize(BallVisualObject ball) {
		int size = getBlobSize(ball);
		float a = getContext().getParam(Float.BALL_DIST_CALIBa);
		float b = getContext().getParam(Float.BALL_DIST_CALIBb);
		return a / size + b;
	}

	/**
	 * blobの大きさを返す<br>
	 * 画面端で切れていたらとりあえず長い方をblobの大きさにする<br>
	 * TODO: 将来的には、切れていても形から本来の大きさを予測したいところ
	 *
	 * @param ball
	 * @return blobSize
	 */
	private int getBlobSize(BallVisualObject ball) {
		int size = 0;

		if (ball.isBottomTouched || ball.isTopTouched)
			size = ball.area.width;
		else if (ball.isLeftTouched || ball.isRightTouched)
			size = ball.area.height;
		else {
			size = ball.area.width;
			if (size < ball.area.height)
				size = ball.area.height;
		}
		log.debug("BallVision: size=" + size);

		return size;
	}

	private void calculateDistance(BallVisualObject ball, float cameraDist) {
		SomaticContext sc = getMotionFrame().getSomaticContext();

		// 求めた距離をつかって，カメラ座標系からみたボールの位置を極座標表示
		Vector3f robotPosition = new Vector3f();
		Coordinates.angle2carthesian(cameraDist, ball.angle, robotPosition);

		log.trace("angle in image coord: " + ball.angle);
		log.trace("angle in robot coord: " + ball.robotAngle);
		log.trace("dist: " + cameraDist);

		// ロボット座標系に変換しておわり.
		log.trace("pos in image coord: " + robotPosition);
		Coordinates.image2cameraCoord(robotPosition, robotPosition);
		log.trace("pos in camera coord: " + robotPosition);
		Coordinates
				.toBodyCoord(sc, Frames.NaoCam, robotPosition, robotPosition);
		log.trace("pos in body coord: " + robotPosition);
		Coordinates.body2robotCoord(sc, robotPosition, robotPosition);
		log.trace("pos in robot coord: " + robotPosition);

		int d = (int) Math.sqrt(MathUtils.square(robotPosition.x)
				+ MathUtils.square(robotPosition.z));

		float h = MathUtils.normalizeAnglePI((float) Math.atan2(
				robotPosition.x, robotPosition.z));

		log.debug("d:" + d + " h:" + Math.toDegrees(h));

		// 後ろのボールがみえたらおかしい
		if (Math.abs(h) > MathUtils.PIf) {
			ball.confidence = 0;
		}

		ball.distanceUsable = true;
		ball.distance = d;
		ball.robotPosition.set(robotPosition);
	}

	/**
	 * 画面端に映った時に縦横比で切れる前にボールとして認識させる。
	 * 大きさ、高さ、縦横比を計算する前にやっておく。
	 * この条件がない場合画面端に映ったものを認識しないときがある。
	 *
	 * @param ball
	 * @return
	 */
	private boolean checkCameraedge(BallVisualObject ball) {
		if (ball.isBottomTouched || ball.isTopTouched || ball.isLeftTouched
				|| ball.isRightTouched) {
		}
		return false;
	}

	/**
	 * ある高さになると切る。
	 * ｈｙ。ボールは高いところにない。naoの顔の少し上ぐらいで切れるようになっている。
	 *
	 * @param ball
	 * @return
	 */
//	private boolean checkRobotAngle(BallVisualObject ball) {
//		if (ball.robotAngle.y && -0.15f || ball.robotAngle.y) {
//			log.info("Ball sanity too high angle." + ball.robotAngle.y);
//			return true;
//		}
//		return false;
//	}

	
	private boolean checkRobotAngle(BallVisualObject ball) {
		context = getContext();
		Camera cam =context.getSuperContext().getCamera();
		if (cam.getSelectedId() == CameraID.TOP) {
			if (ball.robotAngle.y > 5.0f) {
				log.debug("Ball sanity too high angle TOP." + ball.robotAngle.y);
				return true;
			}
		}
			return false;
		}

//		if (cam.getSelectedId() == CameraID.BOTTOM)
//			if (ball.robotAngle.y > -0.15f) {
//				log.info("Ball sanity too high angle BOTTOM." + ball.robotAngle.y);
//				return true;
//			}
//			return false;
//	}
	/**
	 * ボールのサイズで切る。ある大きさより大きかった場合切る。
	 * 見え方によって状況は変わる。色きりの重要性。
	 * 小さいものでボールの色に見えた場合ボールとして認識してしまうので注意。
	 *
	 * @param ball
	 * @return
	 */
	private boolean checkBlobSize(BallVisualObject ball) {
		if (getBlobSize(ball) > 70) {
			log.debug("Ball sanity too big." + getBlobSize(ball));
			return true;
		}
		return false;
	}

	/**
	 *
	 * 縦横比を計算する。
	 * ボールは縦長く細長くないから。色の見え方、位置によって変化する。
	 *
	 *
	 * @author hachi & aqua
	 * @param ball
	 * @return
	 */
	private boolean checkBlobCompare(BallVisualObject ball) {
//		context = getContext();
//		Camera cam =context.getSuperContext().getCamera();
//		if (cam.getSelectedId() == CameraID.TOP){
		float h;
		float w;
		float proportion;
		h = (float) ball.area.height;
		w = (float) ball.area.width;
		if (h > w) {
			proportion = w / h;
		} else {
			proportion = h / w;
		}
		// float x;←↓意味ない。変更済み。
		// x = (float)ball.area.height / ball.area.width;
		if (getContext().getParam(Float.BALL_COMPARE) > proportion) {
			log.debug("Ball sanity unblance." + proportion);
			return true;
		}
//		}
		return false;
	}
	
}
