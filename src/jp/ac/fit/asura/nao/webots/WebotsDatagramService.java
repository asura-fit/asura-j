/*
 * 作成日: 2008/06/29
 */
package jp.ac.fit.asura.nao.webots;

import java.nio.ByteBuffer;

import jp.ac.fit.asura.nao.DatagramService;

import com.cyberbotics.webots.Controller;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class WebotsDatagramService implements DatagramService {
	private int receiver;
	private int emitter;

	/**
	 * 
	 */
	public WebotsDatagramService() {
		emitter = Controller.robot_get_device("emitter");
		receiver = Controller.robot_get_device("receiver");
		
		Controller.receiver_enable(receiver, WebotsPlayer.SIMULATION_STEP);
	}

	public void receive(ByteBuffer buf) {
		if (Controller.receiver_get_queue_length(receiver) > 0) {
			byte[] data = Controller.receiver_get_data(receiver);
			buf.put(data, 0, Math.min(data.length, buf.remaining()));
			Controller.receiver_next_packet(receiver);
		}
	}

	public byte[] receive() {
		if (Controller.receiver_get_queue_length(receiver) > 0) {
			byte[] data = Controller.receiver_get_data(receiver);
			Controller.receiver_next_packet(receiver);
			return data;
		}
		return null;
	}

	public void send(ByteBuffer buf) {
		byte[] tmp = new byte[buf.remaining()];
		buf.get(tmp, 0, buf.remaining());
		Controller.emitter_send_packet(emitter, tmp);
	}
}
