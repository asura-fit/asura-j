/*
 * 作成日: 2008/06/11
 */
package jp.ac.fit.asura.nao.strategy;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class StrategySystem implements RobotLifecycle {
	public enum Team {
		Red, Blue
	};

	public enum Role {
		Goalie, Striker, Libero, Defender
	};

	private RobotContext context;

	private TaskManager taskManager;

	private Team team;

	private Role role;

	public StrategySystem() {
		taskManager = new TaskManager();
	}

	public void init(RobotContext rctx) {
		this.context = rctx;

		// set default role.
		switch (rctx.getCore().getId()) {
		case 0:
			role = Role.Goalie;
			break;
		case 1:
			role = Role.Striker;
			break;
		case 2:
			role = Role.Libero;
			break;
		case 3:
			role = Role.Defender;
			break;
		default:
			assert false;
			role = null;
		}

		taskManager.init(rctx);
	}

	public void start() {
	}

	public void stop() {
	}

	public void step() {
	}

	/**
	 * @return the team
	 */
	public Team getTeam() {
		return team;
	}

	/**
	 * @param team
	 *            the team to set
	 */
	public void setTeam(Team team) {
		this.team = team;
	}

	/**
	 * @return the role
	 */
	public Role getRole() {
		return role;
	}
}
