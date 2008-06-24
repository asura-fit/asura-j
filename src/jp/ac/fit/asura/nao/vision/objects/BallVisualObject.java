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
public class BallVisualObject extends VisualObject {
	public BallVisualObject() {
		super(VisualObjects.Ball);
	}

	protected void updateProperty(Properties prop) {
		switch (prop) {
		case Distance:
			context.ballVision.calculateDistance(this);
			break;
		default:
			super.updateProperty(prop);
		}
	}
}
