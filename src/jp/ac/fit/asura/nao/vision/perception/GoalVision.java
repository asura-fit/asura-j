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

import jp.ac.fit.asura.nao.physical.Goal;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.perception.BlobVision.Blob;

import org.apache.log4j.Logger;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class GoalVision {
	private Logger log = Logger.getLogger(GoalVision.class);

	private VisualContext context;

	public void findYellowGoal() {
		VisualObject goal = context.get(YellowGoal);
		findGoal((GoalVisualObject) goal, cYELLOW);
	}

	public void findBlueGoal() {
		VisualObject goal = context.get(BlueGoal);
		findGoal((GoalVisualObject) goal, cCYAN);
	}

	private void findGoal(GoalVisualObject vo, byte color) {
		List<Blob> blobs = context.blobVision.findBlobs(color, 10, 50);
		Set<Blob> set = vo.getBlobs();

		for (Blob blob : blobs) {
			set.add(blob);
		}

		if (!blobs.isEmpty()) {
			context.generalVision.processObject(vo);
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
					if ((float) (area.height / area.width) > 3.0f) {
						// 縦長ならポールがみえてる
						dist = (int) ((10.0 * 2700) / area.width);

						if (dist > 3000 || dist < 500) {
							// 遠すぎるor近すぎるポールは無視
							dist = -1;
						}
						vo.isLeftPost = true;
						vo.isRightPost = true;
					} else if ((float) (area.width / area.height) > 1.5f) {
						// 横長なら全部みえてる?
						dist = 200 * Goal.Height / area.height;
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
				// 
				dist = 200 * Goal.Height / area.height;
			}

			if (dist >= 0) {
				vo.distance = dist;
				vo.distanceUsable = true;

				log.debug("VC: goal dist" + dist);
				return;
			}
		}
		vo.distanceUsable = false;
	}

	/**
	 * @param context
	 *            the context to set
	 */
	public void setContext(VisualContext context) {
		this.context = context;
	}
}
