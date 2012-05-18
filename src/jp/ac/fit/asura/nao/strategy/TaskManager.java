/*
 * 作成日: 2008/05/18
 */
package jp.ac.fit.asura.nao.strategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jp.ac.fit.asura.nao.RobotContext;
//import jp.ac.fit.asura.nao.strategy.actions.BackShotTask;
import jp.ac.fit.asura.nao.strategy.actions.FrontShotTask;
import jp.ac.fit.asura.nao.strategy.actions.GeneralizedKickTask;
import jp.ac.fit.asura.nao.strategy.actions.InitialTask;
import jp.ac.fit.asura.nao.strategy.actions.InsideKickTask;
import jp.ac.fit.asura.nao.strategy.actions.LookAroundTask;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;
import jp.ac.fit.asura.nao.strategy.permanent.GetUpTask;
import jp.ac.fit.asura.nao.strategy.permanent.ManualSetupTask;
import jp.ac.fit.asura.nao.strategy.schedulers.DefenderStrategyTask;
import jp.ac.fit.asura.nao.strategy.schedulers.ExperimentalScheduler;
import jp.ac.fit.asura.nao.strategy.schedulers.GoalieStrategyTask;
import jp.ac.fit.asura.nao.strategy.schedulers.StrategySchedulerTask;
import jp.ac.fit.asura.nao.strategy.schedulers.StrikerStrategyTask;
import jp.ac.fit.asura.nao.strategy.schedulers.WalkConfigScheduler;
import jp.ac.fit.asura.nao.strategy.tactics.ApproachBallTask;
import jp.ac.fit.asura.nao.strategy.tactics.DefenceTask;
import jp.ac.fit.asura.nao.strategy.tactics.FindBallTask;
import jp.ac.fit.asura.nao.strategy.tactics.GetBallTask;
import jp.ac.fit.asura.nao.strategy.tactics.GoalieKeepTask;
import jp.ac.fit.asura.nao.strategy.tactics.GoalieTask;
import jp.ac.fit.asura.nao.strategy.tactics.GoaliePkTask;
import jp.ac.fit.asura.nao.strategy.tactics.GotoReadyPositionTask;
import jp.ac.fit.asura.nao.strategy.tactics.TurnTask;
import jp.ac.fit.asura.nao.strategy.tactics.BackAreaTask;
import jp.ac.fit.asura.nao.strategy.tactics.DefenceAttackTask;
import jp.ac.fit.asura.nao.strategy.tactics.KickOff01Task;
import jp.ac.fit.asura.nao.strategy.tactics.KickOff02Task;
import jp.ac.fit.asura.nao.strategy.tactics.KickOff03Task;
import org.apache.log4j.Logger;

/**
 *
 * タスクマネージャ.
 *
 * タスクのインスタンスを管理します. タスクの登録、検索、一覧など.
 *
 * @author $Author: sey $
 *
 * @version $Id: TaskManager.java 709 2008-11-23 07:40:31Z sey $
 *
 */
public class TaskManager {
	private Logger log = Logger.getLogger(TaskManager.class);

	private Map<String, Task> tasks;

	private boolean initialized;

	public TaskManager() {
		tasks = new HashMap<String, Task>(64);
		initialized = false;
	}

	/**
	 * タスクマネージャ
	 *
	 * @param context
	 */
	public void init(RobotContext context) {
		registerTasks();

		initialized = true;

		for (Task task : tasks.values())
			task.init(context);
	}

	/**
	 * タスクを登録する.
	 *
	 * これ全部schemeにやらせてよくね?
	 */
	private void registerTasks() {
		add(new BallTrackingTask());
		add(new GetUpTask());
		add(new ManualSetupTask());

		add(new GotoReadyPositionTask());
		add(new FindBallTask());
		add(new ApproachBallTask());
		add(new GoalieKeepTask());
		add(new DefenceTask());
		add(new LookAroundTask());
		add(new InitialTask());
		//add(new BackShotTask());
		// add(new ShootTask());
	    // add(new BackShootTask());
		add(new InsideKickTask());
		add(new FrontShotTask());
		add(new GeneralizedKickTask());
		add(new BackAreaTask());
		add(new DefenceAttackTask());
		add(new GoalieTask());
		add(new GoaliePkTask());

		add(new GetBallTask());
		add(new TurnTask());

		add(new StrategySchedulerTask());
		add(new GoalieStrategyTask());
		add(new StrikerStrategyTask());
        add(new DefenderStrategyTask());
        add(new KickOff01Task());
        add(new KickOff02Task());
        add(new KickOff03Task());

		add(new ExperimentalScheduler());
		add(new WalkConfigScheduler());
	}

	public void add(Task task) {
		assert !tasks.containsKey(task.getName());
		assert !initialized;
		tasks.put(task.getName(), task);
	}

	public Task find(String name) {
		if (!tasks.containsKey(name)) {
			log.error("TaskManager: task not found " + name);
			return null;
		}
		return tasks.get(name);
	}

	public Collection<Task> values() {
		return tasks.values();
	}
}
