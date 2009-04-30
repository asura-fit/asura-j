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
		List<Blob> blobs = getContext().blobVision.findBlobs(color, 10, 500);
		Set<Blob> set = vo.getBlobs();

		for (Blob blob : blobs) {
			set.add(blob);
		}

		if (!blobs.isEmpty()) {
			getContext().generalVision.processObject(vo);
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
			if (vo.isTopTouched()) {
				// 上がついてる
				if (vo.getBlobs().size() == 1) {
					// blobが一つしかない
					if ((float) area.height / area.width > 1.5f) {
						// 縦長ならポールがみえてる
						// f(x) = a / (x+b) + c
						// a = 59111.1
						// b = 3.82416
						// c = -113.482
						dist = (int) (59111.1f / (area.height + 3.82416f) + -113.482f);

						vo.isLeftPost = true;
						vo.isRightPost = true;
						log.debug(vo.getType() + " post detected.");
					} else if ((float) area.width / area.height > 1.5f) {
						// 横長なら全部みえてる?
						// f(x) = a / (x+b) + c
						// a = 643736
						// b = 8.27887
						// c = -154.302
						dist = (int) (643736f / (area.width + 8.27887f) + -154.302f);
						// old
						// dist = 200 * Goal.Height / area.height;
						log.debug(vo.getType() + "Full goal detected.");
					}
				} else if (vo.getBlobs().size() == 2
						&& (float) area.width / area.height > 1.5f) {
					// blob二つ以上で構成されていて横長ならポールがみえてるはず
					// TODO 個々のblobが縦長であることが条件
					// 270 = a*b/95
					dist = (int) (643736f / (area.width + 8.27887f) + -154.302f);
					log.debug(vo.getType() + " posts detected.");
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
		} else if (!vo.isLeftTouched() && !vo.isBottomTouched()
				&& area.width / area.height < 2) {
			// 左右がついていて下がついていない場合は上下から推定
			// 横長なのはだめ
			dist = (int) (59111.1f / (area.height + 3.82416f) + -113.482f);

			if (vo.isRightTouched())
				vo.isRightPost = true;
			if (vo.isLeftTouched())
				vo.isLeftPost = true;
		}

		if (dist * dist > Field.MaxY * Field.MaxX * 4) {
			log.trace(vo.getType() + " dist too far." + dist);
			vo.confidence = 0;
			return;
		}

		if (dist <= 0) {
			log.debug(vo.getType() + " invalid distance " + dist);
			vo.confidence = 0;
			return;
		}

		vo.distance = dist;
		vo.distanceUsable = true;

		log.debug(vo.getType() + " dist" + dist);
		return;
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
