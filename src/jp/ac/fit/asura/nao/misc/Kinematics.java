/*
 * 作成日: 2008/10/03
 */
package jp.ac.fit.asura.nao.misc;

import javax.vecmath.GMatrix;
import javax.vecmath.GVector;
import javax.vecmath.Matrix3f;
import javax.vecmath.SingularMatrixException;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.motion.MotionUtils;
import jp.ac.fit.asura.nao.physical.RobotFrame;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.FrameState;
import jp.ac.fit.asura.nao.sensation.SomaticContext;

import org.apache.log4j.Logger;

/**
 * 運動学/逆運動学計算.
 * 
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class Kinematics {
	public static double SCALE = 1.0;
	public static double LANGLE = 0.75;

	private static Logger log = Logger.getLogger(Kinematics.class);

	public static GMatrix calculateJacobian(Frames[] route,
			SomaticContext context) {
		log.trace("calculate jacobian");

		assert route != null;

		GMatrix mat = new GMatrix(6, route.length);

		// JointStateから取得する
		FrameState endFrame = context.get(route[route.length - 1]);
		Vector3f end = endFrame.getBodyPosition();

		// Bodyから基準座標系への位置ベクトルを，すべての位置ベクトルに足す
		// ことで，基準座標系からの絶対位置を表現する

		for (int i = 0; i < route.length; i++) {
			FrameState fs = context.get(route[i]);

			// このフレームの座標
			Vector3f pos = fs.getBodyPosition();
			Vector3f deltaPos = new Vector3f(end);
			deltaPos.sub(pos);
			// dPos = end - position(i)

			Vector3f zi = new Vector3f();
			MatrixUtils.setAxis(fs.getAxisAngle(), zi);
			fs.getBodyRotation().transform(zi);

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
	 * 逆運動学の計算.
	 * 
	 * @param ss
	 * @param id
	 * @param position
	 */
	public static int calculateInverse(SomaticContext context, FrameState target)
			throws SingularPostureException {
		log.trace("calculate inverse kinematics");

		final double EPS = 1e-2; // 1e-3ぐらいまでが精度の限界か.
		// とりあず右足だけに限定. そのうち全身に拡張する.
		Frames f = target.getId();

		Frames[] route = context.getRobot().findJointRoute(Frames.Body, f);

		GVector err = new GVector(6);
		GVector dq = new GVector(route.length);

		// 繰り返し回数にけっこうブレが大きい模様.
		// 数億分の一ぐらいの確率で1000回を超えることもある
		for (int i = 0; i < 1000; i++) {
			// 計算が進んでも目標に達しないならランダムにリセットする
			if (i != 0 && i % 32 == 0)
				setAngleRandom(context);

			// 順運動学で現在の姿勢を計算
			calculateForward(context);

			// 目標値との差をとる
			calcError(target, context.get(target.getId()), err);

			if (err.normSquared() < EPS) {
				// 間接値は可動域内か?
				boolean inRange = true;
				for (int j = 0; j < route.length; j++) {
					FrameState fs = context.get(route[j]);
					if (!MotionUtils.isInRange(fs.getFrame(), fs.getAngle())) {
						inRange = false;
						// 稼働域内でクリッピング
						fs.getAxisAngle().angle = MotionUtils.clipping(fs
								.getFrame(), fs.getAngle());
					}
				}

				// 稼働域内であれば終了
				if (inRange)
					return i;

				// 間接値が稼働域外であれば、丸めた結果を元に再計算する
				calculateForward(context);
				calcError(target, context.get(target.getId()), err);
				if (err.normSquared() < EPS) {
					// 丸めた結果がそのまま使えるなら終了
					log.debug("rounded");
					return i;
				} else if (err.normSquared() > 10) {
					// 目標値と全然違うなら最初からやり直す
					setAngleRandom(context);
					// 見つかるまで無限ループする?
					// i = 0;
					continue;
				}
				// 丸めた結果が目標値に近いならそのまま続行する
			}

			GMatrix jacobi = calculateJacobian(route, context);
			assert jacobi != null && jacobi.getNumRow() == 6
					&& jacobi.getNumCol() == route.length : jacobi;

			// 未完成.
			// err[6x1] = jacobi[6xN] * dq[Nx1]
			// この連立方程式をといてdqを求めなければならない. N=6であれば
			// dq[Nx1] = (jacobi^-1)[Nx6] * err[6x1]
			// でとけるが... 一般にN>6となるので、何らかの制約条件が必要.
			// 特異値分解による擬似逆行列やSR-Inverseなど.
			// とりあえずN=6に限定
			// LUD+BackSolveで解いてるが、実はN=6ではinvert()のほうが速い?
			try {
				MatrixUtils.solve(jacobi, err, dq);
			} catch (SingularMatrixException e) {
				log.error(e);
				assert false;
				setAngleRandom(context);
				continue;
			}

			// dqを処理して間接角度に適用する
			dq.scale(SCALE);

			double max = Math.abs(dq.getElement(0));
			for (int j = 1; j < dq.getSize(); j++) {
				if (Math.abs(dq.getElement(j)) > max)
					max = Math.abs(dq.getElement(j));
			}

			// 最大変位をLANGLEで正規化
			if (max > LANGLE)
				dq.scale(LANGLE / max);

			for (int j = 0; j < route.length; j++) {
				assert !Double.isNaN(dq.getElement(j)) : dq;
				assert Math.abs(dq.getElement(j)) <= LANGLE + MathUtils.EPSd : dq;
				FrameState fs = context.get(route[j]);
				fs.getAxisAngle().angle += dq.getElement(j);
				fs.getAxisAngle().angle = MathUtils.normalizeAnglePI(fs
						.getAxisAngle().angle);
			}
		}
		// 特異姿勢にはいった可能性がある.
		log.error("error");
		log.error("error " + err.normSquared() + " vectors:" + err);
		GMatrix jacobi = calculateJacobian(route, context);
		log.error(jacobi);
		for (FrameState fs : context.getFrames()) {
			log.error(fs.getId());
			log.error(MathUtils.toDegrees(fs.getAngle()));
		}
		log.error(context.get(Frames.RHipYawPitch).getAngle());
		log.error(context.get(Frames.RHipPitch).getAngle());
		log.error(context.get(Frames.RHipRoll).getAngle());
		log.error(context.get(Frames.RKneePitch).getAngle());
		log.error(context.get(Frames.RAnklePitch).getAngle());
		log.error(context.get(Frames.RAnkleRoll).getAngle());
		assert false;
		throw new SingularPostureException();
	}

	private static void setAngleRandom(SomaticContext sc) {
		for (FrameState fs : sc.getFrames()) {
			if (fs.getId().isJoint()) {
				float max = fs.getFrame().getMaxAngle();
				float min = fs.getFrame().getMinAngle();
				fs.getAxisAngle().angle = (float) MathUtils.rand(min, max);
			}
		}
	}

	/**
	 * ロボット座標系での二つの関節の位置と姿勢の差を返します.
	 * 
	 * @param expected
	 * @param actual
	 * @param err
	 */
	protected static void calcError(FrameState expected, FrameState actual,
			GVector err) {
		log.trace("calculate error");
		assert err.getSize() == 6;

		Vector3f p1 = expected.getBodyPosition();
		Vector3f p2 = actual.getBodyPosition();

		// 目標(expected)との位置の差をとる.
		err.setElement(0, p1.x - p2.x);
		err.setElement(1, p1.y - p2.y);
		err.setElement(2, p1.z - p2.z);

		Matrix3f r1 = expected.getBodyRotation();
		Matrix3f r2 = actual.getBodyRotation();
		assert MathUtils.epsEquals(r1.determinant(), 1);
		assert MathUtils.epsEquals(r2.determinant(), 1);
		// r2^-1は回転行列の逆(すなわち、-θ)を表す.
		// r2^-1 * r1とすることで、回転角度的にはθ = r2 - r1を求めている.
		// よって、rotErrはr1とr2の角度の差を表している.
		Matrix3f rotErr = new Matrix3f();

		// 本当は逆行列を計算するべきだが、直交行列なので転置＝逆行列になるはず.
		rotErr.transpose(r2);
		// rotErr.invert(r2);

		// assert MathUtils.epsEquals(rotErr.determinant(), 1);

		rotErr.mul(r1);
		// normalizeで直交性を回復...したいが、重い.
		// rotErr.normalize();

		// assert MathUtils.epsEquals(rotErr.determinant(), 1);

		// 回転角度の差を角速度ベクトルomegaに変換する.
		// omegaは1秒間でrotErr分の回転角度を実現するための角速度である.
		// すなわち、回転行列を角度とみなすと、形式的にはomega = rotErr [rad/s] となる.
		Vector3f omega = new Vector3f();
		rot2omega(rotErr, omega);

		// ω' = Rωとして、r2にあわせて角速度ベクトルomegaを回転する.
		r2.transform(omega);

		// 角速度ベクトルをセット
		err.setElement(3, omega.x);
		err.setElement(4, omega.y);
		err.setElement(5, omega.z);
	}

	/**
	 * 回転行列から角速度ベクトルへの変換. ヒューマノイドロボット p35より.
	 * 
	 * @param rotation
	 *            変換する回転行列
	 * @param omega
	 *            角速度ベクトルの書き込み先
	 */
	private static void rot2omega(Matrix3f rot, Vector3f omega) {
		double theta = Math.acos(MathUtils.clipAbs(
				(rot.m00 + rot.m11 + rot.m22 - 1) / 2.0, 1));
		if (MathUtils.epsEquals(Math.sin(theta), 0)
				|| MatrixUtils.isIdentity(rot)) {
			// System.out.println(Math.sin(theta));
			omega.set(0, 0, 0);
			return;
		}
		assert !Double.isNaN(theta) && Math.sin(theta) != 0;

		omega.setX(rot.m21 - rot.m12);
		omega.setY(rot.m02 - rot.m20);
		omega.setZ(rot.m10 - rot.m01);
		omega.scale((float) (theta / (2 * Math.sin(theta))));
	}

	/**
	 * ロボット全体の順運動学の計算.
	 * 
	 * @param ss
	 */
	public static void calculateForward(SomaticContext ss) {
		log.trace("calculate forward kinematics");
		// Bodyから再帰的にPositionを計算
		RobotFrame rf = ss.getRobot().get(Frames.Body);
		FrameState fs = ss.get(Frames.Body);
		assert fs.getPosition().equals(rf.getTranslation());

		// Bodyの座標をセット
		fs.getRotation().set(fs.getAxisAngle());
		fs.getBodyRotation().set(fs.getAxisAngle());
		fs.getBodyPosition().set(fs.getPosition());

		// 子フレームがあれば再帰的に計算する
		if (rf.getChildren() != null)
			for (RobotFrame child : rf.getChildren())
				forwardKinematics(ss, child.getId());
	}

	private static void forwardKinematics(SomaticContext ss, Frames id) {
		log.trace("calculate fk recursively");
		RobotFrame rf = ss.getRobot().get(id);
		FrameState fs = ss.get(id);

		// Body及び親フレームは計算されていることが前提
		assert id != Frames.Body && rf.getParent() != null;

		// 親フレームの値
		FrameState parent = ss.get(rf.getParent().getId());
		Matrix3f parentRotation = parent.getBodyRotation();
		Vector3f parentPosition = parent.getBodyPosition();

		// このフレームの値
		Matrix3f rotation = fs.getRotation();
		Matrix3f robotRotation = fs.getBodyRotation();

		// 回転行列をセット
		rotation.set(fs.getAxisAngle());
		// 親フレームからの回転行列をチェーンする
		robotRotation.mul(parentRotation, rotation);
		// robotRotation.normalize();
		assert MathUtils.epsEquals(rotation.determinant(), 1) : rotation
				.determinant();

		Vector3f position = fs.getPosition();
		Vector3f robotPosition = fs.getBodyPosition();
		// 旋回関節のみを想定しているので、親フレームからの位置ベクトルは変化しない
		assert position.equals(rf.getTranslation());

		// 親フレームからの位置ベクトルをロボット座標系でのベクトルに直す
		// robotPosition = parentRotation*position
		parentRotation.transform(position, robotPosition);
		// 親フレームの位置ベクトルと繋げてこのフレームの位置ベクトルをつくる
		// robotPosition += parentPosition
		robotPosition.add(parentPosition);

		// 子フレームがあれば再帰的に計算する
		if (rf.getChildren() != null)
			for (RobotFrame child : rf.getChildren())
				forwardKinematics(ss, child.getId());
	}

	public static void calculateCenterOfMass(SomaticContext ss) {
		log.trace("calculate center of mass");
		// Bodyから再帰的にPositionを計算
		RobotFrame rf = ss.getRobot().get(Frames.Body);
		FrameState fs = ss.get(Frames.Body);
		assert fs.getPosition().equals(rf.getTranslation());

		float mass = rf.getGrossMass();
		Vector3f com = new Vector3f();

		if (rf.getChildren() != null)
			for (RobotFrame child : rf.getChildren())
				calcCenterOfMassRecursively(ss, child.getId(), com);

		com.scale(1 / mass);
		ss.getCenterOfMass().set(com);
	}

	private static void calcCenterOfMassRecursively(SomaticContext ss,
			Frames id, Vector3f com) {
		log.trace("calculate CoM recursively");
		RobotFrame rf = ss.getRobot().get(id);
		FrameState fs = ss.get(id);

		// TODO 重心位置は自リンク相対でいいのか? リンクの中心をとる必要がある?
		com.scaleAdd(rf.getMass(), fs.getBodyPosition(), com);

		// 子フレームがあれば再帰的に計算する
		if (rf.getChildren() != null)
			for (RobotFrame child : rf.getChildren())
				calcCenterOfMassRecursively(ss, child.getId(), com);
	}
}
