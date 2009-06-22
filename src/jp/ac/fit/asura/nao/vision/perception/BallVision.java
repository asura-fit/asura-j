/*
 * 作成日: 2008/06/18
 */
package jp.ac.fit.asura.nao.vision.perception;

import static jp.ac.fit.asura.nao.vision.GCD.cORANGE;

import java.util.List;

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
		int threshold = 100;
		// すごくやる気のないWebots対応.
		// schemeから定数を設定できるようにすべき.
		if (getVisualFrame().getImage().getWidth() == 160)
			threshold = 10;
		List<Blob> blobs = getContext().blobVision.findBlobs(cORANGE, 10,
				threshold);

		if (!blobs.isEmpty()) {
			log.debug("Ball blob found." + blobs.get(0));
			BallVisualObject ball = (BallVisualObject) getContext().get(
					VisualObjects.Ball);
			ball.clear();
			// for (Blob blob : blobs)
			// ball.addBlob(blob);
			ball.getBlobs().add(blobs.get(0));

			getContext().generalVision.processObject(ball);
			float dist;
			// カメラからボールの距離を計算する. どちらか選んで.
			// 角度ベースの距離計算
			// dist = calculateCameraDistanceByAngle(ball);
			// 画像の大きさベースの距離計算
			dist = calculateCameraDistanceBySize(ball);
			calculateDistance(ball, dist);

			checkRobotAngle(ball);
		}
	}

	private float calculateCameraDistanceByAngle(BallVisualObject ball) {
		SomaticContext sc = getMotionFrame().getSomaticContext();

		// 姿勢が当てにならない
		if (sc.getConfidence() < 100) {
			log.debug("Invalid posture. set confidence to 0.");
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

	/*
	 * blobの大きさから距離を求める。 ここでいう距離とは、カメラからボールまでの直線距離 !注意! 今のところ、正方形のみ！
	 *
	 * キャリブレの結果、以下の式で距離を求めてる。 f(x) = a / (x+b) + c a = 34293.2 b = 2.55062 c =
	 * -67.2262
	 */
	private float calculateCameraDistanceBySize(BallVisualObject ball) {
		int size = getBlobSize(ball);
		// すごくやる気のないWebots対応.
		// schemeから定数を設定できるようにすべき.
		if (getVisualFrame().getImage().getWidth() == 320)
			return 34293.2f / (size + 2.55062f) - 67.2262f;
		else
			return 34293.2f / (size * 2 + 2.55062f) - 67.2262f;
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

	private void checkRobotAngle(BallVisualObject ball) {
		if (ball.robotAngle.y > -0.15f) {
			log.debug("Ball sanity too high angle." + ball.robotAngle.y);
			ball.confidence = 0;
		}
	}
}
