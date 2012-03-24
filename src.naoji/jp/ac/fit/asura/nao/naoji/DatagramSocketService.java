/*
 * 作成日: 2009/03/19
 */
package jp.ac.fit.asura.nao.naoji;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import jp.ac.fit.asura.nao.DatagramService;

import org.apache.log4j.Logger;

/**
 *
 * TODO sinに託す.
 *
 * 連続して呼び出したときのふるまい考慮せろ バッファは使いまわしで。 バッファにqueueの利用？
 *
 *gameControler関連も単体テスト
 *
 * @author $Author: sey $
 *
 * @version $Id: $
 *
 */
public class DatagramSocketService implements DatagramService {
	private static final Logger log = Logger
			.getLogger(DatagramSocketService.class);

	// Channels
	private DatagramChannel chan;

	// ソケット
	public DatagramSocket soc;
	public DatagramPacket snd;

	// private Queue queue;

	// GameController uses 3838 port.
	public final static int port = 3838;
	public final static int size = 1024;
	// final static int sndPort = 2001;

	// バッファ
	private ByteBuffer rcvbuf = ByteBuffer.wrap(new byte[size]);

	public DatagramSocketService() {
		log.debug("init datagramService");

		try {
			chan = DatagramChannel.open();
			chan.configureBlocking(false);
			soc = chan.socket();
			soc.bind(new InetSocketAddress(port));
			soc.setBroadcast(true);
		} catch (Exception e) {
			log.error("DatagramSocService: ", e);
			return;
		}

		snd = null;

		log.debug("initialing finished");
	}

	public void receive(ByteBuffer buf) {
		log.debug("DatagramSocService: receive()");

		try {
			log.trace("DatagramSocService: waiting data...");
			rcvbuf.clear();
			SocketAddress from = chan.receive(buf);
			if (from == null)
				return;

			log.trace("DatagramSocService: receive a packet");
			return;
		} catch (Exception e) {
			// Loggerでlog吐く
			log.error("DatagramSocService: ", e);
		}
		return;
	}

	public byte[] receive() {
		log.debug("DatagramSocService: receive()");

		try {
			log.trace("DatagramSocService: waiting data...");
			rcvbuf.clear();
			SocketAddress from = chan.receive(rcvbuf);
			if (from == null)
				return null;
			rcvbuf.flip();

			log.trace("DatagramSocService: receive a packet");
			byte[] b = new byte[rcvbuf.remaining()];
			rcvbuf.get(b);
			return b;
		} catch (Exception e) {
			// Loggerでlog吐く
			log.error("DatagramSocService: ", e);
		}
		return null;
	}


	public int send(ByteBuffer buf) {
		int rslt;

		log.debug("DatagramSocService: send()");

		try {

			if (soc.getBroadcast() != true)
				soc.setBroadcast(true);

			rslt = chan.send(buf, new InetSocketAddress(InetAddress
					.getByName("255.255.255.255"), port));

		} catch (Exception e) {
			log.error("DatagramSocService: ", e);
			return 0;
		}
		log.debug("DatagramSocService: send a packet");

		return rslt;
	}

	public void destroy() {
		soc = null;
		snd = null;
	}

}
