/*
 * 作成日: 2009/04/23
 */
package jp.ac.fit.asura.nao;

import jp.ac.fit.asura.nao.vision.VisualContext;

/**
 * @author sey
 *
 * @version $Id: $
 *
 */
public class VisualFrameContext extends FrameContext {
	private MotionFrameContext motionFrame;
	private VisualContext visualContext;
	private Image image;

	/**
	 * @param robotContext
	 */
	public VisualFrameContext(RobotContext robotContext) {
		super(robotContext);
	}

	/**
	 * @return image
	 */
	public Image getImage() {
		return image;
	}

	void setImage(Image image) {
		this.image = image;
	}

	/**
	 * @return visualContext
	 */
	public VisualContext getVisualContext() {
		return visualContext;
	}

	public void setVisualContext(VisualContext visualContext) {
		this.visualContext = visualContext;
	}

	/**
	 * @return motionFrame
	 */
	public MotionFrameContext getMotionFrame() {
		return motionFrame;
	}

	/**
	 * @param motionFrame
	 */
	void setMotionFrame(MotionFrameContext motionFrame) {
		this.motionFrame = motionFrame;
	}

	@Override
	public long getTime() {
		return image.getTimestamp();
	}
}
