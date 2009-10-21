/*
 * 作成日: 2008/06/18
 */
package jp.ac.fit.asura.nao.vision;

import java.util.Map;

import jp.ac.fit.asura.nao.Camera;
import jp.ac.fit.asura.nao.FrameContext;
import jp.ac.fit.asura.nao.Image;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.vision.perception.BallVision;
import jp.ac.fit.asura.nao.vision.perception.BlobVision;
import jp.ac.fit.asura.nao.vision.perception.GeneralVision;
import jp.ac.fit.asura.nao.vision.perception.GoalVision;
import jp.ac.fit.asura.nao.vision.perception.VisualObject;

/**
 * @author sey
 *
 * @version $Id: VisualContext.java 704 2008-10-23 17:25:51Z sey $
 *
 */
public class VisualContext {
	private RobotContext robotContext;
	private FrameContext frameContext;

	public Camera camera;

	public Image image;
	public byte[] gcdPlane;
	public byte[] houghPlane;

	public BlobVision blobVision;
	public BallVision ballVision;
	public GoalVision goalVision;
	public GeneralVision generalVision;

	protected Map<VisualObjects, VisualObject> objects;

	public VisualContext(RobotContext robotContext) {
		this.robotContext = robotContext;
	}

	public RobotContext getSuperContext() {
		return robotContext;
	}

	/**
	 * @return frameContext
	 */
	public FrameContext getFrameContext() {
		return frameContext;
	}

	/**
	 * @param frameContext
	 *            セットする frameContext
	 */
	void setFrameContext(FrameContext frameContext) {
		this.frameContext = frameContext;
	}

	public VisualObject get(VisualObjects vo) {
		return objects.get(vo);
	}
}
