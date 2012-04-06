package jp.ac.fit.asura.nao.communication;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.localization.WorldObject_set;
import jp.ac.fit.asura.nao.localization.WorldObjects;
import jp.ac.fit.asura.nao.physical.Ball;
import jp.ac.fit.asura.nao.strategy.Role;
import jp.ac.fit.asura.nao.strategy.StrategyContext;

import org.apache.log4j.Logger;

public class AsuraLinkStrategyReceiveData extends AsuraLinkReceiveData {
	private Logger log = Logger.getLogger(AsuraLinkStrategyReceiveData.class);

	private StrategyContext context;

	/**
	 * ストラテジの協調でやり取りするデータを表す内部クラス. 受信時刻、ペナルティ状態、ポジションとWorldObjectの情報.
	 *
	 * @author takata
	 */
	private class StrategyInfo extends WorldObject_set {
		/** データを受信した時刻 **/
		private long time;

		/** ペナルティ状態 */
		private boolean isPenalty;

		/** ポジション */
		private Role role;

		/** WorldObjectデータ */
		private Map<WorldObjects, WorldObject> wo;

		/** コンストラクタ */
		protected StrategyInfo() {
			wo = new HashMap<WorldObjects, WorldObject>();

			// WorldObjectをインスタンス化
			for (WorldObjects type : WorldObjects.values()) {
				wo.put(type, new WorldObject(type));
			}
		}

		/**
		 * 受信時刻を更新する.
		 *
		 * @param time
		 *            データを受信した時刻
		 */
		protected void updateTime(long time) {
			this.time = time;

			log.trace("set Time receiving strategy data : " + time);
		}

		/**
		 * ポジションを更新する.
		 *
		 * @param role
		 */
		protected void updateRole(int roleNum) {
			role = Role.toRole(roleNum);
		}

		/**
		 * ペナルティ状態を更新する.
		 *
		 * @param isPenalty
		 *            Penalizedなら1
		 */
		protected void updateIsPenalty(int isPenalty) {
			if (isPenalty == 1) {
				this.isPenalty = true;
			} else {
				this.isPenalty = false;
			}

		}

		/**
		 * WorldObjectの情報を更新する. 信頼度, X座標, Y座標, 距離, Heading, 最後に認識してからの時間
		 * (Selfは+Yaw姿勢) のみ.
		 *
		 * @param type
		 * @param w
		 */
		protected void updateWorldObjectData(ByteBuffer buf) {
			WorldObjects type = WorldObjects.toWorldObjects(buf.getInt());

			WorldObject obj = wo.get(type);

			setConfidence(obj, buf.getInt());
			setTeamX(obj, buf.getInt());
			setTeamY(obj, buf.getInt());
			setDistance(obj, buf.getInt());
			setHeading(obj, buf.getFloat());
			setDifftime(obj, buf.getLong());

			if (type == WorldObjects.Self)
				setTeamYaw(obj, buf.getFloat());

			log.trace("update " + type + "'s data. conf:" + obj.getConfidence()
					+ " x:" + obj.getX() + " y:" + obj.getY() + " dist:"
					+ obj.getDistance() + " head:" + obj.getHeading() + " diff:"
					+ obj.getDifftime());

		}

		/**
		 * 情報が古い場合は信頼度を0にする.
		 */
		protected void checkData() {
			long diff = context.getTime() - time;
			if (diff > 10000) {
				for (WorldObjects type : WorldObjects.values()) {
					wo.get(type).invalidate();
				}

			}
		}
	}

	/** 他ロボットのデータ */
	private List<StrategyInfo> teammate_data;

	/** コンストラクタ */
	public AsuraLinkStrategyReceiveData(StrategyContext context) {
		super(context);

		this.context = context;

		teammate_data = new ArrayList<StrategyInfo>();

		// プレイヤー数だけStrategyInfoをインスタンス化
		for (int i = 0; i < TeamInfo.MAX_NUM_PLAYERS; i++) {
			teammate_data.add(i, new StrategyInfo());
		}
	}

	@Override
	public void parseBuf(ByteBuffer buf, int sender, long frame) {
		StrategyInfo data = teammate_data.get(sender-1);

		data.updateTime(frame); // 受信時刻の更新
		data.updateRole(buf.getInt()); // ポジションの更新
		data.updateIsPenalty(buf.getInt()); // ペナルティ状態の更新

		for (WorldObjects type : WorldObjects.values()) {
			data.updateWorldObjectData(buf);
		}
	}

	/**
	 * robotIdから受け取ったデータを持っているWorldObjectを取得する.
	 *
	 * @param type
	 * @param robotId
	 * @return
	 */
	public WorldObject get(WorldObjects type, int robotId) {
		log.trace("get Object's data: " + type + " " + robotId);
		return teammate_data.get(robotId-1).wo.get(type);
	}

	/**
	 * ロボットIDがrobotIdであるチームメイトのペナルティ状態を取得する.
	 * @param robotId
	 * @return
	 */
	public boolean isPenalized(int robotId) {
		return teammate_data.get(robotId-1).isPenalty;
	}

	/**
	 * ロボットIDがrobotIdであるチームメイトのポジションを取得する.
	 * @param robotId
	 * @return
	 */
	public Role getRole(int robotId) {
		return teammate_data.get(robotId-1).role;
	}

	/**
	 * 情報が古くないか確認する.
	 */
	public void checkData() {
		for (StrategyInfo td : teammate_data) {
			td.checkData();
		}
	}

}
