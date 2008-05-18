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
 * @version $Id: $
 * 
 */
public class VisualCortex implements RobotLifecycle {
	public enum VisualObjects {
		Ball
	};

	private int[] image;
	private int width;
	private int height;

	private EnumMap<VisualObjects, VisualObject> map;

	private Sensor sensor;

	/**
	 * 
	 */
	public VisualCortex() {
		map = new EnumMap<VisualObjects, VisualObject>(VisualObjects.class);

		map.put(VisualObjects.Ball, new VisualObject());
	}

	public void init(RobotContext rctx) {
		sensor = rctx.getSensor();
	}

	public void start() {
	}

	public void step() {
		clear();
		updateImage(sensor.getImage());
	}

	public void stop() {
	}

	public void updateImage(Image image) {
		this.image = image.getData();
		this.width = image.getWidth();
		this.height = image.getHeight();
		findBall();
	}

	public void clear() {
		for (VisualObject vo : map.values()) {
			vo.cf = 0;
		}
	}

	private void findBall() {
		int orangeCount = 0;
		Point2D.Double cp = new Point2D.Double();
		for (int i = 0; i < image.length; i++) {
			int pixel = image[i];
			int r = pixel2red(pixel);
			int g = pixel2green(pixel);
			int b = pixel2blue(pixel);
			if (r > 0xA0 && g > 0x50 && g < 0xC0 && b > 0x20 && b < 0x40) {
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

	public VisualObject get(VisualObjects key) {
		return map.get(key);
	}

	private int pixel2blue(int pix) {
		return (pix & 0x0000FF);
	}

	private int pixel2green(int pix) {
		return (pix & 0x00FF00) >> 8;
	}

	private int pixel2red(int pix) {
		return (pix & 0xFF0000) >> 16;
	}
}
