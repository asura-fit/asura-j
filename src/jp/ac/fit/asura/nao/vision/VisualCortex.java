/*
 * 作成日: 2008/04/24
 */
package jp.ac.fit.asura.nao.vision;

import static jp.ac.fit.asura.nao.vision.GCD.cCYAN;
import static jp.ac.fit.asura.nao.vision.GCD.cORANGE;
import static jp.ac.fit.asura.nao.vision.GCD.cYELLOW;
import static jp.ac.fit.asura.nao.vision.VisualObjects.Ball;
import static jp.ac.fit.asura.nao.vision.VisualObjects.BlueGoal;
import static jp.ac.fit.asura.nao.vision.VisualObjects.YellowGoal;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import jp.ac.fit.asura.nao.Image;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.vision.objects.BallVisualObject;
import jp.ac.fit.asura.nao.vision.objects.GoalVisualObject;
import jp.ac.fit.asura.nao.vision.objects.VisualObject;
import jp.ac.fit.asura.nao.vision.perception.BallVision;
import jp.ac.fit.asura.nao.vision.perception.BlobVision;
import jp.ac.fit.asura.nao.vision.perception.GeneralVision;
import jp.ac.fit.asura.nao.vision.perception.GoalVision;
import jp.ac.fit.asura.nao.vision.perception.BlobVision.Blob;

/**
 * 画像認識の中枢.
 * 
 * 値はすべて，radian/mm/左上原点の系で扱います.
 * 
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class VisualCortex implements RobotLifecycle {
	private GCD gcd;

	private Map<VisualObjects, VisualObject> map;

	private Sensor sensor;

	private BlobVision blobVision;
	private BallVision ballVision;
	private GoalVision goalVision;
	private GeneralVision generalVision;

	private VisualContext context;

	/**
	 * 
	 */
	public VisualCortex() {
		gcd = new GCD();
		map = new EnumMap<VisualObjects, VisualObject>(VisualObjects.class);
		map.put(Ball, new BallVisualObject());
		map.put(YellowGoal, new GoalVisualObject(YellowGoal));
		map.put(BlueGoal, new GoalVisualObject(BlueGoal));

		blobVision = new BlobVision();
		ballVision = new BallVision();
		goalVision = new GoalVision();
		generalVision = new GeneralVision();
	}

	public void init(RobotContext rctx) {
		sensor = rctx.getSensor();
		context = new VisualContext(rctx);
		context.ballVision = ballVision;
		context.blobVision = blobVision;
		context.generalVision = generalVision;
		context.goalVision = goalVision;
		context.camera = new CameraInfo();
		context.objects = map;
	}

	public void start() {
		gcd.loadTMap("normal.tm2");
	}

	public void step() {
		clear();
		updateImage(sensor.getImage());
	}

	public void stop() {
	}

	public void updateImage(Image image) {
		context.plane = image.getData();
		context.camera.width = image.getWidth();
		context.camera.height = image.getHeight();
		context.camera.horizontalFieldOfView = image.getHorizontalFieldOfView();
		context.camera.verticalFieldOfView = image.getVerticalFieldOfView();

		if (context.gcdPlane == null
				|| context.gcdPlane.length != context.plane.length) {
			context.gcdPlane = new byte[context.plane.length];
		}

		gcd.detect(context.plane, context.gcdPlane);

		updateContext(context);
		blobVision.formBlobs();

		ballVision.findBall();
		goalVision.findBlueGoal();
		goalVision.findYellowGoal();
	}

	private void updateContext(VisualContext context) {
		blobVision.setContext(context);
		ballVision.setContext(context);
		goalVision.setContext(context);
		generalVision.setContext(context);
		for (VisualObject vo : map.values())
			vo.setContext(context);
	}

	public void clear() {
		for (VisualObject vo : map.values()) {
			vo.clear();
		}
	}

	public VisualContext getVisualContext() {
		return context;
	}

	public VisualObject get(VisualObjects key) {
		return map.get(key);
	}

	public GCD getGCD() {
		return gcd;
	}
}
