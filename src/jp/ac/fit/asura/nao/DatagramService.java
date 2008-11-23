/*
 * 作成日: 2008/06/29
 */
package jp.ac.fit.asura.nao;

import java.nio.ByteBuffer;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public interface DatagramService {
	public void send(ByteBuffer buf);

	public void receive(ByteBuffer buf);

	public byte[] receive();
}
