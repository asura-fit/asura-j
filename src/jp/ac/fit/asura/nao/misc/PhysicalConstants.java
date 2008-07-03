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

		public static final Vector3f tHeadYaw2body = new Vector3f(0,
				0.16f * 1000, -0.02f * 1000);
		public static final AxisAngle4f rHeadYaw2body = new AxisAngle4f(0.0f,
				1.0f, 0.0f, 0.0f);

		public static final Vector3f tHeadPitch2yaw = new Vector3f(0,
				0.06f * 1000, 0.0f);
		public static final AxisAngle4f rHeadPitch2yaw = new AxisAngle4f(1.0f,
				0.0f, 0.0f, 0.0f);

		public static final Vector3f tCamera2headPitch = new Vector3f(0,
				0.03f * 1000, 0.058f * 1000);
		public static final AxisAngle4f rCamera2headPitch = new AxisAngle4f(
				0.0f, 1.0f, 0.0f, 0);
		// public static final AxisAngle4f rHeadPitch2camera = new AxisAngle4f(
		// 0.0f, 1.0f, 0.0f, (float) Math.PI);

		public static final Vector3f tRhipYawPitch2body = new Vector3f(
				-0.055f * 1000, -0.045f * 1000, -0.03f * 1000);
		public static final AxisAngle4f rRhipYawPitch2body = new AxisAngle4f(
				0.7071f, 0.7071f, 0.0f, 0.0f);

		public static final Vector3f tRhipRoll2yawPitch = new Vector3f();
		public static final AxisAngle4f rRhipRoll2yawPitch = new AxisAngle4f(
				0.0f, 0.0f, 1.0f, 0.0f);

		public static final Vector3f tRhipPitch2roll = new Vector3f();
		public static final AxisAngle4f rRhipPitch2roll = new AxisAngle4f(1.0f,
				0.0f, 0.0f, 0.0f);

		public static final Vector3f tRkneePitch2hipPitch = new Vector3f(0.0f,
				-0.12f * 1000, 0.005f * 1000);
		public static final AxisAngle4f rRkneePitch2hipPitch = new AxisAngle4f(
				1.0f, 0.0f, 0.0f, 0.0f);

		public static final Vector3f tRanklePitch2kneePitch = new Vector3f(
				0.0f, -0.1f * 1000, 0.0f);
		public static final AxisAngle4f rRanklePitch2kneePitch = new AxisAngle4f(
				1.0f, 0.0f, 0.0f, 0.0f);

		public static final Vector3f tRankleRoll2pitch = new Vector3f();
		public static final AxisAngle4f rRankleRoll2pitch = new AxisAngle4f(
				0.0f, 0.0f, 1.0f, 0.0f);

		public static final Vector3f tRsole2ankleRoll = new Vector3f(0.0f,
				-0.055f * 1000, 0.0f);
		public static final AxisAngle4f rRsole2ankleRoll = new AxisAngle4f(
				1.0f, 0.0f, 0.0f, 0.0f);

		public static final Vector3f tRfl2Sole = new Vector3f(0.02317f * 1000,
				0.0f, 0.06991f * 1000);
		public static final AxisAngle4f rRfl2Sole = new AxisAngle4f(1.0f, 0.0f,
				0.0f, (float) Math.PI / 2);

		public static final Vector3f tRfr2Sole = new Vector3f(-0.02998f * 1000,
				0.0f, 0.06993f * 1000);
		public static final AxisAngle4f rRfr2Sole = new AxisAngle4f(1.0f, 0.0f,
				0.0f, (float) Math.PI / 2);

		public static final Vector3f tRbl2Sole = new Vector3f(-0.02696f * 1000,
				0.0f, -0.03062f * 1000);
		public static final AxisAngle4f rRbl2Sole = new AxisAngle4f(1.0f, 0.0f,
				0.0f, (float) Math.PI / 2);

		public static final Vector3f tRbr2Sole = new Vector3f(0.01911f * 1000,
				0.0f, -0.03002f * 1000);
		public static final AxisAngle4f rRbr2Sole = new AxisAngle4f(1.0f, 0.0f,
				0.0f, (float) Math.PI / 2);

		public static final Vector3f tLhipYawPitch2body = new Vector3f(
				0.055f * 1000, -0.045f * 1000, -0.03f * 1000);
		public static final AxisAngle4f rLhipYawPitch2body = new AxisAngle4f(
				0.7071f, -0.7071f, 0.0f, 0.0f);

		public static final Vector3f tLhipRoll2yawPitch = new Vector3f();
		public static final AxisAngle4f rLhipRoll2yawPitch = new AxisAngle4f(
				0.0f, 0.0f, 1.0f, 0.0f);

		public static final Vector3f tLhipPitch2roll = new Vector3f();
		public static final AxisAngle4f rLhipPitch2roll = new AxisAngle4f(1.0f,
				0.0f, 0.0f, 0.0f);

		public static final Vector3f tLkneePitch2hipPitch = new Vector3f(0.0f,
				-0.12f * 1000, 0.005f * 1000);
		public static final AxisAngle4f rLkneePitch2hipPitch = new AxisAngle4f(
				1.0f, 0.0f, 0.0f, 0.0f);

		public static final Vector3f tLanklePitch2kneePitch = new Vector3f(
				0.0f, -0.1f * 1000, 0.0f);
		public static final AxisAngle4f rLanklePitch2kneePitch = new AxisAngle4f(
				1.0f, 0.0f, 0.0f, 0.0f);

		public static final Vector3f tLankleRoll2pitch = new Vector3f();
		public static final AxisAngle4f rLankleRoll2pitch = new AxisAngle4f(
				0.0f, 0.0f, 1.0f, 0.0f);

		public static final Vector3f tLsole2ankleRoll = new Vector3f(0.0f,
				-0.055f * 1000, 0.0f);
		public static final AxisAngle4f rLsole2ankleRoll = new AxisAngle4f(
				1.0f, 0.0f, 0.0f, 0.0f);

		public static final Vector3f tLfl2Sole = new Vector3f(0.02998f * 1000,
				0.0f, 0.06993f * 1000);
		public static final AxisAngle4f rLfl2Sole = new AxisAngle4f(1.0f, 0.0f,
				0.0f, (float) Math.PI / 2);

		public static final Vector3f tLfr2Sole = new Vector3f(-0.02317f * 1000,
				0.0f, 0.06991f * 1000);
		public static final AxisAngle4f rLfr2Sole = new AxisAngle4f(1.0f, 0.0f,
				0.0f, (float) Math.PI / 2);

		public static final Vector3f tLbl2Sole = new Vector3f(-0.01911f * 1000,
				0.0f, -0.03002f * 1000);
		public static final AxisAngle4f rLbl2Sole = new AxisAngle4f(1.0f, 0.0f,
				0.0f, (float) Math.PI / 2);

		public static final Vector3f tLbr2Sole = new Vector3f(0.02696f * 1000,
				0.0f, -0.03062f * 1000);
		public static final AxisAngle4f rLbr2Sole = new AxisAngle4f(1.0f, 0.0f,
				0.0f, (float) Math.PI / 2);
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
