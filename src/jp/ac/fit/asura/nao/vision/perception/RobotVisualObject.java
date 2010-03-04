/*
 * 作成日: 2010/02/25
 */
package jp.ac.fit.asura.nao.vision.perception;

import jp.ac.fit.asura.nao.vision.VisualObjects;

/**
 * @author kilo
 * 
 */
public class RobotVisualObject extends VisualObject {
	public int distance;
	public boolean distanceUsable;

	public RobotVisualObject(VisualObjects type) {
		super(type);
	}

}
