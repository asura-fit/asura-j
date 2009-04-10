/*
 * 作成日: 2009/04/06
 */
package jp.ac.fit.asura.nao.naoji;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import jp.ac.fit.asura.nao.Image;
import jp.ac.fit.asura.nao.Camera.PixelFormat;
import jp.ac.fit.asura.naoji.v4l2.V4L2Buffer;
import jp.ac.fit.asura.naoji.v4l2.Videodev;

/**
 * @author sey
 *
 * @version $Id: $
 *
 */
public class V4L2Image implements Image {
	private Videodev dev;
	private boolean isValid;
	V4L2Buffer buffer;
	int width;
	int height;
	PixelFormat pixelFormat;

	protected V4L2Image(Videodev dev) {
		this.dev = dev;
		buffer = new V4L2Buffer();
		isValid = false;
	}

	public BufferType getBufferType() {
		return BufferType.BYTES;
	}

	public ByteBuffer getByteBuffer() throws UnsupportedOperationException {
		assert isValid;
		return buffer.getBuffer();
	}

	public int getHeight() {
		return height;
	}

	public IntBuffer getIntBuffer() throws UnsupportedOperationException {
		// return getByteBuffer().asIntBuffer();
		throw new UnsupportedOperationException();
	}

	public PixelFormat getPixelFormat() {
		return pixelFormat;
	}

	public long getTimestamp() {
		return buffer.getTimestamp();
	}

	public int getWidth() {
		return width;
	}

	public void dispose() {
		if (isValid) {
			dev.disposeImage(buffer);
			isValid = false;
		}
	}

	protected void setValid(boolean isValid) {
		this.isValid = isValid;
	}
}
