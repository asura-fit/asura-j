/*
 * 作成日: 2008/06/21
 */
package jp.ac.fit.asura.nao.misc;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class PhysicalConstants {
	public static class Ball {
		// mm
		public static final int Radius = 43;
	}

	public static class Nao {
		// mm
		public static final int CameraHeight = 530;

		// HipからHeadYawまでの長さ
		public static final int BodyLength = 40 + 160;

		public static final RobotFrame camera2headPitch = new RobotFrame(0,
				new Vector3f(0, 0.03f * 1000, 0.058f * 1000), new AxisAngle4f(
						0.0f, 1.0f, 0.0f, 0));

		public static final RobotFrame headPitch2yaw = new RobotFrame(1,
				new Vector3f(0, 0.06f * 1000, 0.0f), new AxisAngle4f(1.0f,
						0.0f, 0.0f, 0.0f));

		public static final RobotFrame headYaw2body = new RobotFrame(2,
				new Vector3f(0, 0.16f * 1000, -0.02f * 1000), new AxisAngle4f(
						0.0f, 1.0f, 0.0f, 0.0f));

		// public static final AxisAngle4f rHeadPitch2camera = new AxisAngle4f(
		// 0.0f, 1.0f, 0.0f, (float) Math.PI);

		public static final RobotFrame rFL2Sole = new RobotFrame(3,
				new Vector3f(0.02317f * 1000, 0.0f, 0.06991f * 1000),
				new AxisAngle4f(1.0f, 0.0f, 0.0f, (float) Math.PI / 2));

		public static final RobotFrame rFR2Sole = new RobotFrame(4,
				new Vector3f(-0.02998f * 1000, 0.0f, 0.06993f * 1000),
				new AxisAngle4f(1.0f, 0.0f, 0.0f, (float) Math.PI / 2));

		public static final RobotFrame rBL2Sole = new RobotFrame(5,
				new Vector3f(-0.02696f * 1000, 0.0f, -0.03062f * 1000),
				new AxisAngle4f(1.0f, 0.0f, 0.0f, (float) Math.PI / 2));

		public static final RobotFrame rBR2Sole = new RobotFrame(6,
				new Vector3f(0.01911f * 1000, 0.0f, -0.03002f * 1000),
				new AxisAngle4f(1.0f, 0.0f, 0.0f, (float) Math.PI / 2));

		public static final RobotFrame rSole2ankleRoll = new RobotFrame(7,
				new Vector3f(0.0f, -0.055f * 1000, 0.0f), new AxisAngle4f(1.0f,
						0.0f, 0.0f, 0.0f));
		public static final RobotFrame rAnkleRoll2pitch = new RobotFrame(8,
				new Vector3f(), new AxisAngle4f(0.0f, 0.0f, 1.0f, 0.0f));

		public static final RobotFrame rAnklePitch2kneePitch = new RobotFrame(
				9, new Vector3f(0.0f, -0.1f * 1000, 0.0f), new AxisAngle4f(
						1.0f, 0.0f, 0.0f, 0.0f));

		public static final RobotFrame rKneePitch2hipPitch = new RobotFrame(10,
				new Vector3f(0.0f, -0.12f * 1000, 0.005f * 1000),
				new AxisAngle4f(1.0f, 0.0f, 0.0f, 0.0f));
		public static final RobotFrame rHipPitch2roll = new RobotFrame(11,
				new Vector3f(), new AxisAngle4f(1.0f, 0.0f, 0.0f, 0.0f));

		public static final RobotFrame rHipRoll2yawPitch = new RobotFrame(12,
				new Vector3f(), new AxisAngle4f(0.0f, 0.0f, 1.0f, 0.0f));
		public static final RobotFrame rHipYawPitch2body = new RobotFrame(13,
				new Vector3f(-0.055f * 1000, -0.045f * 1000, -0.03f * 1000),
				new AxisAngle4f(0.7071f, 0.7071f, 0.0f, 0.0f));

		public static final RobotFrame lFL2Sole = new RobotFrame(14,
				new Vector3f(0.02998f * 1000, 0.0f, 0.06993f * 1000),
				new AxisAngle4f(1.0f, 0.0f, 0.0f, (float) Math.PI / 2));

		public static final RobotFrame lFR2Sole = new RobotFrame(15,
				new Vector3f(-0.02317f * 1000, 0.0f, 0.06991f * 1000),
				new AxisAngle4f(1.0f, 0.0f, 0.0f, (float) Math.PI / 2));

		public static final RobotFrame lBL2Sole = new RobotFrame(16,
				new Vector3f(-0.01911f * 1000, 0.0f, -0.03002f * 1000),
				new AxisAngle4f(1.0f, 0.0f, 0.0f, (float) Math.PI / 2));

		public static final RobotFrame lBR2Sole = new RobotFrame(17,
				new Vector3f(0.02696f * 1000, 0.0f, -0.03062f * 1000),
				new AxisAngle4f(1.0f, 0.0f, 0.0f, (float) Math.PI / 2));

		public static final RobotFrame lSole2ankleRoll = new RobotFrame(18,
				new Vector3f(0.0f, -0.055f * 1000, 0.0f), new AxisAngle4f(1.0f,
						0.0f, 0.0f, 0.0f));
		public static final RobotFrame lAnkleRoll2pitch = new RobotFrame(19,
				new Vector3f(), new AxisAngle4f(0.0f, 0.0f, 1.0f, 0.0f));
		public static final RobotFrame lAnklePitch2kneePitch = new RobotFrame(
				13, new Vector3f(0.0f, -0.1f * 1000, 0.0f), new AxisAngle4f(
						1.0f, 0.0f, 0.0f, 0.0f));
		public static final RobotFrame lKneePitch2hipPitch = new RobotFrame(20,
				new Vector3f(0.0f, -0.12f * 1000, 0.005f * 1000),
				new AxisAngle4f(1.0f, 0.0f, 0.0f, 0.0f));

		public static final RobotFrame lHipPitch2roll = new RobotFrame(21,
				new Vector3f(), new AxisAngle4f(1.0f, 0.0f, 0.0f, 0.0f));

		public static final RobotFrame lHipRoll2yawPitch = new RobotFrame(22,
				new Vector3f(), new AxisAngle4f(0.0f, 0.0f, 1.0f, 0.0f));

		public static final RobotFrame lHipYawPitch2body = new RobotFrame(23,
				new Vector3f(0.055f * 1000, -0.045f * 1000, -0.03f * 1000),
				new AxisAngle4f(0.7071f, -0.7071f, 0.0f, 0.0f));
	}

	public static class Goal {
		public static final int Height = 800;
		public static final int FullWidth = 1500;
		public static final int HalfWidth = FullWidth / 2;
		public static final int PoleRadius = 50;

		public static final int YellowGoalX = 0;
		public static final int YellowGoalY = -2700;
		public static final int BlueGoalX = 0;
		public static final int BlueGoalY = 2700;
	}

	public static class Field {
		public static final int MaxX = 2000;
		public static final int MinX = -2000;
		public static final int MaxY = 3000;
		public static final int MinY = -3000;
	}
}
