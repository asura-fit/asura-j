/*
 * 作成日: 2008/10/26
 */
package jp.ac.fit.asura.nao.motion.motions;

import static jp.ac.fit.asura.nao.motion.motions.BasicWalk.Leg.LEFT;
import static jp.ac.fit.asura.nao.motion.motions.BasicWalk.Leg.RIGHT;
import static jp.ac.fit.asura.nao.motion.motions.BasicWalk.WalkState.READY;
import static jp.ac.fit.asura.nao.motion.motions.BasicWalk.WalkState.START;
import static jp.ac.fit.asura.nao.motion.motions.BasicWalk.WalkState.STOP;
import static jp.ac.fit.asura.nao.motion.motions.BasicWalk.WalkState.SWING;
import static jp.ac.fit.asura.nao.motion.motions.BasicWalk.WalkState.SWING_BEGIN;
import static jp.ac.fit.asura.nao.motion.motions.BasicWalk.WalkState.SWING_END;

import java.awt.Polygon;
import java.io.IOException;
import java.io.PrintWriter;

import javax.vecmath.Matrix3f;
import javax.vecmath.Point2f;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.Effector;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.SensorContext;
import jp.ac.fit.asura.nao.event.MotionEventListener;
import jp.ac.fit.asura.nao.localization.self.GPSLocalization;
import jp.ac.fit.asura.nao.misc.Coordinates;
import jp.ac.fit.asura.nao.misc.Kinematics;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.misc.MatrixUtils;
import jp.ac.fit.asura.nao.misc.SingularPostureException;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.MotionParam;
import jp.ac.fit.asura.nao.motion.MotionParam.WalkParam;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.FrameState;
import jp.ac.fit.asura.nao.sensation.SomaticContext;
import jp.ac.fit.asura.vecmathx.GfVector;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 *
 * @version $Id: TestWalkMotion.java 721 2009-02-18 03:40:44Z sey $
 *
 */
public class BasicWalk extends Motion {
	private static final Logger log = Logger.getLogger(BasicWalk.class);

	enum WalkState {
		START, READY, SWING_BEGIN, SWING, SWING_END, SWING_RIGHT_END, STOP
	}

	enum Leg {
		LEFT, RIGHT
	}

	private Effector effector;

	// 歩行の状態
	private WalkState state;

	// フレームカウント. 歩行状態切替ごとにリセットされる
	int stateCount;

	// 歩数計, 何歩歩いたか. 人間でいう半歩で一歩とカウント
	int pedometer;

	// 現在の遊脚. LEFT or RIGHT
	private Leg swingLeg;
	// 現在の支持脚. LEFT or RIGHT
	private Leg supportLeg;

	// モーション停止要求があったか
	private boolean stopRequested;

	// モーションを継続できるか(停止できるか)
	private boolean hasNextStep;

	// 一歩あたりの歩幅(mm), パラメータによるが70ぐらいが限界.
	float maxStride = 65;

	float maxTurn = MathUtils.toRadians(-15);

	// 基本となる腰の高さ(mm). 高すぎても低すぎてもダメ
	float baseHeight = 265;

	// 基本となる足の横幅(mm). 着地時の腰からの位置.
	float baseWidth = 70;

	// どれぐらい足を上げるか(mm)
	float footHeight = 15;

	// 一歩にかかる時間(フレーム)
	float walkCycle = 15;

	// 体重移動するときの速度の限界. 大きいと不安定になる?
	float leanLimit = 20;

	// 体重移動するときの重心位置オフセット.
	float comOffsetX = 5;

	float targetHeight;
	GPSLocalization gps = new GPSLocalization();
	Sensor sensor;

	// オドメトリデータ(x: forward, y:left, z:turnCCW)
	Vector3f odometer = new Vector3f();

	WalkParam param;

	public BasicWalk() {
		setName("BasicWalk");
		param = new WalkParam();
	}

	@Override
	public void init(RobotContext context) {
		sensor = context.getSensor();
		effector = context.getEffector();
		gps.init(context);
	}

	/**
	 * モーションの開始・初期化
	 */
	@Override
	public void start(MotionParam param) {
		log.debug("start testwalk");

		changeState(START);
		hasNextStep = true;
		stopRequested = false;
		pedometer = 0;
		odometer.set(0, 0, 0);

		if (param != null) {
			assert param instanceof MotionParam.WalkParam;
			WalkParam walkp = (WalkParam) param;
			// TODO 歩行量のパラメータを使ってなんかする
			this.param.set(walkp);
			float f = walkp.getForward();
			float l = walkp.getLeft();
			float t = walkp.getTurn();
			setName("BasikWalk f:" + f + " l:" + l + " t:" + t);
		}
	}

	/**
	 * 歩行のメインループ
	 */
	@Override
	public void step() {
		log.debug(currentStep + " step testwalk");

		SomaticContext sc = context.getSomaticContext();

		// デバッグ時は関節値やCOMのログをとる
		if (log.isDebugEnabled())
			archive(sc);

		try {
			SWITCH: do {
				switch (state) {
				case START:
					// はじめ
					changeState(READY);
					// 右足から振り始める
					supportLeg = LEFT;
					swingLeg = RIGHT;
					continue SWITCH;

				case READY:
					// 歩行姿勢に移る
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

					if (stopRequested)
						changeState(STOP);

					// 重心が移ったら足を振る
					if (canSwingLeg()) {
						changeState(SWING);
						continue SWITCH;
					}
					// 支持脚に重心を移す
					leanSupportLeg();
					break;

				case SWING:
					// 遊足を前へ
					if (!forwardSwingLeg()) {
						// if (!turnSwingLeg()) {
						changeState(SWING_END);
						continue SWITCH;
					}
					break;

				case SWING_END:
					// 歩行の終わり
					pedometer++;
					if (!stopRequested && pedometer < param.getPedometer()) {
						// 歩行を続ける
						changeSupportLeg();
						changeState(SWING_BEGIN);
					} else {
						// やめる
						changeState(STOP);
					}
					continue SWITCH;
				}
			} while (false);
		} catch (SingularPostureException spe) {
			// 歩行を継続できそうにない
			log.error("", spe);
			changeState(STOP);
		}
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
		} else {
			addSolePoint(polygon, Frames.RFsrFL);
			addSolePoint(polygon, Frames.RFsrFR);
			addSolePoint(polygon, Frames.RFsrBR);
			addSolePoint(polygon, Frames.RFsrBL);
			com.x += comOffsetX;
		}

		log.trace("canSwingLeg com:" + com + " poly:" + polygon);
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
		FrameState lar = sc.get(Frames.LAnkleRoll).clone();
		FrameState rar = sc.get(Frames.RAnkleRoll).clone();

		float ldy = -baseHeight - lar.getBodyPosition().y;
		float rdy = -baseHeight - rar.getBodyPosition().y;
		lar.getBodyPosition().z = 0;
		rar.getBodyPosition().z = 0;
		lar.getBodyRotation().setIdentity();
		rar.getBodyRotation().setIdentity();

		lar.getBodyPosition().x = sc.get(Frames.LHipYawPitch).getPosition().x;
		rar.getBodyPosition().x = sc.get(Frames.RHipYawPitch).getPosition().x;

		lar.getBodyPosition().y += MathUtils.clipAbs(ldy, 12.0f);
		rar.getBodyPosition().y += MathUtils.clipAbs(rdy, 12.0f);

		Kinematics.calculateInverse(sc, lar);
		Kinematics.calculateInverse(sc, rar);
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

		// 最初に取得した値を目標に逆運動学計算
		Kinematics.calculateInverse(sc, lar);
		Kinematics.calculateInverse(sc, rar);
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

		if (stopRequested && stateCount < 3) {
			// 3ステップ未満ならキャンセル可能?
			return false;
		}

		float dx = (float) (maxStride * (-Math.cos(theta1) + Math.cos(theta2)));
		float dy = (float) (footHeight * (Math.sin(theta1) - Math.sin(theta2)));

		log.debug("dx:" + dx + " dy:" + dy + " pedometer:" + pedometer);
		if (pedometer >= 1 && !stopRequested) {
			// 二歩目以降
			swing.getBodyPosition().z += dx;
			support.getBodyPosition().z -= dx;
			odometer.x += dx;
		} else {
			swing.getBodyPosition().z += dx / 2;
			support.getBodyPosition().z -= dx / 2;
			odometer.x += dx / 2;
		}

		targetHeight += dy;
		swing.getBodyPosition().y = targetHeight;

		if (swingLeg == Leg.LEFT)
			swing.getBodyPosition().x = baseWidth;
		else
			swing.getBodyPosition().x = -baseWidth;

		lar.getBodyRotation().setIdentity();
		rar.getBodyRotation().setIdentity();

		// 最初に取得した値を目標に逆運動学計算
		Kinematics.calculateInverse(sc, lar);
		Kinematics.calculateInverse(sc, rar);
		copyToOut(sc, effector);
		return true;
	}

	private boolean turnSwingLeg() {
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

		float dw = (float) (maxTurn * (-Math.cos(theta1) + Math.cos(theta2)));
		float dy = (float) (footHeight * (Math.sin(theta1) - Math.sin(theta2)));

		log.debug("dw:" + dw + " dy:" + dy + " pedometer:" + pedometer);
		if (pedometer % 2 == 0) {
			Matrix3f rot = new Matrix3f();
			MatrixUtils.pyr2rot(new Vector3f(0, dw, 0), rot);
			swing.getBodyRotation().mul(rot);

			MatrixUtils.pyr2rot(new Vector3f(0, -dw / 2, 0), rot);
			support.getBodyRotation().mul(rot);
		} else {
			Matrix3f rot = new Matrix3f();
			MatrixUtils.pyr2rot(new Vector3f(0, -dw, 0), rot);
			support.getBodyRotation().mul(rot);
		}

		targetHeight += dy;
		swing.getBodyPosition().y = targetHeight;

		// if (swingLeg == Leg.LEFT)
		// swing.getBodyPosition().x = targetWidth;
		// else
		// swing.getBodyPosition().x = -targetWidth;

		// support.getBodyRotation().setIdentity();

		// 最初に取得した値を目標に逆運動学計算
		// Kinematics.calculateInverse(sc, support);
		Kinematics.calculateInverse(sc, swing);

		// if (swingLeg == Leg.LEFT)
		// Kinematics.calculateInverse(sc, Frames.LAnkleRoll, swing);
		// else
		// Kinematics.calculateInverse(sc, Frames.RAnkleRoll, swing);
		GfVector w = new GfVector(new float[] { 1, 1, 1, 1, 1, 1 });
		if (swingLeg == Leg.LEFT)
			Kinematics.calculateInverse(sc, Frames.RAnkleRoll, support, w);
		else
			Kinematics.calculateInverse(sc, Frames.LAnkleRoll, support, w);
		copyToOut(sc, effector);
		return true;
	}

	private void changeState(WalkState s) {
		log.debug(currentStep + " change state to " + s);
		state = s;
		stateCount = 0;
	}

	private void copyToOut(SomaticContext sc, Effector effector) {
		// 実機にあわせてYawPitchは共通に
		float yp;
		if (supportLeg == RIGHT)
			yp = sc.get(Frames.LHipYawPitch).getAngle();
		else
			yp = sc.get(Frames.RHipYawPitch).getAngle();
		effector.setJoint(Joint.RHipYawPitch, yp);
		effector.setJoint(Joint.LHipYawPitch, yp);

		effector.setJoint(Joint.LHipPitch, sc.get(Frames.LHipPitch).getAngle());
		effector.setJoint(Joint.LHipRoll, sc.get(Frames.LHipRoll).getAngle());
		effector.setJoint(Joint.LKneePitch, sc.get(Frames.LKneePitch)
				.getAngle());
		effector.setJoint(Joint.LAnklePitch, sc.get(Frames.LAnklePitch)
				.getAngle());
		effector.setJoint(Joint.LAnkleRoll, sc.get(Frames.LAnkleRoll)
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

	@Override
	public boolean hasOdometer() {
		return true;
	}

	@Override
	public void updateOdometry(MotionEventListener listener) {
		listener.updateOdometry(odometer.x, odometer.y, odometer.z);
		odometer.set(0, 0, 0);
	}

	PrintWriter jointLog = null;
	PrintWriter torqueLog = null;
	PrintWriter comLog = null;
	PrintWriter gpsLog = null;
	PrintWriter posLog = null;

	/**
	 * 動作ログの記録
	 *
	 * @param sc
	 */
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
			if (posLog == null)
				posLog = new PrintWriter("pos.log");
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

		if (sc.isLeftOnGround() && sc.isRightOnGround() || state == SWING_BEGIN) {
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

		posLog.print(currentStep);
		posLog.print(" ");
		posLog.print(sc.get(Frames.LAnkleRoll).getBodyPosition().x);
		posLog.print(" ");
		posLog.print(sc.get(Frames.LAnkleRoll).getBodyPosition().y);
		posLog.print(" ");
		posLog.print(sc.get(Frames.LAnkleRoll).getBodyPosition().z);
		posLog.print(" ");
		posLog.print(sc.get(Frames.RAnkleRoll).getBodyPosition().x);
		posLog.print(" ");
		posLog.print(sc.get(Frames.RAnkleRoll).getBodyPosition().y);
		posLog.print(" ");
		posLog.print(sc.get(Frames.RAnkleRoll).getBodyPosition().z);
		posLog.println();

		jointLog.flush();
		torqueLog.flush();
		comLog.flush();
		gpsLog.flush();
		posLog.flush();
	}
}
