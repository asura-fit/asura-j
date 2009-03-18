/*
 * 作成日: 2008/06/29
 */
package jp.ac.fit.asura.nao;

import java.nio.ByteBuffer;

/**
 * @author $Author: sey $
 * 
 * @version $Id: DatagramService.java 709 2008-11-23 07:40:31Z sey $
 * 
 */
public interface DatagramService {
	public void send(ByteBuffer buf);

	public void receive(ByteBuffer buf);

	public byte[] receive();
}
