/*
 * 作成日: 2008/12/30
 */
package jp.ac.fit.asura.nao.webots;

import java.nio.ByteBuffer;

import jp.ac.fit.asura.nao.DatagramService;

import com.cyberbotics.webots.controller.Emitter;
import com.cyberbotics.webots.controller.Receiver;
import com.cyberbotics.webots.controller.Robot;

/**
 * @author sey
 *
 * @version $Id: $
 *
 */
class Webots6DatagramService implements DatagramService {
	private Receiver receiver;
	private Emitter emitter;

	/**
		 *
		 */
	public Webots6DatagramService(Robot robot) {
		emitter = robot.getEmitter("emitter");
		receiver = robot.getReceiver("receiver");
		receiver.enable(Webots6Player.SIMULATION_STEP);
	}

	public void receive(ByteBuffer buf) {
		if (receiver.getQueueLength() > 0) {
			byte[] data = receiver.getData();
			buf.put(data, 0, Math.min(data.length, buf.remaining()));
			receiver.nextPacket();
		}
	}

	public byte[] receive() {
		if (receiver.getQueueLength() > 0) {
			byte[] data = receiver.getData();
			receiver.nextPacket();
			return data;
		}
		return null;
	}

	public int send(ByteBuffer buf) {
		byte[] tmp = new byte[buf.remaining()];
		buf.get(tmp, 0, buf.remaining());
		return emitter.send(tmp);
	}
}
