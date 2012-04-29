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
	// 実機で動作しないものはDeprecatedにしてある.
	// 普通のモーション

	public static final int MOTION_STOP = 0;
	@Deprecated
	public static final int MOTION_STOP2 = 1;

	public static final int NULL = 2;

	@Deprecated
	public static final int MOTION_KAGAMI = 3; // lean

	@Deprecated
	public static final int MOTION_GETUP_BACK = 160;
	@Deprecated
	public static final int MOTION_GETUP = 161;

	public static final int MOTION_YY_GETUP_BACK = 162;

	// 歩きとか移動系
	@Deprecated
	public static final int MOTION_LEFT_YY_TURN = 10;
	@Deprecated
	public static final int MOTION_RIGHT_YY_TURN = 11;

	@Deprecated
	public static final int MOTION_CIRCLE_LEFT = 12;
	@Deprecated
	public static final int MOTION_CIRCLE_RIGHT = 13;

	@Deprecated
	public static final int MOTION_YY_FORWARD1 = 30;
	@Deprecated
	public static final int MOTION_YY_FORWARD2 = 31;
	@Deprecated
	public static final int MOTION_YY_FORWARD = 32;
	@Deprecated
	public static final int MOTION_YY_FORWARD_STEP = 33;

	// 歩きっぽいもの
	public static final int BASIC_WALK = 20;

	// シュートとか
	@Deprecated
	public static final int MOTION_SHOT1 = 41;

	@Deprecated
	public static final int MOTION_SHOT2 = 40;

	@Deprecated
	public static final int MOTION_KAKICK_INSIDE_LEFT = 42;
	@Deprecated
	public static final int MOTION_KAKICK_INSIDE_RIGHT = 43;

	@Deprecated
	public static final int MOTION_KAKICK_LEFT = 44;
	@Deprecated
	public static final int MOTION_KAKICK_RIGHT = 45;

	@Deprecated
	public static final int MOTION_SIDEKEEP_LEFT = 46;
	@Deprecated
	public static final int MOTION_SIDEKEEP_RIGHT = 47;

	@Deprecated
	public static final int MOTION_BACKSHOT_LEFT = 48;
	@Deprecated
	public static final int MOTION_BACKSHOT_RIGHT = 49;

	public static final int MOTION_SHOT_LEFT = 50;
	public static final int MOTION_SHOT_RIGHT = 51;
	public static final int MOTION_SHOT_INSIDE_LEFT = 52;
	public static final int MOTION_SHOT_INSIDE_RIGHT = 53;

	public static final int MOTION_SHOT_W_LEFT = 54;
	public static final int MOTION_SHOT_W_RIGHT = 55;
	public static final int MOTION_SHOT_W_INSIDE_LEFT = 56;
	public static final int MOTION_SHOT_W_INSIDE_RIGHT = 57;

	public static final int GENERALIZED_KICK = 58;

	public static final int MOTION_WEAK_SHOT_RIGHT = 59;
	public static final int MOTION_WEAK_SHOT_LEFT = 60;
	//61はまだ安定してない
	@Deprecated
	public static final int MOTION_LL_SHOT_LEFT = 61;
	public static final int MOTION_LR_SHOT_LEFT = 62;

	//強キック
	public static final int MOTION_STRONG_SHOT_LEFT = 63;
	public static final int MOTION_STRONG_SHOT_RIGHT = 64;

    //インサイド調整 (多目的)
	public static final int MOTION_SHOT_W_INSIDE_LEFT_R2 = 65;
	public static final int MOTION_SHOT_W_INSIDE_RIGHT_R2 = 66;

	//中心より外側にあるボールを蹴るキック
	public static final int MOTION_C_SHOT_RIGHT = 67;
	public static final int MOTION_C_SHOT_LEFT = 68;

	//中心にあるボールを蹴るキック
	public static final int MOTION_CBYS_SHOT_LEFT = 69;
	public static final int MOTION_CBYS_SHOT_RIGHT = 70;

    //少し奥にあるボールを蹴るキック
	public static final int MOTION_3MK_SHOT_LEFT = 71;
	public static final int MOTION_3MK_SHOT_RIGHT = 72;

	// お遊びモーション,ポーズとか
	@Deprecated
	public static final int MOTION_RANRANRU = 140;
	@Deprecated
	public static final int MOTION_TAKA = 141;

	@Deprecated
	public static final int MOTION_wryyy = 234;

	public static final int MOTION_ITO = 128;




	// robotstadium, webots標準のモーション
	public static final int MOTION_W_GETUP = 100;
	public static final int MOTION_W_GETUP_BACK = 102;

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
	public static final int NAOJI_CIRCLETURN = 81;

	// ゴーリー
	public static final int MOTION_L_GORI_GUARD = 120;
	public static final int MOTION_L_GORI_ARM = 121;
	public static final int MOTION_L_GORI_STD = 122;
	public static final int MOTION_R_GORI_GUARD = 123;
	public static final int MOTION_R_GORI_ARM = 124;
	public static final int MOTION_R_GORI_STD = 125;
	public static final int MOTION_L_GORI_ITO = 126;
	public static final int MOTION_R_GORI_ITO = 127;
	public static final int MOTION_L_GOKI_ITO = 131;
	public static final int MOTION_R_GOKI_ITO = 132;
	public static final int MOTION_R_GOKIC_ITO = 133;
	public static final int MOTION_L_GOKIC_ITO = 134;

	// Choregraphe由来のモーション
	public static final int CHORE_FROM_FRONT = 90;
	public static final int CHORE_FROM_BACK = 91;

	// Parameterized Actions
	@Deprecated
	public static final int ACTION_SHOOT_LEFT = 10042;
	@Deprecated
	public static final int ACTION_SHOOT_RIGHT = 10043;

	public static final int IIATAI = 739;


}
