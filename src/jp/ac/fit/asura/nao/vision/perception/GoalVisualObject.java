/*
 * 作成日: 2008/06/18
 */
package jp.ac.fit.asura.nao.vision.perception;

import jp.ac.fit.asura.nao.vision.VisualObjects;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class GoalVisualObject extends VisualObject {
	public int distance;
	public boolean distanceUsable;

	// ゴールポストかどうか. ポストだがどちらか判断できないなら，trueになる.
	public boolean isLeftPost;
	public boolean isRightPost;

	public GoalVisualObject(VisualObjects type) {
		super(type);
	}
}
