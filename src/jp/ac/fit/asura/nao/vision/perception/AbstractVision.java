/*
 * 作成日: 2009/04/23
 */
package jp.ac.fit.asura.nao.vision.perception;

import jp.ac.fit.asura.nao.MotionFrameContext;
import jp.ac.fit.asura.nao.VisualFrameContext;
import jp.ac.fit.asura.nao.vision.VisualContext;

/**
 * @author sey
 *
 * @version $Id: $
 *
 */
public abstract class AbstractVision {
	private VisualContext context;
	private VisualFrameContext visualFrame;
	private MotionFrameContext motionFrame;

	/**
	 * @param frameContext
	 *            セットする frameContext
	 */
	public void setVisualFrameContext(VisualFrameContext frameContext) {
		this.visualFrame = frameContext;
		this.context = frameContext.getVisualContext();
		this.motionFrame = frameContext.getMotionFrame();
	}

	public VisualContext getContext() {
		return context;
	}

	public VisualFrameContext getVisualFrame() {
		return visualFrame;
	}

	public MotionFrameContext getMotionFrame() {
		return motionFrame;
	}
}
