/*
 * 作成日: 2008/04/24
 */
package jp.ac.fit.asura.nao.vision;

import static jp.ac.fit.asura.nao.vision.VisualObjects.Ball;
import static jp.ac.fit.asura.nao.vision.VisualObjects.BlueGoal;
import static jp.ac.fit.asura.nao.vision.VisualObjects.YellowGoal;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import jp.ac.fit.asura.nao.Camera;
import jp.ac.fit.asura.nao.Image;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.Image.BufferType;
import jp.ac.fit.asura.nao.event.VisualEventListener;
import jp.ac.fit.asura.nao.vision.perception.BallVision;
import jp.ac.fit.asura.nao.vision.perception.BallVisualObject;
import jp.ac.fit.asura.nao.vision.perception.BlobVision;
import jp.ac.fit.asura.nao.vision.perception.GeneralVision;
import jp.ac.fit.asura.nao.vision.perception.GoalVision;
import jp.ac.fit.asura.nao.vision.perception.GoalVisualObject;
import jp.ac.fit.asura.nao.vision.perception.VisualObject;

/**
 * 画像認識の中枢.
 *
 * 値はすべて，radian/mm/左上原点(画像平面座標系(plane))の系で扱います.
 *
 * たまにイメージ座標系(中央が原点)のものもあります.
 *
 * @author sey
 *
 * @version $Id: VisualCortex.java 704 2008-10-23 17:25:51Z sey $
 *
 */
public class VisualCortex implements RobotLifecycle {
	private GCD gcd;

	private Map<VisualObjects, VisualObject> map;

	private Sensor sensor;
	private Camera camera;
	private Image image;

	private BlobVision blobVision;
	private BallVision ballVision;
	private GoalVision goalVision;
	private GeneralVision generalVision;

	private VisualContext context;

	private List<VisualEventListener> listeners;

	/**
	 *
	 */
	public VisualCortex() {
		listeners = new ArrayList<VisualEventListener>();
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
		camera = rctx.getCamera();
		image = camera.createImage();
		context = new VisualContext(rctx);
		context.ballVision = ballVision;
		context.blobVision = blobVision;
		context.generalVision = generalVision;
		context.goalVision = goalVision;
		context.camera = camera;
		context.objects = map;
	}

	public void start() {
		gcd.loadTMap("normal.tm2");
	}

	public void step() {
		clear();
		camera.updateImage(image);
		updateImage(image);
		fireUpdateVision();
	}

	public void stop() {
	}

	public void updateImage(Image image) {
		context.image = image;

		int length = image.getWidth() * image.getWidth();
		if (context.gcdPlane == null || context.gcdPlane.length != length) {
			context.gcdPlane = new byte[length];
		}

		gcd.detect(context.image, context.gcdPlane);

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
	}

	public void clear() {
		for (VisualObject vo : map.values()) {
			vo.clear();
		}
	}

	public VisualContext getVisualContext() {
		return context;
	}

	public GCD getGCD() {
		return gcd;
	}

	public void addEventListener(VisualEventListener listener) {
		listeners.add(listener);
	}

	public void removeEventListener(VisualEventListener listener) {
		listeners.remove(listener);
	}

	private void fireUpdateVision() {
		for (VisualEventListener listener : listeners)
			listener.updateVision(context);
	}
}
