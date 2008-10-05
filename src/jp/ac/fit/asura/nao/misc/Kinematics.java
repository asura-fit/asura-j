/*
 * 作成日: 2008/10/03
 */
package jp.ac.fit.asura.nao.misc;

import javax.vecmath.GMatrix;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.physical.Nao;
import jp.ac.fit.asura.nao.physical.Nao.Frames;
import jp.ac.fit.asura.nao.sensation.FrameState;
import jp.ac.fit.asura.nao.sensation.SomaticContext;

/**
 * 運動学/逆運動学計算.
 * 
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public class Kinematics {
	public static GMatrix calculateJacobian(Frames from, Frames to,
			SomaticContext joints) {
		Frames[] route = Nao.findRoute(from, to);
		assert route != null;

		GMatrix mat = new GMatrix(6, route.length);

		// JointStateから取得する
		FrameState endFrame = joints.get(to);
		Vector3f end = endFrame.getRobotPosition();

		// Bodyから基準座標系への位置ベクトルを，すべての位置ベクトルに足す
		// ことで，基準座標系からの絶対位置を表現する

		for (int i = 0; i < route.length; i++) {
			FrameState fs = joints.get(route[i]);

			// このフレームの座標
			Vector3f pos = fs.getRobotPosition();
			Vector3f deltaPos = new Vector3f(end);
			deltaPos.sub(pos);
			// dPos = end - position(i)

			// このへんちょっと怪しい parent使わないとだめかも
			Vector3f zi = new Vector3f();
			zi.x = fs.getRobotRotation().m02;
			zi.y = fs.getRobotRotation().m12;
			zi.z = fs.getRobotRotation().m22;

			mat.setElement(3, i, zi.x);
			mat.setElement(4, i, zi.y);
			mat.setElement(5, i, zi.z);

			Vector3f cross = zi;
			cross.cross(zi, deltaPos);
			mat.setElement(0, i, cross.x);
			mat.setElement(1, i, cross.y);
			mat.setElement(2, i, cross.z);

		}

		return mat;
	}

	/**
	 * ロボット全体の順運動学の計算
	 * 
	 * @param ss
	 */
	public static void calculateForward(SomaticContext ss) {
		// Bodyから再帰的にPositionを計算
		RobotFrame rf = Nao.get(Frames.Body);
		FrameState fs = ss.get(Frames.Body);
		assert fs.getPosition().equals(rf.translate);

		// Bodyの座標をセット
		fs.getRotation().set(fs.getAxisAngle());
		fs.getRobotRotation().set(fs.getAxisAngle());
		fs.getRobotPosition().set(fs.getPosition());

		// 子フレームがあれば再帰的に計算する
		if (rf.child != null)
			for (RobotFrame child : rf.child)
				forwardKinematics(ss, child.id);
	}

	private static void forwardKinematics(SomaticContext ss, Frames id) {
		RobotFrame rf = Nao.get(id);
		FrameState fs = ss.get(id);

		// Body及び親フレームは計算されていることが前提
		assert id != Frames.Body && rf.parent != null;
		// 親フレームからみた回転軸は変化しない(角度は変わる)
		assert fs.getAxisAngle().x == Nao.get(id).axis.x;
		assert fs.getAxisAngle().y == Nao.get(id).axis.y;
		assert fs.getAxisAngle().z == Nao.get(id).axis.z;

		// 親フレームの値
		FrameState parent = ss.get(rf.parent.id);
		Matrix3f parentRotation = parent.getRobotRotation();
		Vector3f parentPosition = parent.getRobotPosition();

		// このフレームの値
		Matrix3f rotation = fs.getRotation();
		Matrix3f robotRotation = fs.getRobotRotation();

		// 回転行列をセット
		rotation.set(fs.getAxisAngle());
		// 親フレームからの回転行列をチェーンする
		robotRotation.mul(parentRotation, rotation);

		Vector3f position = fs.getPosition();
		Vector3f robotPosition = fs.getRobotPosition();
		// 旋回関節のみを想定しているので、親フレームからの位置ベクトルは変化しない
		assert position.equals(rf.translate);

		// 親フレームからの位置ベクトルをロボット座標系でのベクトルに直す
		// robotPosition = parentRotation*position
		parentRotation.transform(position, robotPosition);
		// 親フレームの位置ベクトルと繋げてこのフレームの位置ベクトルをつくる
		// robotPosition += parentPosition
		robotPosition.add(parentPosition);

		// 子フレームがあれば再帰的に計算する
		if (rf.child != null)
			for (RobotFrame child : rf.child)
				forwardKinematics(ss, child.id);
	}
}
