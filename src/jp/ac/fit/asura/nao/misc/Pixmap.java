/*
 * 作成日: 2009/04/05
 */
package jp.ac.fit.asura.nao.misc;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author $Author: sey $
 *
 * @version $Id: $
 *
 */
public class Pixmap {
	protected int version;
	protected byte[] data;
	protected List<String> comments;
	protected int width;
	protected int height;
	protected int depth;

	/**
	 *
	 */
	public Pixmap() throws IOException {
		comments = new ArrayList<String>();
	}

	public Pixmap(byte[] data, int width, int height, int depth) {
		assert width * height == data.length;
		comments = new ArrayList<String>();
		this.data = data;
		this.width = width;
		this.height = height;
		this.depth = depth;
		this.version = 6;
	}

	public void write(String fileName) throws IOException {
		// TODO implement
	}

	public void read(InputStream is) throws IOException {
		PushbackInputStream pbis = new PushbackInputStream(is);

		if (!readMagic(pbis)) {
			throw new IOException("Can't read magic.");
		}

		readHeaders(pbis);

		readBody(pbis);
	}

	protected boolean readMagic(PushbackInputStream is) throws IOException {
		int read;
		skipWhitespaces(is);

		read = is.read();
		char ch = (char) read;
		if (ch != 'P') {
			is.unread(read);
			return false;
		}

		read = is.read();
		if (!Character.isDigit(ch)) {
			is.unread(read);
			return false;
		}
		version = Character.digit(ch, 10);
		skipWhitespaces(is);
		return true;
	}

	protected int skipWhitespaces(PushbackInputStream is) throws IOException {
		int read;
		int spaces = 0;
		while ((read = is.read()) != -1) {
			char ch = (char) read;
			if (Character.isWhitespace(ch)) {
				spaces++;
				continue;
			}
			is.unread(read);
			return spaces;
		}
		return spaces;
	}

	protected void readHeaders(PushbackInputStream is) throws IOException {
		skipWhitespaces(is);
		readComments(is);
		width = readInt(is);

		skipWhitespaces(is);
		readComments(is);
		height = readInt(is);

		skipWhitespaces(is);
		readComments(is);
		depth = readInt(is);
		is.read();
	}

	protected void readComments(PushbackInputStream is) throws IOException {
		int read;
		while ((read = is.read()) != -1) {
			is.unread(read);
			char ch = (char) read;
			if (ch != '#') {
				break;
			}
			comments.add(readLine(is));
			skipWhitespaces(is);
		}
	}

	protected String readLine(PushbackInputStream is) throws IOException {
		StringBuilder sb = new StringBuilder();
		int read;
		while ((read = is.read()) != -1) {
			char ch = (char) read;
			if (ch == '\n')
				return sb.toString();
			if (ch == '\r') {
				if ((read = is.read()) != '\n' && read != -1)
					is.unread(read);
				return sb.toString();
			}
			sb.append(ch);
		}
		return sb.toString();
	}

	protected int readInt(PushbackInputStream is) throws IOException,
			NumberFormatException {
		StringBuilder sb = new StringBuilder();
		int read;
		while ((read = is.read()) != -1) {
			char ch = (char) read;
			if (!Character.isDigit(ch)) {
				is.unread(read);
				break;
			}
			sb.append(ch);
		}
		return Integer.parseInt(sb.toString());
	}

	protected void readBody(PushbackInputStream is) throws IOException {
		int length = calcLength();
		data = new byte[length];

		int read;
		int offset = 0;
		while ((read = is.read(data, offset, length)) > 0) {
			offset += read;
			length -= read;
		}
	}

	protected int calcLength() {
		return width * height;
	}

	/**
	 * @return comments
	 */
	public List<String> getComments() {
		return comments;
	}

	public int getDepth() {
		return depth;
	}

	public byte[] getData() {
		return data;
	}

	/**
	 * @return height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @return version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @return width
	 */
	public int getWidth() {
		return width;
	}
}
