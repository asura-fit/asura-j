/*
 * 作成日: 2008/06/18
 */
package jp.ac.fit.asura.nao.vision.perception;

import static jp.ac.fit.asura.nao.vision.GCD.cCYAN;
import static jp.ac.fit.asura.nao.vision.GCD.cYELLOW;
import static jp.ac.fit.asura.nao.vision.VisualObjects.BlueGoal;
import static jp.ac.fit.asura.nao.vision.VisualObjects.YellowGoal;

import java.awt.Rectangle;
import java.util.List;

import jp.ac.fit.asura.nao.physical.Field;
import jp.ac.fit.asura.nao.vision.VisualParam.Float;
import jp.ac.fit.asura.nao.vision.VisualParam.Int;
import jp.ac.fit.asura.nao.vision.perception.BlobVision.Blob;

import org.apache.log4j.Logger;

/**
 * @author sey
 *
 * @version $Id: GoalVision.java 704 2008-10-23 17:25:51Z sey $
 *
 */
public class GoalVision extends AbstractVision {
	private Logger log = Logger.getLogger(GoalVision.class);

	public void findYellowGoal() {
		VisualObject goal = getContext().get(YellowGoal);
		findGoal((GoalVisualObject) goal, cYELLOW);
	}

	public void findBlueGoal() {
		VisualObject goal = getContext().get(BlueGoal);
		findGoal((GoalVisualObject) goal, cCYAN);
	}

	private void findGoal(GoalVisualObject vo, byte color) {
		int threshold = getContext().getParam(Int.GOAL_BLOB_THRESHOLD);
		List<Blob> blobs = getContext().blobVision.findBlobs(color, 10,
				threshold);
		List<Blob> set = vo.getBlobs();

		for (Blob blob : blobs) {
			set.add(blob);
		}

		if (!blobs.isEmpty()) {
			getContext().generalVision.processObject(vo);
			getContext().generalVision.calcBoundary(vo, color);
			calculateDistance(vo);
			checkRobotAngle(vo);
		}
	}

	private void calculateDistance(GoalVisualObject vo) {
		vo.isLeftPost = false;
		vo.isRightPost = false;
		Rectangle area = vo.area;
		int dist = -1;

		if (!vo.isLeftTouched() && !vo.isRightTouched()) {
			// 左右で切れていない
			if (!vo.isTopTouched() && !vo.isBottomTouched()) {
				// どこも切れていない
				log.debug("no touched.");
				dist = calcDistWithHeight(vo);

			} else if ((vo.isTopTouched() && !vo.isBottomTouched())
					|| (!vo.isTopTouched() && vo.isBottomTouched())) {
				// 上もしくは下だけ切れてる
				log.debug("top or bottom touched.");
				if (vo.getBlobs().size() == 1) {
					int mass = 0;
					for (Blob b : vo.getBlobs()) {
						mass += b.mass;
					}
					if ((float) mass / (area.width * area.height) > 0.4) {
						dist = calcDistWithPole(vo);
						vo.isLeftPost = true;
						vo.isRightPost = true;
					} else {
						dist = calcDistWithWidth(vo);
					}
				} else if (vo.getBlobs().size() > 1
						&& (float) (area.width / area.height) > 1.5f) {
					// blob二つ以上で構成されていて横長なら両方のポールがみえてるはず
					dist = calcDistWithWidth(vo);
				}
			} else {
				// 上下が切れている
				log.debug("top and bottom touched.");
				if (vo.getBlobs().size() == 1
						&& (float) area.height / area.width > 1.5f) {
					// たぶん1つのポール
					dist = calcDistWithPole(vo);
					vo.isLeftPost = true;
					vo.isRightPost = true;
				}
			}
		} else {
			// 右か左で切れている
			log.debug("left or right touched.");
			if ((vo.isTopTouched() && !vo.isBottomTouched())
					|| (!vo.isTopTouched() && vo.isBottomTouched())) {
				// 上か下だけ切れている
				log.debug("top or bottom touched");
				if ((float) area.width / area.height > 1.5f) {
					// 横長であれば高さから
					dist = calcDistWithHeight(vo);
				} else if ((float) area.height / area.width > 6.0f) {
					// かなり縦長
					dist = calcDistWithPole(vo);
					if (vo.isRightTouched())
						vo.isRightPost = true;
					if (vo.isLeftTouched())
						vo.isLeftPost = true;
				}

			} else if (!vo.isTopTouched() && !vo.isBottomTouched()) {
				// 上下が切れていない
				dist = calcDistWithHeight(vo);
			}
		}

		if (dist * dist > Field.MaxY * Field.MaxX * 4) {
			log.debug(vo.getType() + " dist too far. dist:" + dist);
			vo.confidence = 0;
			return;
		}

		if (dist <= 0) {
			log.debug(vo.getType() + " invalid distance dist:" + dist);
			vo.confidence = 0;
			return;
		}

		vo.distance = dist;
		vo.distanceUsable = true;
		log.debug(vo.getType() + " dist:" + dist);
		return;
	}

	// blob中のポールの太さから距離を計算
	private int calcDistWithPole(GoalVisualObject vo) {
		Rectangle area = vo.area;
		log.debug(vo.type + " calc distance with pole. size:" + area.width);
		float a = getContext().getParam(Float.GOAL_DIST_CALIB_POLEa);
		float b = getContext().getParam(Float.GOAL_DIST_CALIB_POLEb);
		float c = getContext().getParam(Float.GOAL_DIST_CALIB_POLEc);
		return (int) (a / (area.width + b) - c);
	}

	// blob中のゴールの高さ(ポールの高さ)から距離を計算
	private int calcDistWithHeight(GoalVisualObject vo) {
		Rectangle area = vo.area;
		log.debug(vo.type + " calc distance with height. size:" + area.height);
		float a = getContext().getParam(Float.GOAL_DIST_CALIB_HEIGHTa);
		float b = getContext().getParam(Float.GOAL_DIST_CALIB_HEIGHTb);
		float c = getContext().getParam(Float.GOAL_DIST_CALIB_HEIGHTc);
		return (int) (a / (area.height + b) - c);
	}

	// blob中のゴールの幅(二本のポールの間の幅)から距離を計算
	private int calcDistWithWidth(GoalVisualObject vo) {
		Rectangle area = vo.area;
		log.debug(vo.type + " calc distance with width. size:" + area.width);
		float a = getContext().getParam(Float.GOAL_DIST_CALIB_WIDTHa);
		float b = getContext().getParam(Float.GOAL_DIST_CALIB_WIDTHb);
		float c = getContext().getParam(Float.GOAL_DIST_CALIB_WIDTHc);
		return (int) (a / (area.width + b) - c);
	}

	private void checkRobotAngle(GoalVisualObject goal) {
		if (goal.robotAngle.y > 0.15f) {
			log.debug(goal.getType() + " sanity too high angle.");
			goal.confidence = 0;
		} else if (goal.robotAngle.y < -0.5f) {
			log.debug(goal.getType() + " sanity too low angle.");
			goal.confidence = 0;
		}
	}
}
