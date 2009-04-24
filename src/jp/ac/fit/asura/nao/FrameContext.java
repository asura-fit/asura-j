/*
 * 作成日: 2009/04/22
 */
package jp.ac.fit.asura.nao;

/**
 * @author sey
 *
 * @version $Id: $
 *
 */
public abstract class FrameContext extends Context {
	private RobotContext robotContext;

	private int frame;

	/**
	 *
	 */
	public FrameContext(RobotContext robotContext) {
		this.robotContext = robotContext;
		frame = 0;
	}

	/**
	 * @return robotContext
	 */
	public RobotContext getRobotContext() {
		return robotContext;
	}

	/**
	 * @return frame
	 */
	public int getFrame() {
		return frame;
	}

	/**
	 * @param frame
	 *            セットする frame
	 */
	protected void setFrame(int frame) {
		this.frame = frame;
	}

	/**
	 * @return time
	 */
	public abstract long getTime();
}
