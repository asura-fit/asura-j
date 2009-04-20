/*
 * 作成日: 2008/06/23
 */
package jp.ac.fit.asura.nao.motion;

/**
 * @author sey
 *
 * @version $Id: Motions.java 681 2008-07-13 11:19:55Z kilo $
 *
 */
public class Motions {
	// 普通のモーション
	public static final int MOTION_STOP = 0;
	public static final int MOTION_STOP2 = 1;

	public static final int MOTION_KAGAMI = 3; // lean

	public static final int MOTION_GETUP_BACK = 60;
	public static final int MOTION_GETUP = 61;
	public static final int MOTION_YY_GETUP_BACK = 62;

	// 歩きとか移動系
	public static final int MOTION_LEFT_YY_TURN = 10;
	public static final int MOTION_RIGHT_YY_TURN = 11;

	public static final int MOTION_CIRCLE_LEFT = 12;
	public static final int MOTION_CIRCLE_RIGHT = 13;

	public static final int MOTION_YY_FORWARD1 = 30;
	public static final int MOTION_YY_FORWARD2 = 31;
	public static final int MOTION_YY_FORWARD = 32;
	public static final int MOTION_YY_FORWARD_STEP = 33;

	// シュートとか
	public static final int MOTION_SHOT1 = 41;

	public static final int MOTION_SHOT2 = 40;

	public static final int MOTION_KAKICK_INSIDE_LEFT = 42;
	public static final int MOTION_KAKICK_INSIDE_RIGHT = 43;

	public static final int MOTION_KAKICK_LEFT = 44;
	public static final int MOTION_KAKICK_RIGHT = 45;

	public static final int MOTION_SIDEKEEP_LEFT = 46;
	public static final int MOTION_SIDEKEEP_RIGHT = 47;

	public static final int MOTION_BACKSHOT_LEFT = 48;
	public static final int MOTION_BACKSHOT_RIGHT = 49;

	// お遊びモーション,ポーズとか
	public static final int MOTION_RANRANRU = 70;
	public static final int MOTION_TAKA = 71;

	// robotstadium, webots標準のモーション
	public static final int MOTION_W_GETUP = 100;

	public static final int MOTION_W_SHOT = 104;

	public static final int MOTION_W_FORWARD = 102;
	public static final int MOTION_W_FORWARD50 = 103;

	public static final int MOTION_W_BACKWARD = 101;

	public static final int MOTION_W_LEFT_TURN40 = 109;

	public static final int MOTION_W_RIGHT_TURN40 = 111;

	public static final int MOTION_W_LEFT_SIDESTEP = 105;
	public static final int MOTION_W_RIGHT_SIDESTEP = 106;

	// Naoji固有のモーション
	public static final int NAOJI_WALKER = 80;

	// Parameterized Actions
	public static final int ACTION_SHOOT_LEFT = 10042;
	public static final int ACTION_SHOOT_RIGHT = 10043;
}
