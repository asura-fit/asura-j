package jp.ac.fit.asura.nao.communication;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import jp.ac.fit.asura.nao.strategy.StrategyContext;

abstract public class AsuraLinkReceiveData implements AsuraLinkData {

	private Logger log = Logger.getLogger(AsuraLinkReceiveData.class);


	public AsuraLinkReceiveData(StrategyContext context) {
	}

	/**
	 * 受信したByteBufferのメッセージ部分を解析する
	 * @param buf
	 */
	public abstract void parseBuf(ByteBuffer buf, int sender, long frame);
}
