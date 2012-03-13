package jp.ac.fit.asura.nao.localization;

import org.apache.log4j.Logger;

/**
 * WorldObjectの各メンバの値を設定するメソッド群.
 * 協調（受信部）のデータ更新のために作成した奴なので,多用しない方がいいと思う.
 * @author takata
 *
 */

public abstract class WorldObject_set {
	private Logger log = Logger.getLogger(WorldObject_set.class);

	protected WorldObject_set() {
	}

	protected WorldObject_set(WorldObjects w) {
	}

	protected void setConfidence(WorldObject wo, int conf) {
		wo.cf = conf;
		log.trace("set " + wo.getType() + "'s conf: " + wo.cf);
	}

	protected void setTeamX(WorldObject wo, int x) {
		wo.team.x = x;
		log.trace("set " + wo.getType() + "'s x: " + wo.team.x);
	}

	protected void setTeamY(WorldObject wo, int y) {
		wo.team.y = y;
		log.trace("set " + wo.getType() + "'s y: " + wo.team.y);
	}

	protected void setDistance(WorldObject wo, int dist) {
		wo.dist = dist;
		log.trace("set " + wo.getType() + "'s dist: " + wo.dist);
	}

	protected void setHeading(WorldObject wo, float head) {
		wo.heading = head;
		log.trace("set " + wo.getType() + "'s head: " + wo.heading);
	}

	protected void setDifftime(WorldObject wo, long diff) {
		wo.difftime = diff;
		log.trace("set " + wo.getType() + "'s difftime: " + wo.difftime);
	}

	protected void setTeamYaw(WorldObject wo, float yaw) {
		wo.teamYaw = yaw;
		log.trace("set " + wo.getType() + "'s yaw: " + wo.teamYaw);
	}
}
