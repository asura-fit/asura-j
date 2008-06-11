/*
 * 作成日: 2008/04/24
 */
package jp.ac.fit.asura.nao.vision;

import java.awt.geom.Point2D;
import java.util.EnumMap;

import jp.ac.fit.asura.nao.Image;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.Sensor;

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

	/**
	 * 
	 */
	public VisualCortex() {
		gcd = new GCD();
		map = new EnumMap<VisualObjects, VisualObject>(VisualObjects.class);

		map.put(VisualObjects.Ball, new VisualObject());
		map.put(VisualObjects.YellowGoal, new VisualObject());
		map.put(VisualObjects.BlueGoal, new VisualObject());
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
		int orangeCount = 0;
		Point2D.Double cp = new Point2D.Double();
		for (int i = 0; i < gcdPlane.length; i++) {
			if (gcdPlane[i] == GCD.cORANGE) {
				cp.x += i % width - width / 2;
				cp.y += i / width - height / 2;
				orangeCount++;
			}
		}
		if (orangeCount > 5) {
			VisualObject ball = map.get(VisualObjects.Ball);
			cp.x /= orangeCount;
			cp.y /= orangeCount;
			ball.center = cp;
			ball.cf = orangeCount;
		}
	}

	private void findGoal() {
		int cyanCount = 0;
		int yellowCount = 0;
		Point2D.Double bcp = new Point2D.Double();
		Point2D.Double ycp = new Point2D.Double();
		for (int i = 0; i < gcdPlane.length; i++) {
			if (gcdPlane[i] == GCD.cCYAN) {
				bcp.x += i % width - width / 2;
				bcp.y += i / width - height / 2;
				cyanCount++;
			} else if (gcdPlane[i] == GCD.cYELLOW) {
				bcp.x += i % width - width / 2;
				bcp.y += i / width - height / 2;
				yellowCount++;
			}
		}

		if (cyanCount > 20) {
			VisualObject goal = map.get(VisualObjects.BlueGoal);
			bcp.x /= cyanCount;
			bcp.y /= cyanCount;
			goal.center = bcp;
			goal.cf = cyanCount;
		}
		if (yellowCount > 20) {
			VisualObject goal = map.get(VisualObjects.YellowGoal);
			ycp.x /= yellowCount;
			ycp.y /= yellowCount;
			goal.center = bcp;
			goal.cf = yellowCount;
		}
	}

	public VisualObject get(VisualObjects key) {
		return map.get(key);
	}

	public GCD getGCD() {
		return gcd;
	}
}
