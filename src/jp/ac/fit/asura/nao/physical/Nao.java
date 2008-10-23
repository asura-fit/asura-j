/*
 * 作成日: 2008/09/03
 */
package jp.ac.fit.asura.nao.physical;

import static jp.ac.fit.asura.nao.physical.Nao.Frames.Body;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.Camera;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.HeadPitch;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.HeadYaw;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.LAnklePitch;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.LAnkleRoll;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.LHipPitch;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.LHipRoll;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.LHipYawPitch;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.LKneePitch;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.LSole;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.LSoleBL;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.LSoleBR;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.LSoleFL;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.LSoleFR;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RAnklePitch;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RAnkleRoll;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RHipPitch;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RHipRoll;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RHipYawPitch;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RKneePitch;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RSole;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RSoleBL;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RSoleBR;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RSoleFL;
import static jp.ac.fit.asura.nao.physical.Nao.Frames.RSoleFR;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.misc.CPair;
import jp.ac.fit.asura.nao.misc.RobotFrame;

/**
 * Naoの関節定義. 右手直交座標系. すなわち、右手でx軸を親指、y軸を人差し指とするとz軸は中指の方向.
 *
 * @author sey
 *
 * @version $Id$
 *
 */
public class Nao {
	public enum Frames {
		Body, HeadYaw, HeadPitch, Camera, // RShoulderPitch, RShoulderRoll,
		// RElbowYaw, RElbowRoll,
		RHipYawPitch, RHipRoll, RHipPitch, RKneePitch, RAnklePitch, RAnkleRoll, RSole, RSoleFL, RSoleFR, RSoleBL, RSoleBR,
		// LShoulderPitch, LShoulderRoll, LElbowYaw, LElbowRoll,
		LHipYawPitch, LHipRoll, LHipPitch, LKneePitch, LAnklePitch, LAnkleRoll, LSole, LSoleFL, LSoleFR, LSoleBL, LSoleBR;

		private static final EnumMap<Frames, Joint> f2j = new EnumMap<Frames, Joint>(
				Frames.class);
		private static final EnumMap<Joint, Frames> j2f = new EnumMap<Joint, Frames>(
				Joint.class);

		static {
			for (Joint j : Joint.values()) {
				try {
					assert Frames.valueOf(j.name()) != null;
					f2j.put(Frames.valueOf(j.name()), j);
					j2f.put(j, Frames.valueOf(j.name()));
				} catch (IllegalArgumentException e) {

				}
			}
		}

		/**
		 * 与えられたJointに対応するFramesの列挙体を返します.
		 *
		 * @param id
		 * @return
		 */
		public Frames toFrame() {
			return j2f.get(this);
		}

		public Joint toJoint() {
			assert f2j.containsKey(this);
			return f2j.get(this);
		}

		public boolean isJoint() {
			return f2j.containsKey(this);
		}
	}

	private static final EnumMap<Frames, RobotFrame> frames = new EnumMap<Frames, RobotFrame>(
			Frames.class);

	static {
		for (Frames fr : Frames.values())
			frames.put(fr, new RobotFrame(fr));
	}

	/**
	 * Naoのロボット定義. translateは(親フレーム座標系での)親フレームからの移動量を示す.
	 * axisはこの関節の回転軸(オイラー軸)を示す. massはこの関節の重量を示す.
	 *
	 * <pre>
	 * 図的な意味はこんな感じ.
	 *          translate vector[mm]  axis and angle[rad]
	 * parent -----------------------&gt;@------------------ child
	 *                                &circ;
	 *                              mass[kg]
	 * </pre>
	 */
	static {
		RobotFrame body = frames.get(Body);
		body.parent = null;
		body.child = new RobotFrame[] { frames.get(HeadYaw),
				frames.get(RHipYawPitch), frames.get(LHipYawPitch) };
		body.translate = new Vector3f();
		body.axis = new AxisAngle4f(0.0f, 1.0f, 0.0f, 0.0f);
		body.mass = 1.2171f;
		frames.put(Body, body);
	}

	static {
		RobotFrame body2headYaw = frames.get(HeadYaw);
		body2headYaw.parent = frames.get(Body);
		body2headYaw.child = new RobotFrame[] { frames.get(HeadPitch) };
		body2headYaw.translate = new Vector3f(0, 0.16f, -0.02f);
		body2headYaw.translate.scale(1000);
		body2headYaw.axis = new AxisAngle4f(0.0f, 1.0f, 0.0f, 0.0f);
		body2headYaw.mass = 0.050f;

		RobotFrame headYaw2pitch = frames.get(HeadPitch);
		headYaw2pitch.parent = frames.get(HeadYaw);
		headYaw2pitch.child = new RobotFrame[] { frames.get(Camera) };
		headYaw2pitch.translate = new Vector3f(0, 0.06f, 0.0f);
		headYaw2pitch.translate.scale(1000);
		headYaw2pitch.axis = new AxisAngle4f(1.0f, 0.0f, 0.0f, 0.0f);
		headYaw2pitch.mass = 0.351f;

		RobotFrame headPitch2camera = frames.get(Camera);
		headPitch2camera.parent = frames.get(HeadPitch);
		headPitch2camera.child = new RobotFrame[] {};
		headPitch2camera.translate = new Vector3f(0, 0.03f, 0.058f);
		headPitch2camera.translate.scale(1000);
		headPitch2camera.axis = new AxisAngle4f(0.0f, 1.0f, 0.0f,
				(float) Math.PI);
		headPitch2camera.mass = 0;
	}

	static {
		RobotFrame rBody2hipYawPitch = frames.get(RHipYawPitch);
		rBody2hipYawPitch.parent = frames.get(Body);
		rBody2hipYawPitch.child = new RobotFrame[] { frames.get(RHipRoll) };
		rBody2hipYawPitch.translate = new Vector3f(-0.055f, -0.045f, -0.03f);
		rBody2hipYawPitch.translate.scale(1000);
		rBody2hipYawPitch.axis = new AxisAngle4f(0.7071f, 0.7071f, 0.0f, 0.0f);
		rBody2hipYawPitch.mass = 0.100f;

		RobotFrame rHipYawPitch2roll = frames.get(RHipRoll);
		rHipYawPitch2roll.parent = rBody2hipYawPitch;
		rHipYawPitch2roll.child = new RobotFrame[] { frames.get(RHipPitch) };
		rHipYawPitch2roll.translate = new Vector3f();
		rHipYawPitch2roll.axis = new AxisAngle4f(0.0f, 0.0f, 1.0f, 0.0f);
		rHipYawPitch2roll.mass = 0.140f;

		RobotFrame rHipRoll2pitch = frames.get(RHipPitch);
		rHipRoll2pitch.parent = rHipYawPitch2roll;
		rHipRoll2pitch.child = new RobotFrame[] { frames.get(RKneePitch) };
		rHipRoll2pitch.translate = new Vector3f();
		rHipRoll2pitch.axis = new AxisAngle4f(1.0f, 0.0f, 0.0f, 0.0f);
		rHipRoll2pitch.mass = 0.293f;

		RobotFrame rHipPitch2kneePitch = frames.get(RKneePitch);
		rHipPitch2kneePitch.parent = rHipRoll2pitch;
		rHipPitch2kneePitch.child = new RobotFrame[] { frames.get(RAnklePitch) };
		rHipPitch2kneePitch.translate = new Vector3f(0.0f, -0.12f, 0.005f);
		rHipPitch2kneePitch.translate.scale(1000);
		rHipPitch2kneePitch.axis = new AxisAngle4f(1.0f, 0.0f, 0.0f, 0.0f);
		rHipPitch2kneePitch.mass = 0.423f;

		RobotFrame rKneePitch2anklePitch = frames.get(RAnklePitch);
		rKneePitch2anklePitch.parent = rHipPitch2kneePitch;
		rKneePitch2anklePitch.child = new RobotFrame[] { frames.get(RAnkleRoll) };
		rKneePitch2anklePitch.translate = new Vector3f(0.0f, -0.1f, 0.0f);
		rKneePitch2anklePitch.translate.scale(1000);
		rKneePitch2anklePitch.axis = new AxisAngle4f(1.0f, 0.0f, 0.0f, 0.0f);
		rKneePitch2anklePitch.mass = 0.058f;

		RobotFrame rAnklePitch2roll = frames.get(RAnkleRoll);
		rAnklePitch2roll.parent = rKneePitch2anklePitch;
		rAnklePitch2roll.child = new RobotFrame[] { frames.get(RSole) };
		rAnklePitch2roll.translate = new Vector3f();
		rAnklePitch2roll.axis = new AxisAngle4f(0.0f, 0.0f, 1.0f, 0.0f);
		rAnklePitch2roll.mass = 0.100f;

		RobotFrame rAnkleRoll2sole = frames.get(RSole);
		rAnkleRoll2sole.parent = rAnklePitch2roll;
		rAnkleRoll2sole.child = new RobotFrame[] { frames.get(RSoleFL),
				frames.get(RSoleFR), frames.get(RSoleBL), frames.get(RSoleBR) };
		rAnkleRoll2sole.translate = new Vector3f(0.0f, -0.055f, 0.0f);
		rAnkleRoll2sole.translate.scale(1000);
		rAnkleRoll2sole.axis = new AxisAngle4f(1.0f, 0.0f, 0.0f, 0.0f);
		rAnkleRoll2sole.mass = 0;
	}

	static {
		RobotFrame rSole2fl = frames.get(RSoleFL);
		rSole2fl.parent = frames.get(RSole);
		rSole2fl.child = new RobotFrame[] {};
		rSole2fl.translate = new Vector3f(0.02317f, 0.0f, 0.06991f);
		rSole2fl.translate.scale(1000);
		rSole2fl.axis = new AxisAngle4f(1.0f, 0.0f, 0.0f, (float) Math.PI / 2);
		rSole2fl.mass = 0.01f;

		RobotFrame rSole2fr = frames.get(RSoleFR);
		rSole2fr.parent = frames.get(RSole);
		rSole2fr.child = new RobotFrame[] {};
		rSole2fr.translate = new Vector3f(-0.02998f, 0.0f, 0.06993f);
		rSole2fr.translate.scale(1000);
		rSole2fr.axis = new AxisAngle4f(1.0f, 0.0f, 0.0f, (float) Math.PI / 2);
		rSole2fr.mass = 0.01f;

		RobotFrame rSole2bl = frames.get(RSoleBL);
		rSole2bl.parent = frames.get(RSole);
		rSole2bl.child = new RobotFrame[] {};
		rSole2bl.translate = new Vector3f(-0.02696f, 0.0f, -0.03062f);
		rSole2bl.translate.scale(1000);
		rSole2bl.axis = new AxisAngle4f(1.0f, 0.0f, 0.0f, (float) Math.PI / 2);
		rSole2bl.mass = 0.01f;

		RobotFrame rSole2br = frames.get(RSoleBR);
		rSole2br.parent = frames.get(RSole);
		rSole2br.child = new RobotFrame[] {};
		rSole2br.translate = new Vector3f(0.01911f, 0.0f, -0.03002f);
		rSole2br.translate.scale(1000);
		rSole2br.axis = new AxisAngle4f(1.0f, 0.0f, 0.0f, (float) Math.PI / 2);
		rSole2br.mass = 0.01f;
	}

	static {
		RobotFrame lBody2hipYawPitch = frames.get(LHipYawPitch);
		lBody2hipYawPitch.parent = frames.get(Body);
		lBody2hipYawPitch.child = new RobotFrame[] { frames.get(LHipRoll) };
		lBody2hipYawPitch.translate = new Vector3f(0.055f, -0.045f, -0.03f);
		lBody2hipYawPitch.translate.scale(1000);
		lBody2hipYawPitch.axis = new AxisAngle4f(0.7071f, -0.7071f, 0.0f, 0.0f);
		lBody2hipYawPitch.mass = 0.100f;

		RobotFrame lHipYawPitch2roll = frames.get(LHipRoll);
		lHipYawPitch2roll.parent = lBody2hipYawPitch;
		lHipYawPitch2roll.child = new RobotFrame[] { frames.get(LHipPitch) };
		lHipYawPitch2roll.translate = new Vector3f();
		lHipYawPitch2roll.axis = new AxisAngle4f(0.0f, 0.0f, 1.0f, 0.0f);
		lHipYawPitch2roll.mass = 0.140f;

		RobotFrame lHipRoll2pitch = frames.get(LHipPitch);
		lHipRoll2pitch.parent = lHipYawPitch2roll;
		lHipRoll2pitch.child = new RobotFrame[] { frames.get(LKneePitch) };
		lHipRoll2pitch.translate = new Vector3f();
		lHipRoll2pitch.axis = new AxisAngle4f(1.0f, 0.0f, 0.0f, 0.0f);
		lHipRoll2pitch.mass = 0.293f;

		RobotFrame lHipPitch2kneePitch = frames.get(LKneePitch);
		lHipPitch2kneePitch.parent = lHipRoll2pitch;
		lHipPitch2kneePitch.child = new RobotFrame[] { frames.get(LAnklePitch) };
		lHipPitch2kneePitch.translate = new Vector3f(0.0f, -0.12f, 0.005f);
		lHipPitch2kneePitch.translate.scale(1000);
		lHipPitch2kneePitch.axis = new AxisAngle4f(1.0f, 0.0f, 0.0f, 0.0f);
		lHipPitch2kneePitch.mass = 0.423f;

		RobotFrame lKneePitch2anklePitch = frames.get(LAnklePitch);
		lKneePitch2anklePitch.parent = lHipPitch2kneePitch;
		lKneePitch2anklePitch.child = new RobotFrame[] { frames.get(LAnkleRoll) };
		lKneePitch2anklePitch.translate = new Vector3f(0.0f, -0.1f, 0.0f);
		lKneePitch2anklePitch.translate.scale(1000);
		lKneePitch2anklePitch.axis = new AxisAngle4f(1.0f, 0.0f, 0.0f, 0.0f);
		lKneePitch2anklePitch.mass = 0.058f;

		RobotFrame lAnklePitch2roll = frames.get(LAnkleRoll);
		lAnklePitch2roll.parent = lKneePitch2anklePitch;
		lAnklePitch2roll.child = new RobotFrame[] { frames.get(LSole) };
		lAnklePitch2roll.translate = new Vector3f();
		lAnklePitch2roll.axis = new AxisAngle4f(0.0f, 0.0f, 1.0f, 0.0f);
		lAnklePitch2roll.mass = 0.100f;

		RobotFrame lAnkleRoll2sole = frames.get(LSole);
		lAnkleRoll2sole.parent = lAnklePitch2roll;
		lAnkleRoll2sole.child = new RobotFrame[] { frames.get(LSoleFL),
				frames.get(LSoleFR), frames.get(LSoleBL), frames.get(LSoleBR) };
		lAnkleRoll2sole.translate = new Vector3f(0.0f, -0.055f, 0.0f);
		lAnkleRoll2sole.translate.scale(1000);
		lAnkleRoll2sole.axis = new AxisAngle4f(1.0f, 0.0f, 0.0f, 0.0f);
		lAnkleRoll2sole.mass = 0;
	}

	static {
		RobotFrame lSole2fl = frames.get(LSoleFL);
		lSole2fl.parent = frames.get(LSole);
		lSole2fl.translate = new Vector3f(0.02998f, 0.0f, 0.06993f);
		lSole2fl.translate.scale(1000);
		lSole2fl.axis = new AxisAngle4f(1.0f, 0.0f, 0.0f, (float) Math.PI / 2);
		lSole2fl.mass = 0.01f;

		RobotFrame lSole2fr = frames.get(LSoleFR);
		lSole2fr.parent = frames.get(LSole);
		lSole2fr.translate = new Vector3f(-0.02317f, 0.0f, 0.06991f);
		lSole2fr.translate.scale(1000);
		lSole2fr.axis = new AxisAngle4f(1.0f, 0.0f, 0.0f, (float) Math.PI / 2);
		lSole2fr.mass = 0.01f;

		RobotFrame lSole2bl = frames.get(LSoleBL);
		lSole2bl.parent = frames.get(LSole);
		lSole2bl.translate = new Vector3f(-0.01911f, 0.0f, -0.03002f);
		lSole2bl.translate.scale(1000);
		lSole2bl.axis = new AxisAngle4f(1.0f, 0.0f, 0.0f, (float) Math.PI / 2);
		lSole2bl.mass = 0.01f;

		RobotFrame lSole2br = frames.get(LSoleBR);
		lSole2br.parent = frames.get(LSole);
		lSole2br.translate = new Vector3f(0.02696f, 0.0f, -0.03062f);
		lSole2br.translate.scale(1000);
		lSole2br.axis = new AxisAngle4f(1.0f, 0.0f, 0.0f, (float) Math.PI / 2);
		lSole2br.mass = 0.01f;
	}

	public static RobotFrame get(Frames key) {
		return frames.get(key);
	}

	private final static EnumMap<Frames, EnumMap<Frames, Frames[]>> cache = new EnumMap<Frames, EnumMap<Frames, Frames[]>>(
			Frames.class);
	private final static EnumMap<Frames, EnumMap<Frames, Frames[]>> jointCache = new EnumMap<Frames, EnumMap<Frames, Frames[]>>(
			Frames.class);

	/**
	 * フレームfromからフレームtoまでの経路を探索し、fromからtoまでの最短経路を返します.
	 *
	 * @param from
	 * @param to
	 * @return
	 */
	public static Frames[] findRoute(Frames from, Frames to) {
		if (cache.containsKey(from) && cache.get(from).containsKey(to)) {
			return cache.get(from).get(to);
		}

		RobotFrame fromObj = Nao.get(from);
		RobotFrame toObj = Nao.get(to);

		Queue<CPair<RobotFrame>> q = new LinkedList<CPair<RobotFrame>>();
		q.add(new CPair<RobotFrame>(fromObj, null));
		while (true) {
			// 閉路を考慮しない探索，Dijkstraのほうが良い
			CPair<RobotFrame> p = q.peek();

			if (p.first() == toObj)
				break;
			q.remove();
			if (p.first().parent != null)
				q.add(new CPair<RobotFrame>(p.first().parent, p));
			if (p.first().child != null)
				for (RobotFrame rf : p.first().child)
					q.add(new CPair<RobotFrame>(rf, p));
		}

		// 結果の出力
		if (q.isEmpty())
			return null;
		CPair<RobotFrame> p = q.peek();
		LinkedList<Frames> list = new LinkedList<Frames>();
		do {
			list.addFirst(p.first().id);
		} while ((p = p.second()) != null);
		Frames[] array = list.toArray(new Frames[0]);
		if (!cache.containsKey(from))
			cache.put(from, new EnumMap<Frames, Frames[]>(Frames.class));
		cache.get(from).put(to, array);
		return array;
	}

	/**
	 * フレームfromからフレームtoまでの最短経路のうち、可動関節のみを返します.
	 *
	 * @param from
	 * @param to
	 * @return
	 */
	public static Frames[] findJointRoute(Frames from, Frames to) {
		if (jointCache.containsKey(from)
				&& jointCache.get(from).containsKey(to)) {
			return jointCache.get(from).get(to);
		}

		Frames[] original = findRoute(from, to);
		List<Frames> list = new ArrayList<Frames>();
		for (Frames f : original)
			if (f.isJoint())
				list.add(f);

		Frames[] result = list.toArray(new Frames[0]);
		if (!jointCache.containsKey(from))
			jointCache.put(from, new EnumMap<Frames, Frames[]>(Frames.class));
		jointCache.get(from).put(to, result);
		return result;
	}
}
