/*
 * 作成日: 2008/06/29
 */
package jp.ac.fit.asura.nao.communication;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.communication.messages.AsuraMessage;
import jp.ac.fit.asura.nao.communication.messages.AsuraMessage.Type;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 *
 * @version $Id: AsuraLink.java 709 2008-11-23 07:40:31Z sey $
 *
 */
public class AsuraLink {
	private static final int PACKET_MAGIC = 'a';

	private Logger log = Logger.getLogger(AsuraLink.class);

	private RobotContext robotContext;

	private AsuraLinkStrategyReceiveData rcvData;

	public AsuraLink(RobotContext context) {
		this.robotContext = context;
	}

	public boolean hasValidHeader(byte[] b) {
		if (b.length > 8) {
			// LE, Little endian
			return b[0] == PACKET_MAGIC && b[1] == 0 && b[2] == 0 && b[3] == 0;
		}
		return false;
	}

	public void parse(byte[] bytes) {
		assert hasValidHeader(bytes);

		ByteBuffer buf = ByteBuffer.wrap(bytes);
		buf.order(ByteOrder.LITTLE_ENDIAN);

		// read magic packet
		int magic = buf.getInt();

		assert magic == PACKET_MAGIC;

		// read data length
		int dataLength = buf.getInt();
		assert buf.remaining() == dataLength;

		log.info(dataLength);

		if (dataLength != buf.remaining()) {
			log.error("Corrupted message received. length:" + dataLength);
		}

		try {
			LOOP1: while (true) {
				log.trace("parse message.");
				int sender = buf.getInt();
				long frame = buf.getLong();
				int numMessage = buf.getInt();

				log.trace("parse data. length:" + dataLength + " sender:"
						+ sender + " frame:" + frame + " numMessage:"
						+ numMessage);

				for (int i = 0; i < numMessage; i++) {
					AsuraMessage.Type type = AsuraMessage.Type.toType(buf
							.getInt());

					switch (type) {
					case NONE:	break;
					case PENALTY:	break;
					case STATUS:	break;
					case STRATEGY:
						rcvData = robotContext.getCommunication().getStrategyReceiveData();
						log.info("Strategy packet received!!");
						rcvData.parseBuf(buf, sender, frame);
						break;
					case WMOBJECT:
					default: {
						log.error("Unknown message type:" + type);
						break LOOP1;
					}
					}
				}
				break;
			}
		} catch (BufferUnderflowException e) {
			log.error("Message parse error.", e);
		}
	}
}
