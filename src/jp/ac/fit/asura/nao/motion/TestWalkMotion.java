/*
 * 作成日: 2008/10/26
 */
package jp.ac.fit.asura.nao.motion;

import javax.vecmath.AxisAngle4f;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.misc.Kinematics;
import jp.ac.fit.asura.nao.physical.Nao.Frames;
import jp.ac.fit.asura.nao.sensation.FrameState;
import jp.ac.fit.asura.nao.sensation.SomaticContext;
import jp.ac.fit.asura.nao.sensation.SomatoSensoryCortex;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class TestWalkMotion extends Motion {
	public static final int TESTWALK_MOTION = 5963;

	private double t = 0;
	FrameState lar;
	FrameState rar;
	SomatoSensoryCortex ssc;

	float[] ip = new float[Joint.values().length];

	public TestWalkMotion(int id) {
		setId(id);
	}

	public void init(RobotContext context) {
		ssc = context.getSensoryCortex();
	}

	public void start() {
		t = 0;
		lar = new FrameState(Frames.LAnkleRoll);
		rar = new FrameState(Frames.RAnkleRoll);
		lar.getRobotRotation().set(
				new AxisAngle4f(1, 0, 0, (float) Math.toRadians(-5)));
		rar.getRobotRotation().set(
				new AxisAngle4f(1, 0, 0, (float) Math.toRadians(-5)));
	}

	@Override
	public float[] stepNextFrame(float[] current) {
		System.arraycopy(current, 0, ip, 0, ip.length);
		SomaticContext sc = new SomaticContext(ssc.getContext());
		Kinematics.calculateForward(sc);

		int dy = 10;
		int dz = 15;

		double u = currentStep > 20 ? 1 : currentStep / 20.0;

		t += (35 * Math.PI / 180.0) * u;

		double phase = u * Math.PI + (1 - u) * t;

		lar.getRobotPosition().set(50, (float) (-220 + dy * Math.cos(t)),
				(float) (-5 + dz * Math.sin(t)));
		rar.getRobotPosition().set(-50,
				(float) (-220 + dy * Math.cos(t - phase)),
				(float) (-5 + dz * Math.sin(t - phase)));

		// 最初に取得した値を目標に逆運動学計算
		Kinematics.calculateInverse(sc, lar);
		Kinematics.calculateInverse(sc, rar);
		ip[Joint.LHipYawPitch.ordinal()] = sc.get(Frames.LHipYawPitch)
				.getAngle();
		ip[Joint.LHipPitch.ordinal()] = sc.get(Frames.LHipPitch).getAngle();
		ip[Joint.LHipRoll.ordinal()] = sc.get(Frames.LHipRoll).getAngle();
		ip[Joint.LKneePitch.ordinal()] = sc.get(Frames.LKneePitch).getAngle();
		ip[Joint.LAnklePitch.ordinal()] = sc.get(Frames.LAnklePitch).getAngle();
		ip[Joint.LAnkleRoll.ordinal()] = sc.get(Frames.LAnkleRoll).getAngle();
		ip[Joint.RHipYawPitch.ordinal()] = sc.get(Frames.RHipYawPitch)
				.getAngle();
		ip[Joint.RHipPitch.ordinal()] = sc.get(Frames.RHipPitch).getAngle();
		ip[Joint.RHipRoll.ordinal()] = sc.get(Frames.RHipRoll).getAngle();
		ip[Joint.RKneePitch.ordinal()] = sc.get(Frames.RKneePitch).getAngle();
		ip[Joint.RAnklePitch.ordinal()] = sc.get(Frames.RAnklePitch).getAngle();
		ip[Joint.RAnkleRoll.ordinal()] = sc.get(Frames.RAnkleRoll).getAngle();
		currentStep++;
		return ip;
	}

	public boolean canStop() {
		return true;
	}

	public boolean hasNextStep() {
		return true;
	}
}
