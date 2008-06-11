/*
 * 作成日: 2008/06/11
 */
package jp.ac.fit.asura.nao.strategy;

import jp.ac.fit.asura.nao.Context;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.localization.WorldObjects;

/**
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public class StrategyContext extends Context {
	private RobotContext robotContext;
	private TaskManager taskManager;

	private boolean isMotionSet;
	private boolean isHeadSet;

	public StrategyContext(RobotContext robotContext, TaskManager taskManager) {
		this.robotContext = robotContext;
		this.taskManager = taskManager;
	}

	public RobotContext getSuperContext() {
		return robotContext;
	}

	public TaskManager getTaskManager() {
		return taskManager;
	}

	protected void init() {
		isMotionSet = false;
		isHeadSet = false;
	}

	public void makemotion(int id) {
		getSuperContext().getMotor().makemotion(id, null);
		isMotionSet = true;
	}

	public void makemotion_head() {

	}

	/**
	 * このステップ中でmakemotion_head*が実行されていればtrueを返します.
	 * 
	 * @return the isHeadSet
	 */
	public boolean isHeadSet() {
		return isHeadSet;
	}

	/**
	 * このステップ中でmakemotionが実行されていればtrueを返します.
	 * 
	 * @return the isMotionSet
	 */
	public boolean isMotionSet() {
		return isMotionSet;
	}

	public WorldObject getBall() {
		return getSuperContext().getLocalization().get(WorldObjects.Ball);
	}

	public WorldObject getOwnGoal() {
		if (getSuperContext().getStrategy().getTeam() == StrategySystem.Team.Blue)
			return getSuperContext().getLocalization().get(
					WorldObjects.BlueGoal);
		else
			return getSuperContext().getLocalization().get(
					WorldObjects.YellowGoal);
	}

	public WorldObject getTargetGoal() {
		if (getSuperContext().getStrategy().getTeam() == StrategySystem.Team.Blue)
			return getSuperContext().getLocalization().get(
					WorldObjects.YellowGoal);
		else
			return getSuperContext().getLocalization().get(
					WorldObjects.BlueGoal);
	}
}
