/*
 * 作成日: 2008/06/11
 */
package jp.ac.fit.asura.nao.strategy;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.strategy.schedulers.Scheduler;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public class StrategySystem implements RobotLifecycle {
	private RobotContext robotContext;

	private TaskManager taskManager;

	private Team team;

	private Role role;

	private Scheduler scheduler;
	
	private Scheduler nextScheduler;

	private StrategyContext context;

	public StrategySystem() {
		taskManager = new TaskManager();

	}

	public void init(RobotContext rctx) {
		this.robotContext = rctx;

		// set default role.
		switch (rctx.getCore().getId()) {
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
		taskManager.init(rctx);
		scheduler = null;
		nextScheduler = (Scheduler) taskManager.find("StrategySchedulerTask");

		context = new StrategyContext(robotContext);
	}

	public void start() {
	}

	public void stop() {
	}

	public void step() {
		context.reset();
		
		// スケジューラ切り替え
		if(nextScheduler != null){
			if(scheduler != null)
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
