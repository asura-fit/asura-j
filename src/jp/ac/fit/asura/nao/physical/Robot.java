/*
 * 作成日: 2008/09/03
 */
package jp.ac.fit.asura.nao.physical;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.PressureSensor;
import jp.ac.fit.asura.nao.misc.CPair;

/**
 * Naoの関節定義. 右手直交座標系. すなわち、右手でx軸を親指、y軸を人差し指とするとz軸は中指の方向.
 *
 * @author sey
 *
 * @version $Id: Robot.java 717 2008-12-31 18:16:20Z sey $
 *
 */
public class Robot {
	public enum Frames {
		Body, HeadYaw, HeadPitch, CameraSelect, NaoCam, RShoulderPitch, RShoulderRoll, RElbowYaw, RElbowRoll, RHipYawPitch, RHipRoll, RHipPitch, RKneePitch, RAnklePitch, RAnkleRoll, RSole, RFsrFL, RFsrFR, RFsrBL, RFsrBR, LShoulderPitch, LShoulderRoll, LElbowYaw, LElbowRoll, LHipYawPitch, LHipRoll, LHipPitch, LKneePitch, LAnklePitch, LAnkleRoll, LSole, LFsrFL, LFsrFR, LFsrBL, LFsrBR;

		private static final EnumMap<Frames, Joint> f2j = new EnumMap<Frames, Joint>(
				Frames.class);
		private static final EnumMap<Joint, Frames> j2f = new EnumMap<Joint, Frames>(
				Joint.class);
		private static final EnumMap<Frames, PressureSensor> f2p = new EnumMap<Frames, PressureSensor>(
				Frames.class);
		private static final EnumMap<PressureSensor, Frames> p2f = new EnumMap<PressureSensor, Frames>(
				PressureSensor.class);

		static {
			for (Joint j : Joint.values()) {
				try {
					assert Frames.valueOf(j.name()) != null;
					f2j.put(Frames.valueOf(j.name()), j);
					j2f.put(j, Frames.valueOf(j.name()));
				} catch (IllegalArgumentException e) {
				}
			}
			for (PressureSensor p : PressureSensor.values()) {
				try {
					assert Frames.valueOf(p.name()) != null;
					f2p.put(Frames.valueOf(p.name()), p);
					p2f.put(p, Frames.valueOf(p.name()));
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
		public static Frames valueOf(Joint j) {
			return j2f.get(j);
		}

		public static Frames valueOf(PressureSensor p) {
			return p2f.get(p);
		}

		public Joint toJoint() {
			assert f2j.containsKey(this);
			return f2j.get(this);
		}

		public PressureSensor toPressureSensor() {
			assert f2p.containsKey(this);
			return f2p.get(this);
		}

		public boolean isJoint() {
			return f2j.containsKey(this);
		}

		public boolean isPressureSensor() {
			return f2p.containsKey(this);
		}
	}

	private EnumMap<Frames, RobotFrame> frames;

	private EnumMap<Frames, EnumMap<Frames, Frames[]>> routeCache;
	private EnumMap<Frames, EnumMap<Frames, Frames[]>> jointCache;

	public Robot(RobotFrame body) {
		frames = new EnumMap<Frames, RobotFrame>(Frames.class);
		constructRecur(body);
		routeCache = new EnumMap<Frames, EnumMap<Frames, Frames[]>>(
				Frames.class);
		jointCache = new EnumMap<Frames, EnumMap<Frames, Frames[]>>(
				Frames.class);
	}

	private void constructRecur(RobotFrame frame) {
		frames.put(frame.getId(), frame);
		for (RobotFrame child : frame.getChildren())
			constructRecur(child);
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

	public RobotFrame get(Frames key) {
		return frames.get(key);
	}

	/**
	 * @return the frames
	 */
	public Set<Frames> getFrames() {
		return frames.keySet();
	}

	/**
	 * フレームfromからフレームtoまでの経路を探索し、fromからtoまでの最短経路を返します.
	 *
	 * @param from
	 * @param to
	 * @return
	 */
	public Frames[] findRoute(Frames from, Frames to) {
		if (routeCache.containsKey(from)
				&& routeCache.get(from).containsKey(to)) {
			return routeCache.get(from).get(to);
		}

		RobotFrame fromObj = get(from);
		RobotFrame toObj = get(to);

		Queue<CPair<RobotFrame>> q = new LinkedList<CPair<RobotFrame>>();
		q.add(new CPair<RobotFrame>(fromObj, null));
		while (true) {
			// 閉路を考慮しない探索，Dijkstraのほうが良い
			CPair<RobotFrame> p = q.peek();

			if (p.first() == toObj)
				break;
			q.remove();
			if (p.first().getParent() != null)
				q.add(new CPair<RobotFrame>(p.first().getParent(), p));
			for (RobotFrame rf : p.first().getChildren())
				q.add(new CPair<RobotFrame>(rf, p));
		}

		// 結果の出力
		if (q.isEmpty())
			return null;
		CPair<RobotFrame> p = q.peek();
		LinkedList<Frames> list = new LinkedList<Frames>();
		do {
			list.addFirst(p.first().getId());
		} while ((p = p.second()) != null);
		Frames[] array = list.toArray(new Frames[0]);
		if (!routeCache.containsKey(from))
			routeCache.put(from, new EnumMap<Frames, Frames[]>(Frames.class));
		routeCache.get(from).put(to, array);
		return array;
	}

	/**
	 * フレームfromからフレームtoまでの最短経路のうち、可動関節のみを返します.
	 *
	 * @param from
	 * @param to
	 * @return
	 */
	public Frames[] findJointRoute(Frames from, Frames to) {
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
