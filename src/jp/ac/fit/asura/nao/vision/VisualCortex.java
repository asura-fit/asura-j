/*
 * 作成日: 2008/04/24
 */
package jp.ac.fit.asura.nao.vision;

import java.awt.geom.Point2D;
import java.util.EnumMap;
import java.util.EnumSet;

import jp.ac.fit.asura.nao.Image;

import com.cyberbotics.webots.Controller;

/**
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public class VisualCortex {
	public enum VisualObjects {
		Ball
	};

	private int[] image;
	private int width;
	private int height;

	private EnumMap<VisualObjects, VisualObject> map;

	/**
	 * 
	 */
	public VisualCortex() {
		map = new EnumMap<VisualObjects, VisualObject>(VisualObjects.class);

		map.put(VisualObjects.Ball, new VisualObject());
	}

	public void updateImage(Image image) {
		clear();
		
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
			int r = Controller.camera_pixel_get_red(pixel);
			int g = Controller.camera_pixel_get_green(pixel);
			int b = Controller.camera_pixel_get_blue(pixel);
			if (r > 0xA0 && g > 0x50 && g < 0xC0 && b > 0x20 && b < 0x40) {
				cp.x += i % width - width / 2;
				cp.y += i / width - height / 2;
				orangeCount++;
			}
		}
		if (orangeCount > 0) {
			VisualObject ball = map.get(VisualObjects.Ball);
			ball.center = cp;
			ball.cf = orangeCount;
		}
	}
	
	public VisualObject get(VisualObjects key){
		return map.get(key);
	}
}
