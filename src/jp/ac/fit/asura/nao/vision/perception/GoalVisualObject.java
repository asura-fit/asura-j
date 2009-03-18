/*
 * 作成日: 2008/06/18
 */
package jp.ac.fit.asura.nao.vision.perception;

import jp.ac.fit.asura.nao.vision.VisualObjects;

/**
 * @author sey
 * 
 * @version $Id: GoalVisualObject.java 704 2008-10-23 17:25:51Z sey $
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
