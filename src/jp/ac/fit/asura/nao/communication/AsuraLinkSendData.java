package jp.ac.fit.asura.nao.communication;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.log4j.Logger;

import jp.ac.fit.asura.nao.DatagramService;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.communication.messages.AsuraMessage;
import jp.ac.fit.asura.nao.strategy.StrategyContext;

/**
 * AsuraLinkの送信データを表す.
 *
 * @author takata
 */

abstract public class AsuraLinkSendData implements AsuraLinkData{
	private final Logger log = Logger.getLogger(AsuraLinkSendData.class);

	RobotContext rc;

	/** Magic Packet */
	protected static byte[] magic = {'a', 0, 0, 0};

	/** 送信者のロボットID */
	protected int sender;

	/** データ長 */
	protected int dataLength;

	/** 送信用ByteBuffer */
	protected ByteBuffer sendData;

	/** 書き込み開始位置(MP,senderの後ろ). */
	protected int mark;

	private StrategyContext context;


	public AsuraLinkSendData(StrategyContext context) {
		this.context = context;
		rc = context.getSuperContext();

		log.info("create AsuraLinkSendData's buffer.");

		sendData = ByteBuffer.allocate(512);
		sendData.clear();
		sendData.order(ByteOrder.LITTLE_ENDIAN);

		// MagicPacket, senderをセット
		sendData.put(magic);
		sendData.putInt(196);	// DataLengthを0クリア
		sendData.putInt(sender);

		// 今後書き換えが必要なのは以降なので、ここをマークしておく
		mark = sendData.position();
	}

	public void init(RobotContext rbcx) {
		log.trace("init AsuraLinkSendData.");

		sender = rbcx.getRobotId();
	}


	/**
	 * 送信データを作成し、ByteBufferに格納.
	 */
	abstract void createData();

	/** sendDataを送信 */
	protected void send() {
		sendData.flip();
		rc.getDatagramService().send(sendData);
	}

	/** 送信用ByteBufferを初期化(positionをMP,senderの後ろに戻す) **/
	protected void clearBuf() {
		sendData.position(mark);

		log.trace("strategy send data is cleared. position:" + sendData.position());
	}

	/** 送信用ByteBufferに送信時刻をputする */
	protected void putSendTime() {
		long time = context.getTime();

		sendData.putLong(time);

		log.trace("set Time to StrategySendBuffer: " + time);
	}

	/** 送信用ByteBufferにメッセージ数をputする */
	protected void putMessageNum(int n) {
		sendData.putInt(n);

		log.trace("set MessageNum to StrategySnedBuffer: " + n);
	}

	/** 送信用ByteBufferにメッセージタイプをputする */
	protected void putMessageType(AsuraMessage.Type type) {
		sendData.putInt(type.getType());

		log.trace("set MessageType to StrategySendBuffer: " + type.name() + "(" + type.getType() + ")");
	}

	/** 現在のメッセージ長を計算し、送信用ByteBufferにputする */
	protected void putMessageLength() {
	}

}
