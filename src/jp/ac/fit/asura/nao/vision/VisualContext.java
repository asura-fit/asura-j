/*
 * 作成日: 2008/06/18
 */
package jp.ac.fit.asura.nao.vision;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.vision.perception.BallVision;
import jp.ac.fit.asura.nao.vision.perception.BlobVision;
import jp.ac.fit.asura.nao.vision.perception.GeneralVision;
import jp.ac.fit.asura.nao.vision.perception.GoalVision;

/**
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public class VisualContext {
	private RobotContext robotContext;

	public int[] plane;
	public int width;
	public int height;

	public byte[] gcdPlane;

	public BlobVision blobVision;
	public BallVision ballVision;
	public GoalVision goalVision;
	public GeneralVision generalVision;

	public VisualContext(RobotContext robotContext) {
		this.robotContext = robotContext;
	}

	public RobotContext getSuperContext() {
		return robotContext;
	}
}
