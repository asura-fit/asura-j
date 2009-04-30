/*
 * 作成日: 2008/04/24
 */
package jp.ac.fit.asura.nao.vision;

import static jp.ac.fit.asura.nao.vision.VisualObjects.Ball;
import static jp.ac.fit.asura.nao.vision.VisualObjects.BlueGoal;
import static jp.ac.fit.asura.nao.vision.VisualObjects.YellowGoal;

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
	private GeneralVision generalVision;

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

		blobVision = new BlobVision();
		ballVision = new BallVision();
		goalVision = new GoalVision();
		generalVision = new GeneralVision();
	}

	@Override
	public void init(RobotContext rctx) {
		Camera camera = rctx.getCamera();
		context = new VisualContext(rctx);
		context.ballVision = ballVision;
		context.blobVision = blobVision;
		context.generalVision = generalVision;
		context.goalVision = goalVision;
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

		ballVision.findBall();
		goalVision.findBlueGoal();
		goalVision.findYellowGoal();
		fireUpdateVision();
	}

	@Override
	public void stop() {
	}

	private void updateContext(VisualFrameContext context) {
		blobVision.setVisualFrameContext(context);
		ballVision.setVisualFrameContext(context);
		goalVision.setVisualFrameContext(context);
		generalVision.setVisualFrameContext(context);
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
}
