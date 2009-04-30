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
import java.util.Set;

import jp.ac.fit.asura.nao.physical.Field;
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
		List<Blob> blobs = getContext().blobVision.findBlobs(color, 10, 50);
		Set<Blob> set = vo.getBlobs();

		for (Blob blob : blobs) {
			set.add(blob);
		}

		if (!blobs.isEmpty()) {
			getContext().generalVision.processObject(vo);
			calculateDistance(vo);
		}
	}

	private void calculateDistance(GoalVisualObject vo) {
		vo.isLeftPost = false;
		vo.isRightPost = false;
		if (!vo.isBottomTouched() && !vo.isLeftTouched()
				&& !vo.isRightTouched()) {
			Rectangle area = vo.area;
			int dist = -1;
			if (vo.isTopTouched()) {
				// 上がついてる
				if (vo.getBlobs().size() == 1) {
					// blobが一つしかない
					if ((float) (area.height / area.width) > 1.5f) {
						// 縦長ならポールがみえてる
						// f(x) = a / (x+b) + c
						// a = 59111.1
						// b = 3.82416
						// c = -113.482
						dist = (int) (59111.1f / (area.width + 3.82416f) + -113.482f);

						if (dist > 3000 || dist < 500) {
							// 遠すぎるor近すぎるポールは無視
							dist = -1;
						}
						vo.isLeftPost = true;
						vo.isRightPost = true;
					} else if ((float) (area.width / area.height) > 1.5f) {
						// 横長なら全部みえてる?
						// f(x) = a / (x+b) + c
						// a = 643736
						// b = 8.27887
						// c = -154.302
						dist = (int) (643736f / (area.width + 8.27887f) + -154.302f);
						// old
						// dist = 200 * Goal.Height / area.height;
						log.debug("Full goal detected.");
					}
				} else if (vo.getBlobs().size() == 2
						&& (float) (area.width / area.height) > 1.5f) {
					// blob二つ以上で構成されていて横長ならポールがみえてるはず
					// 270 = a*b/95
					dist = (int) ((95.0 * 2700) / area.width);
					log.debug("Goal posts detected.");
				}
			} else {
				// f(x) = a / (x+b) + c
				// a = 643736
				// b = 8.27887
				// c = -154.302
				dist = (int) (643736f / (area.width + 8.27887f) + -154.302f);
				// old
				// dist = 200 * Goal.Height / area.height;
			}

			if (dist > Field.MaxY) {
				log.trace(vo.getType() + " dist too far.");
				vo.confidence = 0;
				return;
			}

			if (dist >= 0 && dist < Field.MaxY * 3) {
				vo.distance = dist;
				vo.distanceUsable = true;

				log.debug("VC: goal dist" + dist);
				return;
			}
		}
		vo.distanceUsable = false;
	}
}
