/*
 * 作成日: 2009/04/23
 */
package jp.ac.fit.asura.nao;

import jp.ac.fit.asura.nao.sensation.SomaticContext;

/**
 * @author sey
 *
 * @version $Id: $
 *
 */
public class MotionFrameContext extends FrameContext {
	private SensorContext sensorContext;
	private SomaticContext somaticContext;
	private boolean isInUse;
	private boolean isActive;

	/**
	 *
	 */
	public MotionFrameContext(RobotContext robotContext) {
		super(robotContext);
		sensorContext = robotContext.getSensor().create();
	}

	/**
	 * @return sensorContext
	 */
	public SensorContext getSensorContext() {
		return sensorContext;
	}

	/**
	 * @return somaticContext
	 */
	public SomaticContext getSomaticContext() {
		return somaticContext;
	}

	public void setSomaticContext(SomaticContext somaticContext) {
		this.somaticContext = somaticContext;
	}

	@Override
	public long getTime() {
		return sensorContext.getTime();
	}

	boolean isActive() {
		return isActive;
	}

	void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	boolean isInUse() {
		return isInUse;
	}

	void setInUse(boolean isActive) {
		this.isInUse = isActive;
	}

}
