/*
 * 作成日: 2008/05/07
 */
package jp.ac.fit.asura.nao;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import jp.ac.fit.asura.nao.Camera.PixelFormat;

/**
 * @author $Author: sey $
 *
 * @version $Id: Image.java 709 2008-11-23 07:40:31Z sey $
 *
 */
public interface Image {
	public enum BufferType {
		BYTES, INT
	}

	public BufferType getBufferType();

	public ByteBuffer getByteBuffer() throws UnsupportedOperationException;

	public IntBuffer getIntBuffer() throws UnsupportedOperationException;

	public PixelFormat getPixelFormat();

	public long getTimestamp();

	public int getHeight();

	public int getWidth();
}
