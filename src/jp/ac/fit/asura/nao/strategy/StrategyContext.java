/*
 * 作成日: 2008/06/11
 */
package jp.ac.fit.asura.nao.strategy;

import jp.ac.fit.asura.nao.Context;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.MotionFrameContext;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.SensorContext;
import jp.ac.fit.asura.nao.VisualFrameContext;
import jp.ac.fit.asura.nao.communication.RoboCupGameControlData;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.localization.WorldObjects;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.MotionParam;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.SomaticContext;
import jp.ac.fit.asura.nao.strategy.schedulers.Scheduler;

/**
 * @author sey
 *
 * @version $Id: StrategyContext.java 717 2008-12-31 18:16:20Z sey $
 *
 */
public class StrategyContext extends Context {
	private RobotContext robotContext;
	private VisualFrameContext visualFrame;
	private MotionFrameContext motionFrame;
	private float headYaw;
	private float headPitch;

	private boolean isMotionSet;
	private boolean isHeadSet;

	public StrategyContext(RobotContext robotContext) {
		this.robotContext = robotContext;
	}

	public RobotContext getSuperContext() {
		return robotContext;
	}

	public SensorContext getSensorContext() {
		return motionFrame.getSensorContext();
	}

	public SomaticContext getSomaticContext() {
		return motionFrame.getSomaticContext();
	}

	public int getFrame() {
		return visualFrame.getFrame();
	}

	public long getTime() {
		return visualFrame.getTime();
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

	protected void update(VisualFrameContext context) {
		isMotionSet = false;
		isHeadSet = false;
		visualFrame = context;
		motionFrame = context.getMotionFrame();

		headYaw = motionFrame.getSensorContext().getJoint(Joint.HeadYaw);
		headPitch = motionFrame.getSensorContext().getJoint(Joint.HeadPitch);
	}

	public void makemotion(Motion motion) {
		getSuperContext().getMotor().makemotion(motion, MotionParam.EMPTY);
		isMotionSet = true;
	}

	public void makemotion(int id) {
		getSuperContext().getMotor().makemotion(id);
		isMotionSet = true;
	}

	public void makemotion(int id, float forward, float left, float turn) {
		MotionParam param = new MotionParam.WalkParam(forward, left, turn);
		getSuperContext().getMotor().makemotion(id, param);
		isMotionSet = true;
	}

	public void makemotion_head(float headYawInDeg, float headPitchInDeg) {
		makemotion_head(MathUtils.toRadians(headYawInDeg), MathUtils
				.toRadians(headPitchInDeg), 200);
	}

	public void makemotion_head(float headYawInRad, float headPitchInRad,
			int duration) {
		headYaw = headYawInRad;
		headPitch = headPitchInRad;
		getSuperContext().getMotor().makemotion_head(headYaw, headPitch,
				duration);
		isHeadSet = true;
	}

	public void makemotion_head_rel(float headYawInDeg, float headPitchInDeg) {
		makemotion_head_rel(MathUtils.toRadians(headYawInDeg), MathUtils
				.toRadians(headPitchInDeg), 200);
	}

	public void makemotion_head_rel(float headYawInRad, float headPitchInRad,
			int duration) {
		headYaw += headYawInRad;
		headPitch += headPitchInRad;
		getSuperContext().getMotor().makemotion_head(headYaw, headPitch,
				duration);
		isHeadSet = true;
	}

	public boolean hasMotion(int id) {
		return getSuperContext().getMotor().hasMotion(id);
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

	public WorldObject getSelf() {
		return getSuperContext().getLocalization().get(WorldObjects.Self);
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
