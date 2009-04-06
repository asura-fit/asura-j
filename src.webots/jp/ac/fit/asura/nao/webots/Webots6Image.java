/*
 * 作成日: 2009/04/04
 */
package jp.ac.fit.asura.nao.webots;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import jp.ac.fit.asura.nao.Image;
import jp.ac.fit.asura.nao.Camera.PixelFormat;

/**
 * @author $Author: sey $
 *
 * @version $Id: $
 *
 */
class Webots6Image implements Image {
	IntBuffer buffer;
	long timestamp;
	int width;
	int height;

	public BufferType getBufferType() {
		return BufferType.INT;
	}

	public IntBuffer getIntBuffer() {
		return buffer;
	}

	public ByteBuffer getByteBuffer() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	public PixelFormat getPixelFormat() {
		return PixelFormat.RGB444;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public void dispose() {
		buffer = null;
	}
}
