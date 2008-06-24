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

import jp.ac.fit.asura.nao.misc.PhysicalConstants;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualObjects.Properties;
import jp.ac.fit.asura.nao.vision.objects.GoalVisualObject;
import jp.ac.fit.asura.nao.vision.objects.VisualObject;
import jp.ac.fit.asura.nao.vision.perception.BlobVision.Blob;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class GoalVision {
	private VisualContext context;

	public void findYellowGoal() {
		VisualObject goal = context.objects.get(YellowGoal);
		findGoal((GoalVisualObject) goal, cYELLOW);
	}

	public void findBlueGoal() {
		VisualObject goal = context.objects.get(BlueGoal);
		findGoal((GoalVisualObject) goal, cCYAN);
	}

	private void findGoal(GoalVisualObject vo, byte color) {
		List<Blob> blobs = context.blobVision.findBlobs(color, 10, 20);
		Set<Blob> set = vo.getBlobs();

		for (Blob blob : blobs) {
			set.add(blob);
		}
		vo.clearCache();
	}

	public void calculateDistance(GoalVisualObject vo) {
		if (!vo.getBoolean(Properties.TopTouched)
				&& !vo.getBoolean(Properties.BottomTouched)
				&& !vo.getBoolean(Properties.LeftTouched)
				&& !vo.getBoolean(Properties.RightTouched)) {
			Rectangle area = vo.get(Rectangle.class, Properties.Area);
			int dist = 200 * PhysicalConstants.Goal.Height / area.height;
			vo.setProperty(Properties.Distance, Integer.valueOf(dist));
			vo.setProperty(Properties.DistanceUsable, Boolean.TRUE);
		} else {
			vo.setProperty(Properties.DistanceUsable, Boolean.FALSE);
		}
	}

	/**
	 * @param context
	 *            the context to set
	 */
	public void setContext(VisualContext context) {
		this.context = context;
	}
}
