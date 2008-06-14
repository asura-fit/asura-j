/*
 * 作成日: 2008/04/24
 */
package jp.ac.fit.asura.nao.vision;

import java.awt.geom.Point2D;
import java.util.EnumMap;
import java.util.List;

import jp.ac.fit.asura.nao.Image;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.vision.BlobUtils.Blob;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class VisualCortex implements RobotLifecycle {
	private GCD gcd;

	private int[] plane;
	private int width;
	private int height;

	private EnumMap<VisualObjects, VisualObject> map;

	private byte[] gcdPlane;

	private Sensor sensor;

	private BlobUtils blobUtils;

	/**
	 * 
	 */
	public VisualCortex() {
		gcd = new GCD();
		map = new EnumMap<VisualObjects, VisualObject>(VisualObjects.class);

		map.put(VisualObjects.Ball, new VisualObject());
		map.put(VisualObjects.YellowGoal, new VisualObject());
		map.put(VisualObjects.BlueGoal, new VisualObject());

		blobUtils = new BlobUtils();

	}

	public void init(RobotContext rctx) {
		sensor = rctx.getSensor();
		gcdPlane = null;
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
		this.plane = image.getData();
		this.width = image.getWidth();
		this.height = image.getHeight();

		if (gcdPlane == null || gcdPlane.length != plane.length) {
			gcdPlane = new byte[plane.length];
		}

		gcd.detect(plane, gcdPlane);

		blobUtils.formBlobs(gcdPlane, width, height);

		/* 青と黄色の blob を探す => こっちはゴールがあるので，pink がなくても見えるはず */
		findBall();
		findGoal();
	}

	public void clear() {
		for (VisualObject vo : map.values()) {
			vo.cf = 0;
		}
	}

	public byte[] getGcdPlane() {
		return gcdPlane;
	}

	public Image getImage() {
		return new Image(plane, width, height);
	}

	private void findBall() {
		List<Blob> blobs = blobUtils.findBlobs(GCD.cORANGE, 10, 10);

		if (!blobs.isEmpty()) {
			VisualObject ball = map.get(VisualObjects.Ball);
			Blob blob = blobs.get(0);

			Point2D.Double cp = new Point2D.Double();
			cp.x = (blob.xmin + blob.xmax) / 2 - width / 2;
			cp.y = (blob.ymin + blob.ymax) / 2 - width / 2;
			ball.center = cp;
			ball.cf = blob.mass;
			ball.angle = calculateAngle(cp);

			// System.out.println("" + blob);

			ball.dist = (blob.ymax - blob.ymin) * 15;
		}
	}

	private void findGoal() {
		List<Blob> cyanBlobs = blobUtils.findBlobs(GCD.cCYAN, 10, 20);
		List<Blob> yellowBlobs = blobUtils.findBlobs(GCD.cYELLOW, 10, 20);

		if (!cyanBlobs.isEmpty()) {
			VisualObject goal = map.get(VisualObjects.BlueGoal);
			Blob blob = cyanBlobs.get(0);

			Point2D.Double cp = new Point2D.Double();
			cp.x = (blob.xmin + blob.xmax) / 2 - width / 2;
			cp.y = (blob.ymin + blob.ymax) / 2 - width / 2;
			goal.center = cp;
			goal.cf = blob.mass;
		}

		if (!yellowBlobs.isEmpty()) {
			VisualObject goal = map.get(VisualObjects.YellowGoal);
			Blob blob = yellowBlobs.get(0);

			Point2D.Double cp = new Point2D.Double();
			cp.x = (blob.xmin + blob.xmax) / 2 - width / 2;
			cp.y = (blob.ymin + blob.ymax) / 2 - width / 2;
			goal.center = cp;
			goal.cf = blob.mass;
		}
	}

	private Point2D calculateAngle(Point2D point) {
		double angle1 = Math.toDegrees(0.8);
		double angle2 = angle1 * height / width;

		double angleX = point.getX() / width * angle1;
		double angleY = point.getY() / height * angle2;
		return new Point2D.Double(angleX, angleY);
	}

	public VisualObject get(VisualObjects key) {
		return map.get(key);
	}

	public GCD getGCD() {
		return gcd;
	}
}
