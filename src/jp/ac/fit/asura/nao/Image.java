/*
 * 作成日: 2008/05/07
 */
package jp.ac.fit.asura.nao;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import jp.ac.fit.asura.nao.Camera.PixelFormat;

/**
 * カメラから取得した画像バッファのインターフェイス.
 *
 * 画像の形式は{@link #getPixelFormat}で与えられます.
 *
 * 画像は何らかのバッファに格納されています. どの種類のバッファに格納されているかは{@link BufferType}で示されます.
 *
 * ピクセルの格納方式にはPacked formatとPlanar formatがあり、Packed formatでは
 * {@link BufferType#INT}と{@link BufferType#BYTES}が、Planar formatでは
 * {@link BufferType#BYTES}がサポートされています.
 *
 *
 *
 * @author $Author: sey $
 *
 * @version $Id: Image.java 709 2008-11-23 07:40:31Z sey $
 *
 */
public interface Image {
	public enum BufferType {
		BYTES, INT
	}

	/**
	 * この画像の画像バッファのタイプを返します.
	 *
	 * @return 画像バッファのタイプ. {@link BufferType#BYTES}の場合は{@link #getByteBuffer()}
	 *         が、{@link BufferType#INT}の場合は{@link #getIntBuffer()}が利用可能.
	 */
	public BufferType getBufferType();

	public ByteBuffer getByteBuffer() throws UnsupportedOperationException;

	public IntBuffer getIntBuffer() throws UnsupportedOperationException;

	/**
	 * この画像のピクセル形式を返します.
	 *
	 * @return
	 */
	public PixelFormat getPixelFormat();

	/**
	 * この画像を撮影した時間をミリ秒単位で返します.
	 *
	 * @return 撮影した時間.
	 */
	public long getTimestamp();

	/**
	 * この画像の縦方向のピクセル数を返します.
	 *
	 * @return 縦方向のピクセル数
	 */
	public int getHeight();

	/**
	 * この画像の横方向のピクセル数を返します.
	 *
	 * @return 横方向のピクセル数
	 */
	public int getWidth();

	/**
	 * この画像が保持するバッファを開放し、再利用可能な状態にします.
	 *
	 * この呼び出しあと、この画像の保持するバッファの内容は不定となります.
	 *
	 * 画像を再び使用可能な状態にするには、{@link Camera#updateImage(Image)}を呼び出す必要があります.
	 */
	public void dispose();
}
