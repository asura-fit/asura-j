/*
 * 作成日: 2008/06/18
 */
package jp.ac.fit.asura.nao.vision.objects;

import jp.ac.fit.asura.nao.vision.VisualObjects;
import jp.ac.fit.asura.nao.vision.VisualObjects.Properties;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class GoalVisualObject extends VisualObject {
	public GoalVisualObject(VisualObjects type) {
		super(type);
	}
	
	protected void updateProperty(Properties prop) {
		switch (prop) {
		case Distance:
		case DistanceUsable:
		case IsLeftPost:
		case IsRightPost:
			context.goalVision.calculateDistance(this);
			break;
		default:
			super.updateProperty(prop);
		}
	}
}
