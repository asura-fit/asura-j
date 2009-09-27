/*
 * 作成日: 2008/04/24
 */
package jp.ac.fit.asura.nao.motion;

import jp.ac.fit.asura.nao.MotionFrameContext;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.event.MotionEventListener;

/**
 * @author $Author: sey $
 *
 * @version $Id: Motion.java 709 2008-11-23 07:40:31Z sey $
 *
 */
public abstract class Motion {
	protected MotionFrameContext context;
	private int id;
	protected String name;
	@Deprecated
	protected int totalFrames;
	@Deprecated
	protected int currentStep;
	protected MotionParam param;

	public Motion() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * モーションの開始時に呼び出されます.
	 *
	 * @param param
	 *            モーションパラメータ
	 * @throws IllegalArgumentException
	 *             引数のパラメータが使用できない場合に発生します.
	 */
	public void start(MotionParam param) throws IllegalArgumentException {
		this.param = param;
		currentStep = 0;
	}

	/**
	 * モーションの停止時に呼び出されます.
	 */
	public void stop() {
		currentStep = -1;
	}

	public void init(RobotContext context) {
	}

	/**
	 * 指定されたモーションパラメータを使用できるかを返します.
	 *
	 * @param param
	 * @return
	 */
	public boolean canAccept(MotionParam param) {
		return true;
	}

	/**
	 * モーションをすぐに停止できるかどうかを返します.
	 *
	 * @return
	 */
	public boolean canStop() {
		return currentStep == totalFrames || currentStep <= 0;
	}

	/**
	 * モーションの停止を要求します.
	 *
	 * モーションは動作を安全に停止し、canStop()がtrueを返す状態になることが求められます.
	 *
	 */
	public void requestStop() {
	}

	/**
	 * オドメータ(モーションによる移動量の推定)が使用できるかどうかを返します.
	 *
	 * @return
	 */
	public boolean hasOdometer() {
		return false;
	}

	public void updateOdometry(MotionEventListener listener) {
	}

	@Deprecated
	public void continueMotion() {
	}

	public int getTotalFrames() {
		return totalFrames;
	}

	public boolean hasNextStep() {
		return totalFrames > currentStep;
	}

	public abstract void step();

	public void setContext(MotionFrameContext context) {
		this.context = context;
	}

	public String toString() {
		if (getName() != null)
			return getName() + (param != null ? "@" + param : "");
		return getId() + (param != null ? "@" + param : "");
	}
}
