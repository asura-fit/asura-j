/*
 * 作成日: 2009/04/05
 */
package jp.ac.fit.asura.nao.misc;

import java.io.IOException;
import java.io.PushbackInputStream;

/**
 * @author $Author: sey $
 *
 * @version $Id: $
 *
 */
public class TMap extends Pixmap {
	private String pixelFormat;
	private int depth1;
	private int depth2;
	private int depth3;

	public TMap(String fileName) throws IOException {
		super(fileName);
		width = -1;
		height = -1;
		depth = -1;
		version = -1;
	}

	protected boolean readMagic(PushbackInputStream is) throws IOException {
		StringBuilder sb = new StringBuilder();
		int read;
		while ((read = is.read()) != -1 && sb.length() < 4) {
			char ch = (char) read;
			sb.append(ch);
		}
		return sb.toString().equals("TMAP");
	}

	protected void readHeaders(PushbackInputStream is) throws IOException {
		skipWhitespaces(is);
		readComments(is);
		pixelFormat = readLine(is);

		skipWhitespaces(is);
		readComments(is);
		depth1 = readInt(is);

		skipWhitespaces(is);
		readComments(is);
		depth2 = readInt(is);

		skipWhitespaces(is);
		readComments(is);
		depth3 = readInt(is);
		is.read();
	}

	protected int calcLength() {
		return depth1 * depth2 * depth3;
	}

	public int getDepth1() {
		return depth1;
	}

	public int getDepth2() {
		return depth2;
	}

	public int getDepth3() {
		return depth3;
	}

	public String getPixelFormat() {
		return pixelFormat;
	}
}
