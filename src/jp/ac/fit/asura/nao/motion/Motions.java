/*
 * 作成日: 2008/06/23
 */
package jp.ac.fit.asura.nao.motion;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class Motions {
	// 普通のモーション
	public static final int MOTION_STOP = 0;

	public static final int MOTION_GETUP_BACK = 1;
	public static final int MOTION_GETUP = 4;
	public static final int MOTION_YY_GETUP_BACK = 5;

	public static final int MOTION_SHOT1 = 2;

	public static final int MOTION_KAGAMI = 3; // lean

	// 歩きとか移動系
	public static final int MOTION_LEFT_YY_TURN = 10;
	public static final int MOTION_RIGHT_YY_TURN = 11;
	
	public static final int MOTION_CIRCLE_LEFT = 12;
	public static final int MOTION_CIRCLE_RIGHT = 13;

	public static final int MOTION_YY_FORWARD = 15;

	// お遊びモーション,ポーズとか
	public static final int MOTION_RANRANRU = 70;
	public static final int MOTION_TAKA = 71;

	// robotstadium, webots標準のモーション
	public static final int MOTION_W_GETUP = 100;
	public static final int MOTION_W_GETUP_BACK = 107;

	public static final int MOTION_W_SHOT = 104;

	public static final int MOTION_W_FORWARD = 102;
	public static final int MOTION_W_FORWARD50 = 103;

	public static final int MOTION_W_BACKWARD = 101;

	public static final int MOTION_W_LEFT_TURN40 = 109;
	public static final int MOTION_W_LEFT_TURN60 = 110;
	public static final int MOTION_W_LEFT_TURN180 = 108;

	public static final int MOTION_W_RIGHT_TURN40 = 111;
	public static final int MOTION_W_RIGHT_TURN60 = 112;

	public static final int MOTION_W_LEFT_SIDESTEP = 105;
	public static final int MOTION_W_RIGHT_SIDESTEP = 106;
}
