/*
 * 作成日: 2008/05/18
 */
package jp.ac.fit.asura.nao.strategy.permanent;

import jp.ac.fit.asura.nao.Image;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.vision.VisualObject;
import jp.ac.fit.asura.nao.vision.VisualCortex.VisualObjects;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class BallTrackingTask implements Task {

	public void step(RobotContext context) {
		Image image = context.getSensor().getImage();

		int width = image.getWidth();
		int height = image.getHeight();

		VisualObject vo = context.getVision().get(VisualObjects.Ball);
		if (vo.cf > 0) {
			double angle1 = 0.8;
			double angle2 = angle1 * height / width;
			double aw = vo.center.getX() / width;
			double ah = vo.center.getY() / height;
			context.getMotor().makemotion_head_rel((float) (-0.4 * angle1 * aw),
					(float) (0.4 * angle2 * ah));
		} else {
			float yaw = (float) (Math.sin(context.getFrame() * Math.PI / 100.0
					* Math.toRadians(60.0)));
			float pitch = (float) (Math.sin(context.getFrame() * Math.PI / 50.0
					* Math.toRadians(20.0)) + Math.toRadians(40.0));
			context.getMotor().makemotion_head(yaw, pitch);
		}

	}

	public void enter(RobotContext context) {
	}

	public String getName() {
		return "BallTracking";
	}

	public void leave(RobotContext context) {
	}
}
