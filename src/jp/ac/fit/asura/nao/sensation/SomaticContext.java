/*
 * 作成日: 2008/10/03
 */
package jp.ac.fit.asura.nao.sensation;

import java.util.Collection;
import java.util.EnumMap;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.Context;
import jp.ac.fit.asura.nao.physical.Robot;
import jp.ac.fit.asura.nao.physical.Robot.Frames;

/**
 * ロボットの姿勢情報を保持します.
 *
 * これには順運動学計算の結果、ワールド座標系での角度、接地状態などが含まれます.
 *
 * @author $Author: sey $
 *
 * @version $Id: SomaticContext.java 717 2008-12-31 18:16:20Z sey $
 *
 */
public class SomaticContext extends Context {
	private EnumMap<Frames, FrameState> frames;
	private Robot robot;
	private Vector3f com;

	private Matrix3f bodyPosture;
	private float bodyHeight;

	private boolean leftOnGround;
	private boolean rightOnGround;

	private int confidence;

	public SomaticContext(Robot robot) {
		this.robot = robot;
		com = new Vector3f();
		bodyPosture = new Matrix3f();
		frames = new EnumMap<Frames, FrameState>(Frames.class);
		for (Frames frame : robot.getFrames())
			frames.put(frame, new FrameState(robot.get(frame)));
	}

	public SomaticContext(SomaticContext state) {
		frames = new EnumMap<Frames, FrameState>(Frames.class);
		for (Frames frame : state.frames.keySet())
			frames.put(frame, state.frames.get(frame).clone());
		com = new Vector3f(state.com);
		robot = state.robot;
	}

	public FrameState get(Frames frame) {
		return frames.get(frame);
	}

	public Collection<FrameState> getFrames() {
		return frames.values();
	}

	public Vector3f getCenterOfMass() {
		return com;
	}

	public Robot getRobot() {
		return robot;
	}

	/**
	 * ロボットのボディの地面からの姿勢(Roll,Pitchのみ)を返します. これは{@link Matrix3f}
	 * であらわされる回転行列を返しますが、適切なオブジェクト以外は内容を変更するべきではありません.
	 *
	 * @return
	 */
	public Matrix3f getBodyPosture() {
		return bodyPosture;
	}

	/**
	 * ロボットのボディの地面からの高さを返します.
	 *
	 * @return bodyHeight
	 */
	public float getBodyHeight() {
		return bodyHeight;
	}

	/**
	 * この姿勢情報がどれくらい信頼できるのかを返します.
	 *
	 * @return the confidence
	 */
	public int getConfidence() {
		return confidence;
	}

	/**
	 * @return the leftOnGround
	 */
	public boolean isLeftOnGround() {
		return leftOnGround;
	}

	/**
	 * @return the rightOnGround
	 */
	public boolean isRightOnGround() {
		return rightOnGround;
	}

	void setConfidence(int confidence) {
		this.confidence = confidence;
	}

	void setLeftOnGround(boolean leftOnGround) {
		this.leftOnGround = leftOnGround;
	}

	void setRightOnGround(boolean rightOnGround) {
		this.rightOnGround = rightOnGround;
	}

	public void setBodyHeight(float bodyHeight) {
		this.bodyHeight = bodyHeight;
	}
}
