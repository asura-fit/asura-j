/*
 * 作成日: 2008/06/11
 */
package jp.ac.fit.asura.nao.strategy;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.VisualCycle;
import jp.ac.fit.asura.nao.VisualFrameContext;
import jp.ac.fit.asura.nao.strategy.schedulers.Scheduler;

/**
 * @author $Author: sey $
 *
 * @version $Id: StrategySystem.java 717 2008-12-31 18:16:20Z sey $
 *
 */
public class StrategySystem implements VisualCycle {
	private RobotContext robotContext;

	private TaskManager taskManager;

	private Team team;

	private Role role;

	private Scheduler scheduler;

	private Scheduler nextScheduler;

	private StrategyContext context;

	public StrategySystem() {
		taskManager = new TaskManager();
		team = Team.Red;
	}

	@Override
	public void init(RobotContext rctx) {
		this.robotContext = rctx;

		// set default role.
		switch (rctx.getRobotId()) {
		case 0:
			setRole(Role.Goalie);
			break;
		case 1:
			setRole(Role.Striker);
			break;
		case 2:
			setRole(Role.Libero);
			break;
		case 3:
			setRole(Role.Defender);
			break;
		default:
			assert false;
			setRole(null);
		}
		context = new StrategyContext(robotContext);
	}

	@Override
	public void start() {
		taskManager.init(robotContext);
		scheduler = null;
		nextScheduler = (Scheduler) taskManager.find("StrategySchedulerTask");
	}

	@Override
	public void stop() {
	}

	@Override
	public void step(VisualFrameContext frameContext) {
		context.update(frameContext);

		// スケジューラ切り替え
		if (nextScheduler != null) {
			if (scheduler != null)
				scheduler.leave(context);
			scheduler = nextScheduler;
			nextScheduler = null;
			scheduler.enter(context);
		}

		// beforeを実行
		for (Task task : taskManager.values())
			task.before(context);

		scheduler.continueTask(context);

		// afterを実行
		for (Task task : taskManager.values())
			task.after(context);
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
		if (team == Team.Red) {
			robotContext.getEffector().setLed("LFoot/Led/Red", 1.0f);
			robotContext.getEffector().setLed("LFoot/Led/Blue", 0.0f);
			robotContext.getEffector().setLed("LFoot/Led/Green", 0.0f);
		}
		if (team == Team.Blue) {
			robotContext.getEffector().setLed("LFoot/Led/Red", 0.0f);
			robotContext.getEffector().setLed("LFoot/Led/Blue", 1.0f);
			robotContext.getEffector().setLed("LFoot/Led/Green", 0.0f);
		}
	}

	/**
	 * @return the role
	 */
	public Role getRole() {
		return role;
	}

	public void setRole(Role newRole) {
		this.role = newRole;
	}

	/**
	 * @return the taskManager
	 */
	public TaskManager getTaskManager() {
		return taskManager;
	}

	/**
	 * @return the scheduler
	 */
	public Scheduler getScheduler() {
		return scheduler;
	}

	public void setNextScheduler(Scheduler scheduler) {
		nextScheduler = scheduler;
	}
}
