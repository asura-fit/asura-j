package jp.ac.fit.asura.nao.communication;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import jp.ac.fit.asura.nao.DatagramService;
import jp.ac.fit.asura.nao.communication.messages.AsuraMessage;
import jp.ac.fit.asura.nao.communication.messages.AsuraMessage.Type;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.localization.WorldObjects;
import jp.ac.fit.asura.nao.strategy.Role;
import jp.ac.fit.asura.nao.strategy.StrategyContext;

/**
 * ストラテジ協調の送信クラス.
 *
 * @author takata
 *
 */

public class AsuraLinkStrategySendData extends AsuraLinkSendData {

	private Logger log = Logger.getLogger(AsuraLinkStrategySendData.class);

	private StrategyContext context;

	/** ペナルティ状態 */
	private boolean isPenalty = false;

	/** ポジション */
	private Role role = Role.Striker;

	/** メッセージタイプ */
	private static AsuraMessage.Type type;

	/** 1メッセージあたりのByte長 */
	private static int messageLength;

	public AsuraLinkStrategySendData(StrategyContext context) {
		super(context);
		this.context = context;

		type = Type.STRATEGY; // メッセージタイプはSTRATEGY
		messageLength = 10;


	}

	@Override
	void createData() {
		log.trace("creating StrategySendData... by number " + sender + " of Team" + context.getSuperContext().getTeamId());

		// 送信用ByteBufferをクリアする
		clearBuf();


		updateData(); // ペナルティ状態, ポジションを最新状態にする

		//試しにデータ長をput
		//sendData.putInt(100);

		// データをセット
		putSendTime(); // 送信時刻
		putMessageNum(1); // メッセージ数.今は便宜的に1

		putMessageType(type);	// メッセージタイプ

		// Roleをput
		sendData.putInt(role.getRoleNum());

		// ペナルティ状態をput
		if (isPenalty) {
			sendData.putInt(1);
		} else {
			sendData.putInt(0);
		}

		// WorldObjectのデータをputする
		for (WorldObjects type : WorldObjects.values()) {
			WorldObject wo = context.getSuperContext().getLocalization().get(type);

			sendData.putInt(wo.getConfidence());		// 信頼度
			sendData.putInt(wo.getX());					// チーム座標系でのX座標
			sendData.putInt(wo.getY());					// チーム座標系でのY座標
			sendData.putInt(wo.getDistance());			// 距離
			sendData.putFloat(wo.getHeading());			// Heading
			sendData.putLong(wo.getDifftime());			// 最後に認識してからの時間[ms]

			if (wo.getType() == WorldObjects.Self)
				sendData.putFloat(wo.getYaw());			// Yaw姿勢（Selfのみ）
		}

	}

	/**
	 * クラスのデータで現在値で更新する.
	 */
	private void updateData() {
		isPenalty = context.isPenalized();
		role = context.getRole();
	}

	/**
	 * ストラテジデータを送信する.
	 */
	public void send() {
		createData();	// 送信用ByteBufferにデータを格納する

		super.send();
	}

}
