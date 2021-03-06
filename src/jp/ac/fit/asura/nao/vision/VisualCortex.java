/*
 * 作成日: 2008/04/24
 */
package jp.ac.fit.asura.nao.vision;

import static jp.ac.fit.asura.nao.vision.VisualObjects.Ball;
import static jp.ac.fit.asura.nao.vision.VisualObjects.BlueGoal;
import static jp.ac.fit.asura.nao.vision.VisualObjects.YellowGoal;
import static jp.ac.fit.asura.nao.vision.VisualObjects.RedNao;
import static jp.ac.fit.asura.nao.vision.VisualObjects.BlueNao;
import static jp.ac.fit.asura.nao.vision.VisualParam.Boolean.USE_HOUGH;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import jp.ac.fit.asura.nao.Camera;
import jp.ac.fit.asura.nao.Image;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.VisualCycle;
import jp.ac.fit.asura.nao.VisualFrameContext;
import jp.ac.fit.asura.nao.event.VisualEventListener;
import jp.ac.fit.asura.nao.vision.perception.BallVision;
import jp.ac.fit.asura.nao.vision.perception.BallVisualObject;
import jp.ac.fit.asura.nao.vision.perception.BlobVision;
import jp.ac.fit.asura.nao.vision.perception.GeneralVision;
import jp.ac.fit.asura.nao.vision.perception.GoalVision;
import jp.ac.fit.asura.nao.vision.perception.GoalVisualObject;
import jp.ac.fit.asura.nao.vision.perception.HoughVision;
import jp.ac.fit.asura.nao.vision.perception.RobotVision;
import jp.ac.fit.asura.nao.vision.perception.RobotVisualObject;
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
public class VisualCortex implements VisualCycle {
	private GCD gcd;

	private Map<VisualObjects, VisualObject> map;

	private BlobVision blobVision;
	private BallVision ballVision;
	private GoalVision goalVision;
	private RobotVision robotVision;
	private GeneralVision generalVision;
	private HoughVision houghVision;

	private VisualContext context;

	private List<VisualEventListener> listeners;

	/**
	 *
	 */
	public VisualCortex() {
		listeners = new CopyOnWriteArrayList<VisualEventListener>();
		map = new EnumMap<VisualObjects, VisualObject>(VisualObjects.class);
		map.put(Ball, new BallVisualObject());
		map.put(YellowGoal, new GoalVisualObject(YellowGoal));
		map.put(BlueGoal, new GoalVisualObject(BlueGoal));
		map.put(RedNao, new RobotVisualObject(RedNao));
		map.put(BlueNao, new RobotVisualObject(BlueNao));

		blobVision = new BlobVision();
		ballVision = new BallVision();
		goalVision = new GoalVision();
		robotVision = new RobotVision();
		generalVision = new GeneralVision();
		houghVision = new HoughVision();
	}

	@Override
	public void init(RobotContext rctx) {
		Camera camera = rctx.getCamera();
		context = new VisualContext(rctx);
		context.ballVision = ballVision;
		context.blobVision = blobVision;
		context.generalVision = generalVision;
		context.goalVision = goalVision;
		context.robotVision = robotVision;
		context.camera = camera;
		context.objects = map;
	}

	@Override
	public void start() {
	}

	@Override
	public void step(VisualFrameContext frameContext) {
		clear();
		frameContext.setVisualContext(context);
		context.setFrameContext(frameContext);
		Image image = frameContext.getImage();
		context.image = image;

		int length = image.getWidth() * image.getHeight();
		if (context.gcdPlane == null || context.gcdPlane.length != length) {
			context.gcdPlane = new byte[length];
		}

		if (gcd != null)
			gcd.detect(context.image, context.gcdPlane);

		updateContext(frameContext);
		blobVision.formBlobs();
		// striateVision.process();
		ballVision.findBall();
		goalVision.findBlueGoal();
		goalVision.findYellowGoal();
		robotVision.findRedNao();
		robotVision.findBlueNao();
		if (context.getParam(USE_HOUGH))
			houghVision.process();
		fireUpdateVision();
	}

	@Override
	public void stop() {
	}

	private void updateContext(VisualFrameContext context) {
		blobVision.setVisualFrameContext(context);
		ballVision.setVisualFrameContext(context);
		goalVision.setVisualFrameContext(context);
		robotVision.setVisualFrameContext(context);
		generalVision.setVisualFrameContext(context);
		houghVision.setVisualFrameContext(context);
	}

	public void clear() {
		for (VisualObject vo : map.values()) {
			vo.clear();
		}
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

	public void setGCD(GCD gcd) {
		this.gcd = gcd;
	}

	public void setParam(VisualParam.Boolean key, boolean value) {
		context.setParam(key, value);
	}

	public void setParam(VisualParam.Float key, float value) {
		context.setParam(key, value);
	}

	public void setParam(VisualParam.Int key, int value) {
		context.setParam(key, value);
	}
}
