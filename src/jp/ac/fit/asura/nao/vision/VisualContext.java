/*
 * 作成日: 2008/06/18
 */
package jp.ac.fit.asura.nao.vision;

import java.util.EnumMap;
import java.util.Map;

import jp.ac.fit.asura.nao.Camera;
import jp.ac.fit.asura.nao.Context;
import jp.ac.fit.asura.nao.Image;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.VisualFrameContext;
import jp.ac.fit.asura.nao.vision.perception.BallVision;
import jp.ac.fit.asura.nao.vision.perception.BlobVision;
import jp.ac.fit.asura.nao.vision.perception.GeneralVision;
import jp.ac.fit.asura.nao.vision.perception.GoalVision;
import jp.ac.fit.asura.nao.vision.perception.RobotVision;
import jp.ac.fit.asura.nao.vision.perception.VisualObject;

/**
 * @author sey
 *
 * @version $Id: VisualContext.java 704 2008-10-23 17:25:51Z sey $
 *
 */
public class VisualContext extends Context {
	private RobotContext robotContext;
	private VisualFrameContext frameContext;

	private EnumMap<VisualParam.Boolean, Boolean> boolParams;
	private EnumMap<VisualParam.Float, Float> floatParams;
	private EnumMap<VisualParam.Int, Integer> intParams;

	public Camera camera;

	public Image image;
	public byte[] gcdPlane;
	public byte[] houghPlane;

	public BlobVision blobVision;
	public BallVision ballVision;
	public GoalVision goalVision;
	public RobotVision robotVision;
	public GeneralVision generalVision;

	protected Map<VisualObjects, VisualObject> objects;

	public VisualContext(RobotContext robotContext) {
		this.robotContext = robotContext;
		boolParams = new EnumMap<VisualParam.Boolean, Boolean>(
				VisualParam.Boolean.class);
		floatParams = new EnumMap<VisualParam.Float, Float>(
				VisualParam.Float.class);
		intParams = new EnumMap<VisualParam.Int, Integer>(VisualParam.Int.class);
	}

	public RobotContext getSuperContext() {
		return robotContext;
	}

	/**
	 * @return frameContext
	 */
	public VisualFrameContext getFrameContext() {
		return frameContext;
	}

	/**
	 * @param frameContext
	 *            セットする frameContext
	 */
	void setFrameContext(VisualFrameContext frameContext) {
		this.frameContext = frameContext;
	}

	public VisualObject get(VisualObjects vo) {
		return objects.get(vo);
	}

	public boolean getParam(VisualParam.Boolean key) {
		if (boolParams.containsKey(key))
			return boolParams.get(key).booleanValue();
		return key.getDefault();
	}

	public float getParam(VisualParam.Float key) {
		if (floatParams.containsKey(key))
			return floatParams.get(key).floatValue();
		return key.getDefault();
	}

	public int getParam(VisualParam.Int key) {
		if (intParams.containsKey(key))
			return intParams.get(key).intValue();
		return key.getDefault();
	}

	public void setParam(VisualParam.Boolean key, boolean value) {
		boolParams.put(key, value);
	}

	public void setParam(VisualParam.Float key, float value) {
		floatParams.put(key, value);
	}

	public void setParam(VisualParam.Int key, int value) {
		intParams.put(key, value);
	}
}
