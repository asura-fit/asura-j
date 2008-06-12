/*
 * 作成日: 2008/06/11
 */
package jp.ac.fit.asura.nao.strategy;

import jp.ac.fit.asura.nao.Context;
import jp.ac.fit.asura.nao.RoboCupGameControlData;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.localization.WorldObjects;
import jp.ac.fit.asura.nao.strategy.schedulers.Scheduler;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class StrategyContext extends Context {
	private RobotContext robotContext;

	private boolean isMotionSet;
	private boolean isHeadSet;

	public StrategyContext(RobotContext robotContext) {
		this.robotContext = robotContext;
	}

	public RobotContext getSuperContext() {
		return robotContext;
	}

	public RoboCupGameControlData getGameState() {
		return getSuperContext().getGameControlData();
	}

	public Scheduler getScheduler() {
		return getSuperContext().getStrategy().getScheduler();
	}

	private TaskManager getTaskManager() {
		return getSuperContext().getStrategy().getTaskManager();
	}

	protected void reset() {
		isMotionSet = false;
		isHeadSet = false;
	}

	public void makemotion(int id) {
		getSuperContext().getMotor().makemotion(id, null);
		isMotionSet = true;
	}

	public void makemotion_head(float headYawInDeg, float headPitchInDeg) {
		getSuperContext().getMotor().makemotion_head(headYawInDeg,
				headPitchInDeg);
		isHeadSet = true;
	}

	public void makemotion_head_rel(float headYawInDeg, float headPitchInDeg) {
		getSuperContext().getMotor().makemotion_head_rel(headYawInDeg,
				headPitchInDeg);
		isHeadSet = true;
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
		if (getTeam() == Team.Blue)
			return getSuperContext().getLocalization().get(
					WorldObjects.BlueGoal);
		else
			return getSuperContext().getLocalization().get(
					WorldObjects.YellowGoal);
	}

	public WorldObject getTargetGoal() {
		if (getTeam() == Team.Blue)
			return getSuperContext().getLocalization().get(
					WorldObjects.YellowGoal);
		else
			return getSuperContext().getLocalization().get(
					WorldObjects.BlueGoal);
	}

	public Team getTeam() {
		return getSuperContext().getStrategy().getTeam();
	}

	public Role getRole() {
		return getSuperContext().getStrategy().getRole();
	}

	public Task findTask(String taskName) {
		Task task = getTaskManager().find(taskName);
		assert task != null;
		return task;
	}

	public void pushQueue(Task task) {
		getScheduler().pushQueue(task);
	}

	public void pushQueue(String taskName) {
		Task task = findTask(taskName);
		assert task != null;
		pushQueue(task);
	}
}
