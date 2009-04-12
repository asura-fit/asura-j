/*
 * 作成日: 2009/04/12
 */
package jp.ac.fit.asura.nao.misc;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.output.ProxyOutputStream;

/**
 * @author $Author: sey $
 *
 * @version $Id: $
 *
 */
public class TeeOutputStream extends ProxyOutputStream {
	private List<OutputStream> teeStreams;

	public TeeOutputStream(OutputStream proxy) {
		super(proxy);
		teeStreams = new ArrayList<OutputStream>();
	}

	public synchronized void addStream(OutputStream os){
		teeStreams.add(os);
	}

	public synchronized void removeStream(OutputStream os){
		teeStreams.remove(os);
	}

	public synchronized void write(byte b[]) throws IOException {
		super.write(b);
		for (OutputStream os : teeStreams)
			os.write(b);
	}

	public synchronized void write(byte b[], int off, int len)
			throws IOException {
		super.write(b, off, len);
		for (OutputStream os : teeStreams)
			os.write(b, off, len);
	}

	public synchronized void write(int b) throws IOException {
		super.write(b);
		for (OutputStream os : teeStreams)
			os.write(b);
	}

	public void flush() throws IOException {
		super.flush();
		for (OutputStream os : teeStreams)
			os.flush();
	}

	public void close() throws IOException {
		super.close();
		for (OutputStream os : teeStreams)
			os.close();
	}
}
