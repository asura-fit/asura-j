/*
 * 作成日: 2008/06/18
 */
package jp.ac.fit.asura.nao.vision;

import java.util.Map;

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

	public CameraInfo camera;

	public int[] plane;
	public byte[] gcdPlane;

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

	public VisualObject get(VisualObjects vo) {
		return objects.get(vo);
	}
}
