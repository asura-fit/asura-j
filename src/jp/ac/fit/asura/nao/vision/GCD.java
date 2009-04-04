/*
 * 作成日: 2008/05/24
 */
package jp.ac.fit.asura.nao.vision;

import static jp.ac.fit.asura.nao.vision.VisionUtils.getBlue;
import static jp.ac.fit.asura.nao.vision.VisionUtils.getGreen;
import static jp.ac.fit.asura.nao.vision.VisionUtils.getRed;

import java.awt.Color;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import jp.ac.fit.asura.nao.Image;
import jp.ac.fit.asura.nao.Camera.PixelFormat;
import jp.ac.fit.asura.nao.Image.BufferType;
import jp.ac.fit.asura.nao.misc.TMap;

/**
 * @author $Author: sey $
 *
 * @version $Id: GCD.java 709 2008-11-23 07:40:31Z sey $
 *
 */
public class GCD {
	public static final byte cORANGE = 0;
	public static final byte cCYAN = 1;
	public static final byte cGREEN = 2;
	public static final byte cYELLOW = 3;
	// public static final byte cPINK = 4;
	public static final byte cBLUE = 5;
	public static final byte cRED = 6;
	public static final byte cWHITE = 7;
	// public static final byte cFGREEN = 7;
	public static final byte cBLACK = 9;
	public static final int COLOR_NUM = 10;

	private byte[] yvuPlane;
	private byte[] tmap;

	public void loadTMap(String fileName) {
		try {
			TMap ppm = new TMap(fileName);
			this.tmap = ppm.getData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void detect(Image image, byte[] gcdPlane) {
		if (image.getBufferType() == BufferType.INT) {
			IntBuffer ib = image.getIntBuffer();
			assert ib != null;
			detect(ib, gcdPlane, image.getPixelFormat());
		} else if (image.getBufferType() == BufferType.BYTES) {
			ByteBuffer bb = image.getByteBuffer();
			assert bb != null;
			detect(bb, gcdPlane, image.getPixelFormat());
		}
	}

	public void detect(ByteBuffer plane, byte[] gcdPlane, PixelFormat format) {
		if (format == PixelFormat.UYVY) {
			detectUyvy(plane, gcdPlane);
		} else {
			assert false;
		}
	}

	public void detect(IntBuffer plane, byte[] gcdPlane, PixelFormat format) {
		int length = plane.remaining();
		if (format == PixelFormat.RGB444) {
			if (yvuPlane == null || yvuPlane.length != length * 3) {
				yvuPlane = new byte[length * 3];
			}
			rgb2yvu(plane, yvuPlane);
			detectYvu(yvuPlane, gcdPlane);
		} else {
			assert false;
		}
	}

	public static void uyvy2yvu(ByteBuffer uyvyPlane, byte[] yvuPlane) {
		assert uyvyPlane.remaining() * 6 == yvuPlane.length * 4;
		uyvyPlane.mark();
		for (int i = 0; i < yvuPlane.length;) {
			byte u = uyvyPlane.get();
			byte y1 = uyvyPlane.get();
			byte v = uyvyPlane.get();
			byte y2 = uyvyPlane.get();
			yvuPlane[i++] = y1;
			yvuPlane[i++] = v;
			yvuPlane[i++] = u;
			yvuPlane[i++] = y2;
			yvuPlane[i++] = v;
			yvuPlane[i++] = u;
		}
		uyvyPlane.reset();
	}

	public static void rgb2yvu(IntBuffer plane, byte[] yvuPlane) {
		assert plane.remaining()  == yvuPlane.length * 3;
		for (int i = 0; i < yvuPlane.length; i++) {
			int pixel = plane.get();
			int r = getRed(pixel);
			int g = getGreen(pixel);
			int b = getBlue(pixel);
			int y = clipping((int) (0.257f * r + 0.504f * g + 0.098f * b + 16),
					0, 255);
			int u = clipping(
					(int) (-0.148f * r - 0.291f * g + 0.439f * b + 128), 0, 255);
			int v = clipping(
					(int) (0.439f * r - 0.368f * g - 0.071f * b + 128), 0, 255);
			yvuPlane[i++] = (byte) y;
			yvuPlane[i++] = (byte) v;
			yvuPlane[i++] = (byte) u;
		}
	}

	public static void gcd2rgb(byte[] gcdPlane, int[] rgbPlane) {
		for (int i = 0; i < gcdPlane.length; i++) {
			switch (gcdPlane[i]) {
			case cORANGE:
				rgbPlane[i] = Color.ORANGE.getRGB();
				break;
			case cCYAN:
				rgbPlane[i] = Color.CYAN.getRGB();
				break;
			case cBLUE:
				rgbPlane[i] = Color.BLUE.getRGB();
				break;
			case cGREEN:
				rgbPlane[i] = Color.GREEN.getRGB();
				break;
			case cRED:
				rgbPlane[i] = Color.RED.getRGB();
				break;
			case cWHITE:
				rgbPlane[i] = Color.WHITE.getRGB();
				break;
			case cYELLOW:
				rgbPlane[i] = Color.YELLOW.getRGB();
				break;
			case cBLACK:
				rgbPlane[i] = Color.BLACK.getRGB();
				break;
			default:
				rgbPlane[i] = Color.GRAY.getRGB();
			}
		}
	}

	private void detectYvu(byte[] yvuPlane, byte[] gcdPlane) {
		assert tmap != null;
		int j = 0;
		for (int i = 0; i < gcdPlane.length; i++) {
			byte y = yvuPlane[j++];
			byte v = yvuPlane[j++];
			byte u = yvuPlane[j++];
			// 11110000 = F0
			// 11111100 = FC
			gcdPlane[i] = tmap[(y & 0xF0) << 12 | (v & 0xFC) << 6 | (u & 0xFC)];
		}
	}

	private void detectUyvy(ByteBuffer uyvyPlane, byte[] gcdPlane) {
		assert tmap != null;
		uyvyPlane.mark();
		for (int i = 1; i < gcdPlane.length;) {
			byte u = uyvyPlane.get();
			byte y1 = uyvyPlane.get();
			byte v = uyvyPlane.get();
			byte y2 = uyvyPlane.get();
			// 11110000 = F0
			// 11111100 = FC
			gcdPlane[i-1] = tmap[(y1 & 0xF0) << 12 | (v & 0xFC) << 6
					| (u & 0xFC)];
			gcdPlane[i] = tmap[(y2 & 0xF0) << 12 | (v & 0xFC) << 6
					| (u & 0xFC)];
		}
		uyvyPlane.reset();
	}

	private static int clipping(int param, int min, int max) {
		if (param > max)
			return min;
		if (param < min)
			return min;
		return param;
	}
}
