/*
 * 作成日: 2009/04/05
 */
package jp.ac.fit.asura.nao.vision;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import jp.ac.fit.asura.nao.Camera.PixelFormat;
import junit.framework.TestCase;

/**
 * @author $Author: sey $
 *
 * @version $Id: $
 *
 */
public class GCDTest extends TestCase {
	public void testDetectInt() throws Exception {
		GCD gcd = new GCD();
		gcd.loadTMap("test/normal.tm2");
		IntBuffer buf = IntBuffer.allocate(16);
		byte[] gcdPlane = new byte[16];

		for (int i = 0; i < 0xFFFFFF;) {
			buf.clear();
			while (buf.position() < buf.limit()) {
				buf.put(i++);
			}
			buf.flip();
			gcd.detect(buf, gcdPlane, PixelFormat.RGB444);
		}
	}

	public void testDetectByte() throws Exception {
		GCD gcd = new GCD();
		gcd.loadTMap("test/normal.tm2");
		ByteBuffer buf = ByteBuffer.allocateDirect(16 / 2 * 4);
		byte[] gcdPlane = new byte[16];

		for (int i = 0; i < 0xFFFFFFFF;) {
			buf.clear();
			while (buf.position() < buf.limit()) {
				buf.put((byte) ((i & 0xFF000000) >>> 24));
				buf.put((byte) ((i & 0x00FF0000) >>> 16));
				buf.put((byte) ((i & 0x0000FF00) >>> 8));
				buf.put((byte) ((i & 0x000000FF)));
			}
			buf.flip();
			gcd.detect(buf, gcdPlane, PixelFormat.YUYV);
		}
	}
}
