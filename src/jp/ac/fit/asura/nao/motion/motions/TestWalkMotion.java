/*
 * 作成日: 2008/10/26
 */
package jp.ac.fit.asura.nao.motion.motions;

import static jp.ac.fit.asura.nao.motion.motions.TestWalkMotion.Leg.LEFT;
import static jp.ac.fit.asura.nao.motion.motions.TestWalkMotion.Leg.RIGHT;
import static jp.ac.fit.asura.nao.motion.motions.TestWalkMotion.LegState.SUPPORT_PHASE;
import static jp.ac.fit.asura.nao.motion.motions.TestWalkMotion.LegState.SWING_PHASE;
import static jp.ac.fit.asura.nao.motion.motions.TestWalkMotion.WalkState.READY;
import static jp.ac.fit.asura.nao.motion.motions.TestWalkMotion.WalkState.START;
import static jp.ac.fit.asura.nao.motion.motions.TestWalkMotion.WalkState.STOP;
import static jp.ac.fit.asura.nao.motion.motions.TestWalkMotion.WalkState.SWING;
import static jp.ac.fit.asura.nao.motion.motions.TestWalkMotion.WalkState.SWING_BEGIN;
import static jp.ac.fit.asura.nao.motion.motions.TestWalkMotion.WalkState.SWING_END;

import java.awt.Polygon;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumMap;

import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.SensorContext;
import jp.ac.fit.asura.nao.localization.self.GPSLocalization;
import jp.ac.fit.asura.nao.misc.Coordinates;
import jp.ac.fit.asura.nao.misc.Kinematics;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.misc.SingularPostureException;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.MotionParam;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.FrameState;
import jp.ac.fit.asura.nao.sensation.SomaticContext;
import jp.ac.fit.asura.nao.sensation.SomatoSensoryCortex;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 *
 * @version $Id: TestWalkMotion.java 721 2009-02-18 03:40:44Z sey $
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
	private Effector effector;

	private WalkState state;
	int stateCount;
	int pedometer;

	private EnumMap<Leg, LegState> legState;
	private Leg swingLeg;
	private Leg supportLeg;

	private boolean stopRequested;
	private boolean hasNextStep;

	float stride = 120;
	float baseHeight = 265;
	float footHeight = 20;
	float walkCycle = 10;
	float leanLimit = 15;
	float comOffsetX = 10;

	float targetHeight;
	GPSLocalization gps = new GPSLocalization();
	Sensor sensor;

	public TestWalkMotion(int id) {
		setId(id);
		legState = new EnumMap<Leg, LegState>(Leg.class);
	}

	@Override
	public void init(RobotContext context) {
		ssc = context.getSensoryCortex();
		sensor = context.getSensor();
		effector = context.getEffector();
		gps.init(context);
	}

	@Override
	public void start(MotionParam param) {
		log.debug("start testwalk");

		changeState(START);
		hasNextStep = true;
		pedometer = 0;
	}

	@Override
	public void step() {
		log.debug(currentStep + " step testwalk");

		SomaticContext sc = context.getSomaticContext();
		updateLegState();
		archive(sc);

		SWITCH: do {
			switch (state) {
			case START:
				changeState(READY);
				// 右足から振り始める
				supportLeg = LEFT;
				swingLeg = RIGHT;
				continue SWITCH;

			case READY:
				if (Math.abs(sc.get(Frames.LAnkleRoll).getBodyPosition().y
						+ baseHeight) > 1.0f) {
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
				// if (isSwingLegReached()) {
				// // 目標位置に達したら下ろす
				// changeState(SWING_END);
				// continue SWITCH;
				// }

				// TODO 遊足を前へ
				if (!forwardSwingLeg()) {
					changeState(SWING_END);
					continue SWITCH;
				}
				break;

			case SWING_END:
				// 歩行の終わり 遊足を下ろす
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
		} while (false);
		if (state == STOP)
			hasNextStep = false;
		stateCount++;
		currentStep++;
	}

	private void changeSupportLeg() {
		// 支持脚を交換する
		Leg t = supportLeg;
		supportLeg = swingLeg;
		swingLeg = t;

	}

	private void updateLegState() {
		SomaticContext sc = context.getSomaticContext();
		if (sc.isLeftOnGround())
			legState.put(LEFT, SUPPORT_PHASE);
		else
			legState.put(LEFT, SWING_PHASE);

		if (sc.isRightOnGround())
			legState.put(RIGHT, SUPPORT_PHASE);
		else
			legState.put(RIGHT, SWING_PHASE);

		if (Math.abs(sc.get(Frames.LSole).getBodyPosition().y
				- sc.get(Frames.RSole).getBodyPosition().y) < 5) {
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
		SomaticContext sc = context.getSomaticContext();
		// 支持脚の凸多角形内に重心があるか?

		// 別にロボット座標系で計算する必要はない気がする
		Vector3f com = new Vector3f(sc.getCenterOfMass());
		Coordinates.body2robotCoord(sc, com, com);

		Polygon polygon = new Polygon();

		if (supportLeg == LEFT) {
			addSolePoint(polygon, Frames.LFsrFL);
			addSolePoint(polygon, Frames.LFsrFR);
			addSolePoint(polygon, Frames.LFsrBR);
			addSolePoint(polygon, Frames.LFsrBL);
			com.x -= comOffsetX;
			// com.y -= 20;
		} else {
			addSolePoint(polygon, Frames.RFsrFL);
			addSolePoint(polygon, Frames.RFsrFR);
			addSolePoint(polygon, Frames.RFsrBR);
			addSolePoint(polygon, Frames.RFsrBL);
			com.x += comOffsetX;
			// com.y -= 20;
		}

		log.trace("canSwingLeg com:" + com + " poly:");
		return polygon.contains(com.x, com.z);
	}

	private void addSolePoint(Polygon poly, Frames sole) {
		SomaticContext sc = context.getSomaticContext();
		Vector3f vec = new Vector3f(sc.get(sole).getBodyPosition());
		Coordinates.body2robotCoord(sc, vec, vec);
		poly.addPoint((int) vec.x, (int) vec.z);
	}

	private void setReadyPosition() {
		SomaticContext sc = new SomaticContext(context.getSomaticContext());
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
		copyToOut(sc, effector);
	}

	private void leanSupportLeg() {
		log.trace("leanSupportLeg " + supportLeg);
		SomaticContext sc = new SomaticContext(context.getSomaticContext());

		FrameState lar = sc.get(Frames.LAnkleRoll).clone();
		FrameState rar = sc.get(Frames.RAnkleRoll).clone();

		// 現在の重心位置
		Vector3f com = new Vector3f();
		Coordinates.body2robotCoord(sc, sc.getCenterOfMass(), com);

		// 足のx座標をずらす。ボディからみた位置なので、傾けたい方向とは逆になる。
		Vector3f sole;
		if (supportLeg == Leg.LEFT) {
			Vector3f robotLar = new Vector3f();
			Coordinates.body2robotCoord(sc, lar.getBodyPosition(), robotLar);
			sole = robotLar;
			com.x -= comOffsetX;
		} else {
			Vector3f robotRar = new Vector3f();
			Coordinates.body2robotCoord(sc, rar.getBodyPosition(), robotRar);
			sole = robotRar;
			com.x += comOffsetX;
		}

		Vector3f dx = new Vector3f(com.x - sole.x, 0, Math.min(com.z - sole.z,
				0));
		dx.scale(0.5f);
		if (dx.length() > leanLimit) {
			dx.scale(leanLimit / dx.length());
		}

		Coordinates.robot2bodyCoord(sc, dx, dx);
		dx.y = 0;
		lar.getBodyPosition().add(dx);
		rar.getBodyPosition().add(dx);

		lar.getBodyPosition().y = targetHeight;
		rar.getBodyPosition().y = targetHeight;

		lar.getBodyRotation().setIdentity();
		rar.getBodyRotation().setIdentity();

		// 最初に取得した値を目標に逆運動学計算
		try {
			Kinematics.calculateInverse(sc, lar);
			Kinematics.calculateInverse(sc, rar);
		} catch (SingularPostureException spe) {
			log.error("", spe);
			return;
		}
		copyToOut(sc, effector);
	}

	private boolean forwardSwingLeg() {
		log.trace("forwardSwingLeg " + swingLeg);
		SomaticContext sc = new SomaticContext(context.getSomaticContext());

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

		double theta1 = stateCount * Math.PI / (walkCycle);
		double theta2 = (stateCount - 1) * Math.PI / (walkCycle);

		if (theta1 > Math.PI) {
			return false;
		}

		float dx = (float) (stride * (-Math.cos(theta1) + Math.cos(theta2)));
		float dy = (float) (footHeight * (Math.sin(theta1) - Math.sin(theta2)));

		log.debug("dx:" + dx + " dy:" + dy + " pedometer:" + pedometer);
		if (pedometer >= 1) {
			// 二歩目以降
			swing.getBodyPosition().z += dx / 2;
			support.getBodyPosition().z -= dx / 2;
		} else {
			swing.getBodyPosition().z += dx / 4;
			support.getBodyPosition().z -= dx / 4;
		}

		targetHeight += dy;
		swing.getBodyPosition().y = targetHeight;

		lar.getBodyRotation().setIdentity();
		rar.getBodyRotation().setIdentity();

		// 最初に取得した値を目標に逆運動学計算
		try {
			Kinematics.calculateInverse(sc, lar);
			Kinematics.calculateInverse(sc, rar);
		} catch (SingularPostureException spe) {
			log.error("", spe);
			return true;
		}
		copyToOut(sc, effector);
		return true;
	}

	private void changeState(WalkState s) {
		log.debug(currentStep + " change state to " + s);
		state = s;
		stateCount = 0;
	}

	private void copyToOut(SomaticContext sc, Effector effector) {
		effector.setJoint(Joint.LHipYawPitch, sc.get(Frames.LHipYawPitch)
				.getAngle());
		effector.setJoint(Joint.LHipPitch, sc.get(Frames.LHipPitch).getAngle());
		effector.setJoint(Joint.LHipRoll, sc.get(Frames.LHipRoll).getAngle());
		effector.setJoint(Joint.LKneePitch, sc.get(Frames.LKneePitch)
				.getAngle());
		effector.setJoint(Joint.LAnklePitch, sc.get(Frames.LAnklePitch)
				.getAngle());
		effector.setJoint(Joint.LAnkleRoll, sc.get(Frames.LAnkleRoll)
				.getAngle());
		effector.setJoint(Joint.RHipYawPitch, sc.get(Frames.RHipYawPitch)
				.getAngle());
		effector.setJoint(Joint.RHipPitch, sc.get(Frames.RHipPitch).getAngle());
		effector.setJoint(Joint.RHipRoll, sc.get(Frames.RHipRoll).getAngle());
		effector.setJoint(Joint.RKneePitch, sc.get(Frames.RKneePitch)
				.getAngle());
		effector.setJoint(Joint.RAnklePitch, sc.get(Frames.RAnklePitch)
				.getAngle());
		effector.setJoint(Joint.RAnkleRoll, sc.get(Frames.RAnkleRoll)
				.getAngle());
	}

	@Override
	public void requestStop() {
		// TODO 歩行の軌道を動的に修正する
		stopRequested = true;
	}

	@Override
	public boolean canStop() {
		return !hasNextStep;
	}

	@Override
	public boolean hasNextStep() {
		return hasNextStep;
	}

	PrintWriter jointLog = null;
	PrintWriter torqueLog = null;
	PrintWriter comLog = null;
	PrintWriter gpsLog = null;

	private void archive(SomaticContext sc) {
		SensorContext sensor = context.getSensorContext();
		try {
			if (jointLog == null)
				jointLog = new PrintWriter("joint.log");
			if (torqueLog == null)
				torqueLog = new PrintWriter("torque.log");
			if (comLog == null)
				comLog = new PrintWriter("com.log");
			if (gpsLog == null)
				gpsLog = new PrintWriter("gps.log");
		} catch (IOException e) {
			log.error("", e);
		}
		// 間接角度を記録
		jointLog.print(currentStep);
		jointLog.print(" ");
		jointLog.print(Math.toDegrees(sc.get(Frames.LHipYawPitch).getAngle()));
		jointLog.print(" ");
		jointLog.print(Math.toDegrees(sc.get(Frames.LHipPitch).getAngle()));
		jointLog.print(" ");
		jointLog.print(Math.toDegrees(sc.get(Frames.LHipRoll).getAngle()));
		jointLog.print(" ");
		jointLog.print(Math.toDegrees(sc.get(Frames.LKneePitch).getAngle()));
		jointLog.print(" ");
		jointLog.print(Math.toDegrees(sc.get(Frames.LAnklePitch).getAngle()));
		jointLog.print(" ");
		jointLog.print(Math.toDegrees(sc.get(Frames.LAnkleRoll).getAngle()));
		jointLog.print(" ");
		jointLog.print(Math.toDegrees(sc.get(Frames.RHipYawPitch).getAngle()));
		jointLog.print(" ");
		jointLog.print(Math.toDegrees(sc.get(Frames.RHipPitch).getAngle()));
		jointLog.print(" ");
		jointLog.print(Math.toDegrees(sc.get(Frames.RHipRoll).getAngle()));
		jointLog.print(" ");
		jointLog.print(Math.toDegrees(sc.get(Frames.RKneePitch).getAngle()));
		jointLog.print(" ");
		jointLog.print(Math.toDegrees(sc.get(Frames.RAnklePitch).getAngle()));
		jointLog.print(" ");
		jointLog.print(Math.toDegrees(sc.get(Frames.RAnkleRoll).getAngle()));
		jointLog.println();

		torqueLog.print(currentStep);
		torqueLog.print(" ");
		torqueLog.print(sensor.getForce(Joint.LHipYawPitch));
		torqueLog.print(" ");
		torqueLog.print(sensor.getForce(Joint.LHipPitch));
		torqueLog.print(" ");
		torqueLog.print(sensor.getForce(Joint.LHipRoll));
		torqueLog.print(" ");
		torqueLog.print(sensor.getForce(Joint.LKneePitch));
		torqueLog.print(" ");
		torqueLog.print(sensor.getForce(Joint.LAnklePitch));
		torqueLog.print(" ");
		torqueLog.print(sensor.getForce(Joint.LAnkleRoll));
		torqueLog.print(" ");
		torqueLog.print(sensor.getForce(Joint.RHipYawPitch));
		torqueLog.print(" ");
		torqueLog.print(sensor.getForce(Joint.RHipPitch));
		torqueLog.print(" ");
		torqueLog.print(sensor.getForce(Joint.RHipRoll));
		torqueLog.print(" ");
		torqueLog.print(sensor.getForce(Joint.RKneePitch));
		torqueLog.print(" ");
		torqueLog.print(sensor.getForce(Joint.RAnklePitch));
		torqueLog.print(" ");
		torqueLog.print(sensor.getForce(Joint.RAnkleRoll));
		torqueLog.println();

		Vector3f com = sc.getCenterOfMass();
		comLog.print(currentStep); // 1
		comLog.print(" ");
		comLog.print(com.x - gps.getX()); // 2
		comLog.print(" ");
		comLog.print(com.z + gps.getY()); // 3

		Point2f cop = sc.getCenterOfPressure();
		comLog.print(" ");
		comLog.print(cop.x - gps.getX()); // 4
		comLog.print(" ");
		comLog.print(cop.y + gps.getY()); // 5

		Point2f comMax = new Point2f();
		Point2f comMin = new Point2f();
		Point2f copMax = new Point2f();
		Point2f copMin = new Point2f();

		// 支持多角形を求める Convex hullのアルゴリズムを使ったほうがいいが、数が少ないので直接.
		Vector3f lfl = new Vector3f(sc.get(Frames.LFsrFL).getBodyPosition());
		Vector3f lfr = new Vector3f(sc.get(Frames.LFsrFR).getBodyPosition());
		Vector3f lbl = new Vector3f(sc.get(Frames.LFsrBL).getBodyPosition());
		Vector3f lbr = new Vector3f(sc.get(Frames.LFsrBR).getBodyPosition());
		Vector3f rfl = new Vector3f(sc.get(Frames.RFsrFL).getBodyPosition());
		Vector3f rfr = new Vector3f(sc.get(Frames.RFsrFR).getBodyPosition());
		Vector3f rbl = new Vector3f(sc.get(Frames.RFsrBL).getBodyPosition());
		Vector3f rbr = new Vector3f(sc.get(Frames.RFsrBR).getBodyPosition());
		Coordinates.body2robotCoord(sc, lfl, lfl);
		Coordinates.body2robotCoord(sc, lfr, lfr);
		Coordinates.body2robotCoord(sc, lbl, lbl);
		Coordinates.body2robotCoord(sc, lbr, lbr);
		Coordinates.body2robotCoord(sc, rfl, rfl);
		Coordinates.body2robotCoord(sc, rfr, rfr);
		Coordinates.body2robotCoord(sc, rbl, rbl);
		Coordinates.body2robotCoord(sc, rbr, rbr);
		// lfl.x -= gps.getX();
		// lfr.x -= gps.getX();
		// lbl.x -= gps.getX();
		// lbr.x -= gps.getX();
		// rfl.x -= gps.getX();
		// rfr.x -= gps.getX();
		// rbl.x -= gps.getX();
		// rbr.x -= gps.getX();

		if (sc.isLeftOnGround() && sc.isRightOnGround() || state == SWING_BEGIN) {
			// if (rfl.z > lfl.z) {
			// copMax.y = rfr.z;
			// copMin.y = lbr.z;
			// if (cop.x < lbr.x)
			// copMin.y += (rbr.z - lbr.z) / (lbr.x - rbr.x)
			// * (lbr.x - cop.x);
			// if (cop.x > rfl.x)
			// copMax.y += (rfl.z - lfl.z) / (rfl.x - lfl.x)
			// * (rfl.x - cop.x);
			//
			// copMax.x = lfl.x;
			// copMin.x = rbr.x;
			// if (cop.y < rbr.y)
			// copMin.x += (lbr.x - rbr.x) / (rbr.z - lbr.z)
			// * (rbr.z - cop.y);
			// if (cop.y > lfl.z)
			// copMax.x -= (lfl.x - rfl.x) / (lfl.z - rfl.z)
			// * (lfl.z - cop.y);
			//
			// comMax.y = rfr.z;
			// comMin.y = lbr.z;
			// if (com.x < lbr.x)
			// comMin.y -= (rbr.z - lbr.z) / (lbr.x - rbr.x)
			// * (lbr.x - com.x);
			// if (com.x > rfl.x)
			// comMax.y -= (rfl.z - lfl.z) / (rfl.x - lfl.x)
			// * (rfl.x - com.x);
			//
			// comMax.x = lfl.x;
			// if (com.z > lfl.z) {
			// comMax.x += (rfl.x - lfl.x) / (rfl.z - lfl.z)
			// * (rfl.z - com.z);
			// }
			// comMax.x = Math.max(comMax.x, rfl.x);
			//
			// comMin.x = rbr.x;
			// if (com.z < rbr.z)
			// comMin.x += (lbr.x - rbr.x) / (rbr.z - lbr.z)
			// * (lbr.z - com.z);
			// comMin.x = Math.min(comMin.x, lbr.x);
			// } else {
			// copMax.y = lfr.z;
			// copMin.y = rbr.z;
			// if (cop.x > rbl.x)
			// copMin.y += (lbl.z - rbl.z) / (rbl.x - lbl.x)
			// * (rbl.x - cop.x);
			// if (cop.x < lfr.x)
			// copMax.y += (lfr.z - rfr.z) / (lfr.x - rfr.x)
			// * (lfr.x - cop.x);
			//
			// copMax.x = lbl.x;
			// copMin.x = rfr.x;
			// if (cop.y > rfr.z)
			// copMin.x += (lfr.x - rfr.x) / (rfr.z - lfr.z)
			// * (rfr.z - cop.y);
			// if (cop.y < lbr.z)
			// copMax.x += (lbl.x - rbl.x) / (lbl.z - rbl.z)
			// * (lbl.z - cop.y);
			//
			// comMax.y = lfr.z;
			// comMin.y = rbr.z;
			// if (com.x > rbl.x)
			// comMin.y -= (lbl.z - rbl.z) / (rbl.x - lbl.x)
			// * (rbl.x - com.x);
			// if (com.x < lfr.x)
			// comMax.y -= (lfr.z - rfr.z) / (lfr.x - rfr.x)
			// * (lfr.x - com.x);
			//
			// comMax.x = lbl.x;
			// if (com.z < lbl.z)
			// comMax.x -= (lbl.x - rbl.x) / (rbl.z - lbl.z)
			// * (rbl.z - com.z);
			// comMax.x = Math.max(comMax.x, rfl.x);
			//
			// comMin.x = rfr.x;
			// if (com.z > rfr.z)
			// comMin.x -= (rfr.x - lfr.x) / (lfr.z - rfr.z )
			// * (lfr.z - com.z);
			// comMin.x = Math.min(comMin.x, lbr.x);
			// }
			if (rfl.z > lfl.z) {
				copMax.y = comMax.y = rfl.z;
				copMin.y = comMin.y = lbr.z;
			} else {
				copMax.y = comMax.y = lfl.z;
				copMin.y = comMin.y = rbr.z;
			}
			copMax.x = comMax.x = lfl.x;
			copMin.x = comMin.x = rbr.x;
		} else if (sc.isLeftOnGround()) {
			copMax.x = comMax.x = lfl.x;
			copMax.y = comMax.y = lfl.z;
			copMin.x = comMin.x = lbr.x;
			copMin.y = comMin.y = lbr.z;
		} else if (sc.isRightOnGround()) {
			copMax.x = comMax.x = rfl.x;
			copMax.y = comMax.y = rfl.z;
			copMin.x = comMin.x = rbr.x;
			copMin.y = comMin.y = rbr.z;
		}

		comLog.print(" ");
		comLog.print(comMax.x - gps.getX()); // 6
		// comLog.print(comMax.x ); // 6
		comLog.print(" ");
		comLog.print(comMin.x - gps.getX()); // 7
		// comLog.print(comMin.x); // 7
		comLog.print(" ");
		comLog.print(comMax.y + gps.getY()); // 8
		comLog.print(" ");
		comLog.print(comMin.y + gps.getY()); // 9

		comLog.print(" ");
		comLog.print(copMax.x + gps.getX()); // 10
		comLog.print(" ");
		comLog.print(copMin.x + gps.getX()); // 11
		comLog.print(" ");
		comLog.print(copMax.y + gps.getY()); // 12
		comLog.print(" ");
		comLog.print(copMin.y + gps.getY()); // 13
		comLog.println();

		gpsLog.print(currentStep);
		gpsLog.print(" ");
		gpsLog.print(gps.getX());
		gpsLog.print(" ");
		gpsLog.print(gps.getY());
		gpsLog.print(" ");
		gpsLog.print(gps.getZ());
		gpsLog.println();

		jointLog.flush();
		torqueLog.flush();
		comLog.flush();
		gpsLog.flush();
	}
}
