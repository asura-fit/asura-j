/*
 * 作成日: 2009/03/19
 */
package jp.ac.fit.asura.nao.naoji;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Queue;

import jp.ac.fit.asura.nao.DatagramService;

import org.apache.log4j.Logger;

/**
 *
 * TODO sinに託す.
 *
 * 連続して呼び出したときのふるまい考慮せろ
 * バッファは使いまわしで。
 * バッファにqueueの利用？
 *
 *gameControler関連も単体テスト
 * @author $Author: sey $
 *
 * @version $Id: $
 *
 */
public class DatagramSocketService implements DatagramService {
	//ソケット
	public DatagramSocket soc;
	public DatagramPacket snd;
	public DatagramPacket rcv;
	private byte[] rcvbuf = new byte[size];

//	private Queue queue;

	public final static int port = 3001;
	public final static int size = 1024;
	final static int sndPort = 2001;

	//バッファ
//	private byte[] buf;
	private ByteBuffer bbuf;

	private Logger log = Logger.getLogger(DatagramSocketService.class);


	public DatagramSocketService() {
		log.debug("init datagramService");

		try {
			soc = new DatagramSocket(port);
			soc.setBroadcast(true);
		} catch (Exception e) {
			log.error("DatagramSocService: " ,e);
			return;
		}

		rcv = new DatagramPacket(rcvbuf, size);
		snd = null;
		bbuf = ByteBuffer.allocate(size);

		log.debug("initialing finished");
	}

	public void receive(ByteBuffer buf) {
		log.debug("DatagramSocService: receive(ByteBuffer).");

//		bbuf.wrap(receive().clone());
	}

	public byte[] receive() {
		log.debug("DatagramSocService: receive()");

		byte[] tmp = null;

		try {
			log.debug("DatagramSocService: waiting data...");
//			soc.receive(rcv);
//			tmp = rcv.getData();

			log.debug("DatagramSocService: receive a packet");

		} catch (Exception e) {
			//Loggerでlog吐く
			log.error("DatagramSocService: " , e);
		}

		return tmp;
	}

	public void send(ByteBuffer buf) {
		log.debug("DatagramSocService: send()");

		try {
			snd = new DatagramPacket(buf.array(),
									size,
									InetAddress.getByName("255.255.255.255"),
									port);

			if (soc.getBroadcast() != true)
				soc.setBroadcast(true);
			soc.send(snd);
		} catch (Exception e) {
			log.error("DatagramSocService: ", e);
			return;
		}
		log.debug("DatagramSocService: send a packet");
	}

	public void destroy() {
		soc = null;
		snd = null;
		rcv = null;
	}

}
