/*
 * 作成日: 2008/10/26
 */
package jp.ac.fit.asura.nao.motion;

import static jp.ac.fit.asura.nao.motion.TestWalkMotion.Leg.LEFT;
import static jp.ac.fit.asura.nao.motion.TestWalkMotion.Leg.RIGHT;
import static jp.ac.fit.asura.nao.motion.TestWalkMotion.LegState.SUPPORT_PHASE;
import static jp.ac.fit.asura.nao.motion.TestWalkMotion.LegState.SWING_PHASE;
import static jp.ac.fit.asura.nao.motion.TestWalkMotion.WalkState.START;
import static jp.ac.fit.asura.nao.motion.TestWalkMotion.WalkState.STOP;
import static jp.ac.fit.asura.nao.motion.TestWalkMotion.WalkState.SWING;
import static jp.ac.fit.asura.nao.motion.TestWalkMotion.WalkState.SWING_BEGIN;
import static jp.ac.fit.asura.nao.motion.TestWalkMotion.WalkState.SWING_END;

import java.awt.Polygon;
import java.util.EnumMap;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.misc.Kinematics;
import jp.ac.fit.asura.nao.physical.Nao.Frames;
import jp.ac.fit.asura.nao.sensation.FrameState;
import jp.ac.fit.asura.nao.sensation.SomaticContext;
import jp.ac.fit.asura.nao.sensation.SomatoSensoryCortex;

import org.apache.log4j.Logger;

/**
 * @author $Author$
 *
 * @version $Id$
 *
 */
public class TestWalkMotion extends Motion {
	public static final int TESTWALK_MOTION = 5963;
	private static Logger log = Logger.getLogger(TestWalkMotion.class);

	enum WalkState {
		START, SWING_BEGIN, SWING, SWING_END, SWING_RIGHT_END, STOP
	}

	enum LegState {
		SUPPORT_PHASE, SWING_PHASE
	}

	enum Leg {
		LEFT, RIGHT
	}

	private SomatoSensoryCortex ssc;

	private WalkState state;
	int stateCount;

	private EnumMap<Leg, LegState> legState;
	private Leg swingLeg;
	private Leg supportLeg;

	private float[] out;

	private boolean stopRequested;
	private boolean hasNextStep;

	FrameState lar;
	FrameState rar;

	float stride = 30;

	public TestWalkMotion(int id) {
		setId(id);
		out = new float[Joint.values().length];
		legState = new EnumMap<Leg, LegState>(Leg.class);
	}

	public void init(RobotContext context) {
		ssc = context.getSensoryCortex();
	}

	public void start() {
		log.debug("start testwalk");
		lar = new FrameState(Frames.LAnkleRoll);
		rar = new FrameState(Frames.RAnkleRoll);
		lar.getBodyRotation().set(
				new AxisAngle4f(1, 0, 0, (float) Math.toRadians(-5)));
		rar.getBodyRotation().set(
				new AxisAngle4f(1, 0, 0, (float) Math.toRadians(-5)));

		changeState(START);
		hasNextStep = true;

	}

	public float[] stepNextFrame(float[] current) {
		log.debug("step testwalk");
		System.arraycopy(current, 0, out, 0, out.length);

		updateLegState();

		switch (state) {
		case START:
			changeState(SWING_BEGIN);
			// 右足から振り始める
			supportLeg = LEFT;
			swingLeg = RIGHT;
		case SWING_BEGIN:
			// 歩行のはじめ 支持脚に重心を移す
			assert legState.get(supportLeg) == SUPPORT_PHASE;

			// 重心が移ったら足を振る
			if (canSwingLeg()) {
				changeState(SWING);
			}

			// TODO 支持脚に重心を移す
			leanSupportLeg();
			break;
		case SWING:
			// 歩行の途中 遊足を前へ出す
			if (isSwingLegReached()) {
				// 目標位置に達したら下ろす
				changeState(SWING_END);
			}

			// TODO 遊足を前へ
			forwardSwingLeg();

			break;
		case SWING_END:
			// 歩行の終わり 遊足を下ろす

			// 支持脚になったら終了
			if (legState.get(swingLeg) == SUPPORT_PHASE) {
				if (!stopRequested) {
					// 歩行を続ける
					changeSupportLeg();
					changeState(SWING_BEGIN);
				} else {
					// やめる
					changeState(STOP);
				}
				break;
			}

			// TODO 足を下ろす
			downSwingLeg();

			break;
		}
		if (state == STOP)
			hasNextStep = false;
		stateCount++;
		return out;
	}

	private void changeSupportLeg() {
		// 支持脚を交換する
		Leg t = supportLeg;
		supportLeg = swingLeg;
		swingLeg = t;
	}

	private void updateLegState() {

		if (ssc.isLeftOnGround())
			legState.put(LEFT, SUPPORT_PHASE);
		else
			legState.put(LEFT, SWING_PHASE);

		if (ssc.isRightOnGround())
			legState.put(RIGHT, SUPPORT_PHASE);
		else
			legState.put(RIGHT, SWING_PHASE);

		if (Math.abs(ssc.getContext().get(Frames.LSole).getBodyPosition().y
				- ssc.getContext().get(Frames.RSole).getBodyPosition().y) < 3) {
			if (legState.get(LEFT) == SUPPORT_PHASE)
				legState.put(RIGHT, SUPPORT_PHASE);
			else if (legState.get(RIGHT) == SUPPORT_PHASE)
				legState.put(LEFT, SUPPORT_PHASE);
		}
	}

	/**
	 * 遊脚をあげることができるか?
	 *
	 * 支持脚に重心が移っているかをチェック
	 *
	 * @return
	 */
	private boolean canSwingLeg() {
		// 支持脚の凸多角形内に重心があるか?

		// 別にロボット座標系で計算する必要はない気がする
		Vector3f com = new Vector3f(ssc.getContext().getCenterOfMass());
		ssc.body2robotCoord(com, com);

		Polygon polygon = new Polygon();

		if (supportLeg == LEFT) {
			addSolePoint(polygon, Frames.LSoleFL);
			addSolePoint(polygon, Frames.LSoleFR);
			addSolePoint(polygon, Frames.LSoleBL);
			addSolePoint(polygon, Frames.LSoleBR);
			com.x -= 5;
		} else {
			addSolePoint(polygon, Frames.RSoleFL);
			addSolePoint(polygon, Frames.RSoleFR);
			addSolePoint(polygon, Frames.RSoleBL);
			addSolePoint(polygon, Frames.RSoleBR);
			com.x += 5;
		}

		log.trace("canSwingLeg com:" + com + " poly:" + polygon);
		return polygon.contains(com.x, com.z);
	}

	private void addSolePoint(Polygon poly, Frames sole) {
		Vector3f vec = new Vector3f(ssc.getContext().get(sole)
				.getBodyPosition());
		ssc.body2robotCoord(vec, vec);
		poly.addPoint((int) vec.x, (int) vec.z);
	}

	/**
	 * 遊脚が目標位置に達したか?
	 *
	 * 歩幅からチェック
	 *
	 * @return
	 */
	private boolean isSwingLegReached() {
		// 一歩あたりの歩幅

		FrameState swing;

		if (swingLeg == Leg.LEFT) {
			swing = ssc.getContext().get(Frames.LAnkleRoll);
		} else {
			swing = ssc.getContext().get(Frames.RAnkleRoll);
		}
		log.trace("isSwingLegReached " + (swing.getBodyPosition().z > stride));
		return swing.getBodyPosition().z > stride;
	}

	private void leanSupportLeg() {
		log.trace("leanSupportLeg " + supportLeg);
		SomaticContext sc = new SomaticContext(ssc.getContext());
		// Kinematics.calculateForward(sc);

		lar.getBodyPosition().set(sc.get(Frames.LAnkleRoll).getBodyPosition());
		rar.getBodyPosition().set(sc.get(Frames.RAnkleRoll).getBodyPosition());
		lar.getBodyRotation().set(sc.get(Frames.LAnkleRoll).getBodyRotation());
		rar.getBodyRotation().set(sc.get(Frames.RAnkleRoll).getBodyRotation());

		// 1フレームあたり10mmぐらいで
		float dx = 10.0f;

		lar.getBodyPosition().y = -220;
		rar.getBodyPosition().y = -220;

		// 足のx座標をずらす。ボディからみた位置なので、傾けたい方向とは逆になる。
		if (supportLeg == Leg.LEFT) {
			lar.getBodyPosition().x -= dx;
			rar.getBodyPosition().x -= dx;
		} else {
			lar.getBodyPosition().x += dx;
			rar.getBodyPosition().x += dx;
		}

		// 最初に取得した値を目標に逆運動学計算
		Kinematics.calculateInverse(sc, lar);
		Kinematics.calculateInverse(sc, rar);

		copyToOut(sc);
	}

	private void forwardSwingLeg() {
		log.trace("forwardSwingLeg " + swingLeg);
		SomaticContext sc = new SomaticContext(ssc.getContext());

		lar.getBodyPosition().set(sc.get(Frames.LAnkleRoll).getBodyPosition());
		rar.getBodyPosition().set(sc.get(Frames.RAnkleRoll).getBodyPosition());
		lar.getBodyRotation().set(sc.get(Frames.LAnkleRoll).getBodyRotation());
		rar.getBodyRotation().set(sc.get(Frames.RAnkleRoll).getBodyRotation());

		// 1フレームあたり10mmぐらいで
		float dx = 5.0f;

		// 一歩あたりの歩幅
		FrameState support;
		FrameState swing;

		float legHeight;
		if (swingLeg == Leg.LEFT) {
			swing = lar;
			support = rar;
			legHeight = sc.get(Frames.LSole).getBodyPosition().y;
		} else {
			swing = rar;
			support = lar;
			legHeight = sc.get(Frames.RSole).getBodyPosition().y;
		}
		// 足のx座標をずらす。ボディからみた位置なので、傾けたい方向とは逆になる。

		log.trace("swing y:" + legHeight);
		log.trace("body y:" + ssc.calculateBodyHeight());

		if (legHeight + ssc.calculateBodyHeight() < 40)
			swing.getBodyPosition().y += dx;
		else {
			swing.getBodyPosition().z += dx;
			support.getBodyPosition().z -= dx;

		}
		// support.getBodyPosition().z -= dx;

		// 最初に取得した値を目標に逆運動学計算
		Kinematics.calculateInverse(sc, lar);
		Kinematics.calculateInverse(sc, rar);

		copyToOut(sc);
	}

	private void downSwingLeg() {
		log.trace("downSwingLeg " + swingLeg);
		SomaticContext sc = new SomaticContext(ssc.getContext());

		lar.getBodyPosition().set(sc.get(Frames.LAnkleRoll).getBodyPosition());
		rar.getBodyPosition().set(sc.get(Frames.RAnkleRoll).getBodyPosition());
		lar.getBodyRotation().set(sc.get(Frames.LAnkleRoll).getBodyRotation());
		rar.getBodyRotation().set(sc.get(Frames.RAnkleRoll).getBodyRotation());

		// 1フレームあたり10mmぐらいで
		float dx = 10.0f;

		// 一歩あたりの歩幅
		FrameState support;
		FrameState swing;

		if (swingLeg == Leg.LEFT) {
			swing = lar;
			support = rar;
		} else {
			swing = rar;
			support = lar;
		}
		// 足のx座標をずらす。ボディからみた位置なので、傾けたい方向とは逆になる。

		// if (legHeight + ssc.calculateBodyHeight() < 40)

		dx = support.getBodyPosition().y - swing.getBodyPosition().y;

		if (dx < -10)
			dx = -10;
		else {
			dx *= 0.5f;
			dx -= 1;
		}

		swing.getBodyPosition().y += dx;

		log.trace("swing y:" + swing.getBodyPosition().y);
		log.trace("support y:" + support.getBodyPosition().y);
		log.trace("dy:" + dx);

		// 最初に取得した値を目標に逆運動学計算
		Kinematics.calculateInverse(sc, lar);
		Kinematics.calculateInverse(sc, rar);

		copyToOut(sc);
	}

	private void changeState(WalkState s) {
		log.debug("change state to " + s);
		state = s;
		stateCount = 0;
	}

	private void copyToOut(SomaticContext sc) {
		out[Joint.LHipYawPitch.ordinal()] = sc.get(Frames.LHipYawPitch)
				.getAngle();
		out[Joint.LHipPitch.ordinal()] = sc.get(Frames.LHipPitch).getAngle();
		out[Joint.LHipRoll.ordinal()] = sc.get(Frames.LHipRoll).getAngle();
		out[Joint.LKneePitch.ordinal()] = sc.get(Frames.LKneePitch).getAngle();
		out[Joint.LAnklePitch.ordinal()] = sc.get(Frames.LAnklePitch)
				.getAngle();
		out[Joint.LAnkleRoll.ordinal()] = sc.get(Frames.LAnkleRoll).getAngle();
		out[Joint.RHipYawPitch.ordinal()] = sc.get(Frames.RHipYawPitch)
				.getAngle();
		out[Joint.RHipPitch.ordinal()] = sc.get(Frames.RHipPitch).getAngle();
		out[Joint.RHipRoll.ordinal()] = sc.get(Frames.RHipRoll).getAngle();
		out[Joint.RKneePitch.ordinal()] = sc.get(Frames.RKneePitch).getAngle();
		out[Joint.RAnklePitch.ordinal()] = sc.get(Frames.RAnklePitch)
				.getAngle();
		out[Joint.RAnkleRoll.ordinal()] = sc.get(Frames.RAnkleRoll).getAngle();
	}

	// public float[] stepNextFrame2(float[] current) {
	// System.arraycopy(current, 0, out, 0, out.length);
	// SomaticContext sc = new SomaticContext(ssc.getContext());
	// Kinematics.calculateForward(sc);
	//
	// int dy = 10;
	// int dz = 15;
	//
	// double u = currentStep > 20 ? 1 : currentStep / 20.0;
	//
	// t += (35 * Math.PI / 180.0) * u;
	//
	// double phase = u * Math.PI + (1 - u) * t;
	//
	// lar.getBodyPosition().set(50, (float) (-220 + dy * Math.cos(t)),
	// (float) (-5 + dz * Math.sin(t)));
	// rar.getBodyPosition().set(-50,
	// (float) (-220 + dy * Math.cos(t - phase)),
	// (float) (-5 + dz * Math.sin(t - phase)));
	//
	// // 最初に取得した値を目標に逆運動学計算
	// Kinematics.calculateInverse(sc, lar);
	// Kinematics.calculateInverse(sc, rar);
	//
	// // 歩行
	// // ip[Joint.LHipYawPitch.ordinal()] = sc.get(Frames.LHipYawPitch)
	// // .getAngle();
	// // ip[Joint.LHipPitch.ordinal()] = sc.get(Frames.LHipPitch).getAngle();
	// // ip[Joint.LHipRoll.ordinal()] = sc.get(Frames.LHipRoll).getAngle();
	// // ip[Joint.LKneePitch.ordinal()] =
	// // sc.get(Frames.LKneePitch).getAngle();
	// // ip[Joint.LAnklePitch.ordinal()] =
	// // sc.get(Frames.LAnklePitch).getAngle();
	// // ip[Joint.LAnkleRoll.ordinal()] =
	// // sc.get(Frames.LAnkleRoll).getAngle();
	// // ip[Joint.RHipYawPitch.ordinal()] = sc.get(Frames.RHipYawPitch)
	// // .getAngle();
	// // ip[Joint.RHipPitch.ordinal()] = sc.get(Frames.RHipPitch).getAngle();
	// // ip[Joint.RHipRoll.ordinal()] = sc.get(Frames.RHipRoll).getAngle();
	// // ip[Joint.RKneePitch.ordinal()] =
	// // sc.get(Frames.RKneePitch).getAngle();
	// // ip[Joint.RAnklePitch.ordinal()] =
	// // sc.get(Frames.RAnklePitch).getAngle();
	// // ip[Joint.RAnkleRoll.ordinal()] =
	// // sc.get(Frames.RAnkleRoll).getAngle();
	//
	// // 傾斜
	// // Vector3f com = new Vector3f(ssc.getContext().getCenterOfMass());
	// // ssc.body2robotCoord(com, com);
	// // Vector3f tiptoe = new Vector3f(ssc.getContext().get(Frames.LSoleFR)
	// // .getBodyPosition());
	// // ssc.body2robotCoord(tiptoe, tiptoe);
	// //
	// // System.out.println(tiptoe + "," + com);
	// // if (tiptoe.z - 5 > com.z) {
	// // ip[Joint.LHipPitch.ordinal()] += (float) (-Math.PI / 1800);
	// // ip[Joint.RHipPitch.ordinal()] = ip[Joint.LHipPitch.ordinal()];
	// // ip[Joint.LAnklePitch.ordinal()] = ip[Joint.LHipPitch.ordinal()];
	// // ip[Joint.RAnklePitch.ordinal()] = ip[Joint.RHipPitch.ordinal()];
	// // }
	// currentStep++;
	// return out;
	// }

	public void requestStop() {
		// TODO 歩行の軌道を動的に修正する
		stopRequested = true;
	}

	public boolean canStop() {
		return !hasNextStep;
	}

	public boolean hasNextStep() {
		return hasNextStep;
	}
}
