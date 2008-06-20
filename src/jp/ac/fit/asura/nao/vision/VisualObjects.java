/*
 * 作成日: 2008/06/11
 */
package jp.ac.fit.asura.nao.vision;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public enum VisualObjects {
	Ball, BlueGoal, YellowGoal;

	public static enum Properties {
		Distance, Confidence, Center, Angle, TopTouched, BottomTouched, LeftTouched, RightTouched,
	}
}
