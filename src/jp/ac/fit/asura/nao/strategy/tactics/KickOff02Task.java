package jp.ac.fit.asura.nao.strategy.tactics;

import static jp.ac.fit.asura.nao.motion.Motions.NAOJI_WALKER;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask.Mode;

import org.apache.log4j.Logger;

public class KickOff02Task extends Task {
	private static final Logger log = Logger.getLogger(KickOff02Task.class);
	private BallTrackingTask tracking;
	private int preBalld;
	private float preBallh;
	private int count;
	private int Goaldist;

	@Override
	public void continueTask(StrategyContext context) {
		context.getScheduler().setTTL(700);
		// サイドステップしてから攻める
		if (context.getWalkFlag() == false) {
			// log.info("WalkFlag = 0");
			if (count < 60) {
				count++;
				if (context.hasMotion(Motions.NAOJI_WALKER))
					context.makemotion(NAOJI_WALKER, 0, 0.4f, 0);// ここで右に行くか左に行くか決める。正だと左、負だと右
				else
					context.makemotion(Motions.MOTION_W_LEFT_SIDESTEP);

				// } else if (count < 160) {
				// count++;
				// if (context.hasMotion(Motions.NAOJI_WALKER))
				// context.makemotion(NAOJI_WALKER, 0, -0.5f, 0);
				// else
				// context.makemotion(Motions.MOTION_W_RIGHT_SIDESTEP);
			} else {
				context.setWalkFlag(true);
				log.info("WalkFlag = 1");
				context.getScheduler().abort();
				return;
			}

		}
	}

	@Override
	public void enter(StrategyContext context) {
		context.getScheduler().setTTL(700);
	}

	@Override
	public void init(RobotContext context) {
		tracking = (BallTrackingTask) context.getStrategy().getTaskManager()
				.find("BallTracking");
		count = 0;
	}

	@Override
	public void leave(StrategyContext context) {
		context.setWalkFlag(true);

	}

	@Override
	public String getName() {
		// TODO 自動生成されたメソッド・スタブ
		return "KickOff02Task";
	}

}
