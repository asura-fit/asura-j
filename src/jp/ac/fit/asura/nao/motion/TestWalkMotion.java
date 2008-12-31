/*
 * 作成日: 2008/10/26
 */
package jp.ac.fit.asura.nao.motion;

import static jp.ac.fit.asura.nao.motion.TestWalkMotion.Leg.LEFT;
import static jp.ac.fit.asura.nao.motion.TestWalkMotion.Leg.RIGHT;
import static jp.ac.fit.asura.nao.motion.TestWalkMotion.LegState.SUPPORT_PHASE;
import static jp.ac.fit.asura.nao.motion.TestWalkMotion.LegState.SWING_PHASE;
import static jp.ac.fit.asura.nao.motion.TestWalkMotion.WalkState.READY;
import static jp.ac.fit.asura.nao.motion.TestWalkMotion.WalkState.START;
import static jp.ac.fit.asura.nao.motion.TestWalkMotion.WalkState.STOP;
import static jp.ac.fit.asura.nao.motion.TestWalkMotion.WalkState.SWING;
import static jp.ac.fit.asura.nao.motion.TestWalkMotion.WalkState.SWING_BEGIN;
import static jp.ac.fit.asura.nao.motion.TestWalkMotion.WalkState.SWING_END;

import java.awt.Point;
import java.awt.Polygon;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumMap;

import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.self.GPSLocalization;
import jp.ac.fit.asura.nao.misc.Kinematics;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.misc.SingularPostureException;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
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
	private static final Logger log = Logger.getLogger(TestWalkMotion.class);

	enum WalkState {
		START, READY, SWING_BEGIN, SWING, SWING_END, SWING_RIGHT_END, STOP
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
	int pedometer;

	private EnumMap<Leg, LegState> legState;
	private Leg swingLeg;
	private Leg supportLeg;

	private float[] out;

	private boolean stopRequested;
	private boolean hasNextStep;

	float stride = 50;
	float baseHeight = 240;
	float footHeight = 15;
	float walkCycle = 30;
	float leanLimit = 20;

	float targetHeight;
	GPSLocalization gps = new GPSLocalization();

	public TestWalkMotion(int id) {
		setId(id);
		out = new float[Joint.values().length];
		legState = new EnumMap<Leg, LegState>(Leg.class);
	}

	public void init(RobotContext context) {
		ssc = context.getSensoryCortex();
		gps.init(context);
	}

	public void start() {
		log.debug("start testwalk");

		changeState(START);
		hasNextStep = true;
		pedometer = 0;
	}

	public float[] stepNextFrame(float[] current) {
		log.debug("step testwalk");
		System.arraycopy(current, 0, out, 0, out.length);

		updateLegState();

		archive();

		SWITCH: do {
			switch (state) {
			case START:
				changeState(READY);
				// 右足から振り始める
				supportLeg = LEFT;
				swingLeg = RIGHT;
				continue SWITCH;

			case READY:
				if (ssc.getContext().get(Frames.LAnkleRoll).getBodyPosition().y >= -baseHeight) {
					setReadyPosition();
					break;
				}
				changeState(SWING_BEGIN);
				continue SWITCH;

			case SWING_BEGIN:
				// 歩行のはじめ 支持脚に重心を移す
				targetHeight = -baseHeight;

				// 重心が移ったら足を振る
				if (canSwingLeg()) {
					changeState(SWING);
					continue SWITCH;
				}
				// TODO 支持脚に重心を移す
				leanSupportLeg();
				break;

			case SWING:
				assert legState.get(supportLeg) == SUPPORT_PHASE;
				// 歩行の途中 遊足を前へ出す
				if (isSwingLegReached()) {
					// 目標位置に達したら下ろす
					changeState(SWING_END);
					continue SWITCH;
				}

				// TODO 遊足を前へ
				if (!forwardSwingLeg()) {
					changeState(SWING_END);
					continue SWITCH;
				}
				break;

			case SWING_END:
				// 歩行の終わり 遊足を下ろす

				// 支持脚になったら終了
				if (legState.get(swingLeg) == SUPPORT_PHASE || true) {
					if (!stopRequested) {
						// 歩行を続ける
						pedometer++;
						changeSupportLeg();
						changeState(SWING_BEGIN);
					} else {
						// やめる
						changeState(STOP);
					}
					continue SWITCH;
				}

				// TODO 足を下ろす
				downSwingLeg();

				break;
			}
		} while (false);
		if (state == STOP)
			hasNextStep = false;
		stateCount++;
		currentStep++;
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
			// com.y -= 20;
		} else {
			addSolePoint(polygon, Frames.RSoleFL);
			addSolePoint(polygon, Frames.RSoleFR);
			addSolePoint(polygon, Frames.RSoleBL);
			addSolePoint(polygon, Frames.RSoleBR);
			com.x += 5;
			// com.y -= 20;
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
		FrameState swing;
		FrameState support;

		boolean touched = false;
		boolean reached;
		if (swingLeg == Leg.LEFT) {
			swing = ssc.getContext().get(Frames.LAnkleRoll);
			support = ssc.getContext().get(Frames.RAnkleRoll);
			if (ssc.getLeftPressure() > 20) {
				touched = true;
			}
		} else {
			swing = ssc.getContext().get(Frames.RAnkleRoll);
			support = ssc.getContext().get(Frames.LAnkleRoll);
			if (ssc.getRightPressure() > 20) {
				touched = true;
			}
		}

		// if(legState.get(swingLeg) == SUPPORT_PHASE)
		// touched = true;
		reached = swing.getBodyPosition().z - support.getBodyPosition().z > stride / 4;

		log.trace("isSwingLegReached " + reached);
		return reached && touched;
	}

	private void setReadyPosition() {
		SomaticContext sc = new SomaticContext(ssc.getContext());
		// Kinematics.calculateForward(sc);
		FrameState lar = sc.get(Frames.LAnkleRoll).clone();
		FrameState rar = sc.get(Frames.RAnkleRoll).clone();

		float ldy = -baseHeight - lar.getBodyPosition().y;
		float rdy = -baseHeight - rar.getBodyPosition().y;
		lar.getBodyPosition().y += MathUtils.clipAbs(ldy, 10.0f);
		rar.getBodyPosition().y += MathUtils.clipAbs(rdy, 10.0f);

		try {
			Kinematics.calculateInverse(sc, lar);
			Kinematics.calculateInverse(sc, rar);
		} catch (SingularPostureException spe) {
			log.error("", spe);
		}
		copyToOut(sc);
	}

	private void leanSupportLeg() {
		log.trace("leanSupportLeg " + supportLeg);
		SomaticContext sc = new SomaticContext(ssc.getContext());

		FrameState lar = sc.get(Frames.LAnkleRoll).clone();
		FrameState rar = sc.get(Frames.RAnkleRoll).clone();

		// 現在の重心位置
		Vector3f com = new Vector3f();
		ssc.body2robotCoord(ssc.getContext().getCenterOfMass(), com);

		// 足のx座標をずらす。ボディからみた位置なので、傾けたい方向とは逆になる。
		Vector3f sole;
		if (supportLeg == Leg.LEFT) {
			Vector3f robotLar = new Vector3f();
			ssc.body2robotCoord(lar.getBodyPosition(), robotLar);
			sole = robotLar;
		} else {
			Vector3f robotRar = new Vector3f();
			ssc.body2robotCoord(rar.getBodyPosition(), robotRar);
			sole = robotRar;
		}

		Vector3f dx = new Vector3f(com.x - sole.x, 0, Math.min(com.z - sole.z,
				0));
		dx.scale(0.5f);
		if (dx.length() > leanLimit) {
			dx.scale(leanLimit / dx.length());
		}

		ssc.robot2bodyCoord(dx, dx);
		dx.y = 0;
		lar.getBodyPosition().add(dx);
		rar.getBodyPosition().add(dx);

		lar.getBodyPosition().y = targetHeight;
		rar.getBodyPosition().y = targetHeight;

		// 最初に取得した値を目標に逆運動学計算
		try {
			Kinematics.calculateInverse(sc, lar);
			Kinematics.calculateInverse(sc, rar);
		} catch (SingularPostureException spe) {
			log.error("", spe);
			return;
		}
		copyToOut(sc);
	}

	private boolean forwardSwingLeg() {
		log.trace("forwardSwingLeg " + swingLeg);
		SomaticContext sc = new SomaticContext(ssc.getContext());

		FrameState lar = sc.get(Frames.LAnkleRoll).clone();
		FrameState rar = sc.get(Frames.RAnkleRoll).clone();

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

		double theta1 = stateCount * 2 * Math.PI / (walkCycle);
		double theta2 = (stateCount - 1) * 2 * Math.PI / (walkCycle);

		if (theta1 > Math.PI) {
			return false;
		}

		float dx = (float) (stride * (-Math.cos(theta1) + Math.cos(theta2)));
		float dy = (float) (footHeight * (Math.sin(theta1) - Math.sin(theta2)));

		if (pedometer >= 1) {
			// 二歩目以降
			swing.getBodyPosition().z += dx;
			support.getBodyPosition().z -= dx;
		} else {
			swing.getBodyPosition().z += dx / 2;
			support.getBodyPosition().z -= dx / 2;
		}

		targetHeight += dy;
		swing.getBodyPosition().y = targetHeight;

		// 最初に取得した値を目標に逆運動学計算
		try {
			Kinematics.calculateInverse(sc, lar);
			Kinematics.calculateInverse(sc, rar);
		} catch (SingularPostureException spe) {
			log.error("", spe);
			return true;
		}
		copyToOut(sc);
		return true;
	}

	private void downSwingLeg() {
		log.trace("downSwingLeg " + swingLeg);
		SomaticContext sc = new SomaticContext(ssc.getContext());

		FrameState lar = sc.get(Frames.LAnkleRoll).clone();
		FrameState rar = sc.get(Frames.RAnkleRoll).clone();

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
		try {
			Kinematics.calculateInverse(sc, lar);
			Kinematics.calculateInverse(sc, rar);
		} catch (SingularPostureException spe) {
			log.error("", spe);
		}

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

	PrintWriter jointLog = null;
	PrintWriter comLog = null;
	PrintWriter gpsLog = null;

	private void archive() {
		try {
			if (jointLog == null)
				jointLog = new PrintWriter("ioint.log");
			if (comLog == null)
				comLog = new PrintWriter("com.log");
			if (gpsLog == null)
				gpsLog = new PrintWriter("gps.log");
		} catch (IOException e) {
		}
		// 間接角度を記録
		jointLog.print(currentStep);
		jointLog.print(" ");
		jointLog.print(out[Joint.LHipYawPitch.ordinal()]);
		jointLog.print(" ");
		jointLog.print(out[Joint.LHipPitch.ordinal()]);
		jointLog.print(" ");
		jointLog.print(out[Joint.LHipRoll.ordinal()]);
		jointLog.print(" ");
		jointLog.print(out[Joint.LKneePitch.ordinal()]);
		jointLog.print(" ");
		jointLog.print(out[Joint.LAnklePitch.ordinal()]);
		jointLog.print(" ");
		jointLog.print(out[Joint.LAnkleRoll.ordinal()]);
		jointLog.print(" ");
		jointLog.print(out[Joint.RHipYawPitch.ordinal()]);
		jointLog.print(" ");
		jointLog.print(out[Joint.RHipPitch.ordinal()]);
		jointLog.print(" ");
		jointLog.print(out[Joint.RHipRoll.ordinal()]);
		jointLog.print(" ");
		jointLog.print(out[Joint.RKneePitch.ordinal()]);
		jointLog.print(" ");
		jointLog.print(out[Joint.RAnklePitch.ordinal()]);
		jointLog.print(" ");
		jointLog.print(out[Joint.RAnkleRoll.ordinal()]);
		jointLog.println();

		Vector3f com = ssc.getContext().getCenterOfMass();
		comLog.print(currentStep);
		comLog.print(" ");
		comLog.print(com.x);
		comLog.print(" ");
		comLog.print(com.z);

		int lf = ssc.getLeftPressure();
		int rf = ssc.getRightPressure();

		Point cop = new Point();
		int force = 0;
		if (lf > 0) {
			Point leftCOP = new Point();
			ssc.getLeftCOP(leftCOP);
			cop.x += leftCOP.x * lf;
			cop.y += leftCOP.y * lf;
			force += lf;
		}

		if (rf > 0) {
			Point rightCOP = new Point();
			ssc.getRightCOP(rightCOP);
			cop.x += rightCOP.x * rf;
			cop.y += rightCOP.y * rf;
			force += rf;
		}

		// 圧力中心を描画
		if (force > 0) {
			cop.x /= force;
			cop.y /= force;
		}

		comLog.print(" ");
		comLog.print(cop.x);
		comLog.print(" ");
		comLog.print(cop.y);
		comLog.println();

		gpsLog.print(currentStep);
		gpsLog.print(" ");
		gpsLog.print(gps.getX());
		gpsLog.print(" ");
		gpsLog.print(gps.getY());
		gpsLog.print(" ");
		gpsLog.print(gps.getZ());
		gpsLog.println();
	}
}
