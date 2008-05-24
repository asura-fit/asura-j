/*
 * 作成日: 2008/05/24
 */
package jp.ac.fit.asura.nao.vision;

import static jp.ac.fit.asura.nao.vision.VisionUtils.getBlue;
import static jp.ac.fit.asura.nao.vision.VisionUtils.getGreen;
import static jp.ac.fit.asura.nao.vision.VisionUtils.getRed;

import java.awt.Color;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
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

	protected int[] yvuPlane;
	protected byte[] tmap;

	public void loadTMap(String fileName) {
		try {
			ByteBuffer buf = ByteBuffer.allocate(1024);
			RandomAccessFile file = new RandomAccessFile(fileName, "r");
			FileChannel ch = file.getChannel();
			ch.read(buf);
			buf.flip();

			String magic = readLine(buf);
			if (!magic.equals("TMAP"))
				throw new Exception("Can't read magic.");

			while (skipComment(buf)) {
				buf.compact();
				ch.read(buf);
				buf.flip();
			}
			skipLine(buf);
			String size = readLine(buf);
			// System.out.println("size:" + size);
			byte[] tmap = new byte[16 * 64 * 64];
			int offset = 0;
			buf.compact();
			while (ch.read(buf) != -1) {
				buf.flip();
				int length = buf.remaining();
				buf.get(tmap, offset, length);
				offset += length;
				buf.clear();
			}
			this.tmap = tmap;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean skipComment(ByteBuffer buf) {
		if (buf.get(buf.position()) == '#') {
			skipLine(buf);
			return true;
		}
		return false;
	}

	private void skipLine(ByteBuffer buf) {
		while (buf.get() != '\n')
			;
	}

	private String readLine(ByteBuffer buf) {
		int begin = buf.position();
		while (buf.get() != '\n')
			;
		return new String(buf.array(), begin, buf.position() - 1);
	}

	public void detect(int[] plane, byte[] gcdPlane) {
		if (yvuPlane == null || yvuPlane.length != plane.length) {
			yvuPlane = new int[plane.length];
		}
		rgb2yvu(plane, yvuPlane);
		detect2(yvuPlane, gcdPlane);
	}

	private void rgb2yvu(int[] plane, int[] yvuPlane) {
		for (int i = 0; i < plane.length; i++) {
			int pixel = plane[i];
			int r = getRed(pixel);
			int g = getGreen(pixel);
			int b = getBlue(pixel);
			int y = clipping((int) (0.257 * r + 0.504 * g + 0.098 * b + 16), 0,
					255);
			int u = clipping((int) (-0.148 * r - 0.291 * g + 0.439 * b + 128),
					0, 255);
			int v = clipping((int) (0.439 * r - 0.368 * g - 0.071 * b + 128),
					0, 255);
			yvuPlane[i] = y << 16 | v << 8 | u;
		}
	}

	public void gcd2rgb(byte[] gcdPlane, int[] rgbPlane) {
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

	private void detect2(int[] yvuPlane, byte[] gcdPlane) {
		assert tmap != null;
		for (int i = 0; i < yvuPlane.length; i++) {
			int pixel = yvuPlane[i];
			int y = getRed(pixel) >> 4;
			int v = getGreen(pixel) >> 2;
			int u = getBlue(pixel) >> 2;
			gcdPlane[i] = tmap[y << 12 | v << 6 | u];
		}
	}

	private int clipping(int param, int min, int max) {
		if (param > max)
			return min;
		if (param < min)
			return min;
		return param;
	}

	public int[] getYvuPlane() {
		return yvuPlane;
	}
}
