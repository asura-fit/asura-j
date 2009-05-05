/*
 * 作成日: 2008/04/24
 */
package jp.ac.fit.asura.nao.localization;

import static jp.ac.fit.asura.nao.misc.MathUtils.normalizeAngle180;
import static jp.ac.fit.asura.nao.misc.MathUtils.normalizeAnglePI;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Point2f;

import jp.ac.fit.asura.nao.FrameContext;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.VisualCycle;
import jp.ac.fit.asura.nao.VisualFrameContext;
import jp.ac.fit.asura.nao.event.MotionEventListener;
import jp.ac.fit.asura.nao.event.VisualEventListener;
import jp.ac.fit.asura.nao.localization.self.MonteCarloLocalization;
import jp.ac.fit.asura.nao.localization.self.SelfLocalization;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.strategy.Team;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualObjects;
import jp.ac.fit.asura.nao.vision.perception.BallVisualObject;
import jp.ac.fit.asura.nao.vision.perception.GoalVisualObject;
import jp.ac.fit.asura.nao.vision.perception.VisualObject;

import org.apache.log4j.Logger;

/**
 * @author sey
 *
 * @version $Id: Localization.java 717 2008-12-31 18:16:20Z sey $
 *
 */
public class Localization implements VisualCycle, MotionEventListener,
		VisualEventListener {
	private static final Logger log = Logger.getLogger(Localization.class);

	private SelfLocalization self;

	private Map<WorldObjects, WorldObject> worldObjects;
	private WorldObject woSelf;

	private RobotContext context;
	private FrameContext frame;

	private float odometryForward;
	private float odometryLeft;
	private float odometryTurn;

	public Localization() {
		worldObjects = new HashMap<WorldObjects, WorldObject>();
		for (WorldObjects e : WorldObjects.values())
			worldObjects.put(e, new WorldObject());
		self = new MonteCarloLocalization();
		// self = new GPSLocalization();
		// self = new OdometryLocalization();
		woSelf = worldObjects.get(WorldObjects.Self);
	}

	@Override
	public void init(RobotContext rctx) {
		this.context = rctx;
		self.init(rctx);

		context.getMotor().addEventListener(this);
		context.getVision().addEventListener(this);
	}

	@Override
	public void start() {
		self.start();
	}

	@Override
	public void step(VisualFrameContext context) {
		self.step(context);
	}

	@Override
	public void updateVision(VisualContext vc) {
		updateOdometryBatch();
		frame = vc.getFrameContext();
		self.updateVision(vc);

		mapSelf();
		mapVisualObject(vc.get(VisualObjects.Ball));
		mapVisualObject(vc.get(VisualObjects.BlueGoal));
		mapVisualObject(vc.get(VisualObjects.YellowGoal));

		boolean isRed = context.getStrategy().getTeam() == Team.Red;
		for (WorldObject wo : worldObjects.values())
			copyWorldToTeamCoord(wo, isRed);
		frame = null;
	}

	@Override
	public void stop() {
		self.stop();
	}

	public WorldObject get(WorldObjects wo) {
		return worldObjects.get(wo);
	}

	private void mapSelf() {
		WorldObject wo = worldObjects.get(WorldObjects.Self);
		wo.world.x = self.getX();
		wo.world.y = self.getY();
		wo.worldYaw = MathUtils.toDegrees(self.getHeading());
		wo.worldAngle = MathUtils.toDegrees((float) Math.atan2(wo.world.y,
				wo.world.x));
		wo.cf = self.getConfidence();
		wo.dist = 0;
		wo.heading = 0;
	}

	private void mapVisualObject(VisualObject vo) {
		WorldObject wo;
		boolean distUsable = false;
		int dist;
		if (vo.getType() == VisualObjects.YellowGoal) {
			wo = worldObjects.get(WorldObjects.YellowGoal);
			distUsable = ((GoalVisualObject) vo).distanceUsable;
			dist = ((GoalVisualObject) vo).distance;
		} else if (vo.getType() == VisualObjects.BlueGoal) {
			wo = worldObjects.get(WorldObjects.BlueGoal);
			distUsable = ((GoalVisualObject) vo).distanceUsable;
			dist = ((GoalVisualObject) vo).distance;
		} else if (vo.getType() == VisualObjects.Ball) {
			wo = worldObjects.get(WorldObjects.Ball);
			distUsable = ((BallVisualObject) vo).distanceUsable;
			dist = ((BallVisualObject) vo).distance;
		} else {
			assert false;
			return;
		}

		wo.setVision(vo);
		int voCf = vo.confidence;
		// find ball coordinate
		// WMObject を更新
		// ボールが見えていれば
		if (voCf > 0 && distUsable) {
			Point2f angle = vo.robotAngle;
			float head = MathUtils.toDegrees(angle.x);

			// filter
			dist = wo.distFilter.eval(dist);
			head = wo.headingFilter.eval(head);

			float woHead = woSelf.worldYaw + head;
			float rad = MathUtils.toRadians(woHead);

			wo.world.x = woSelf.world.x + (int) (dist * MathUtils.sin(rad));
			wo.world.y = woSelf.world.y + (int) (dist * MathUtils.cos(rad));
			float rate = (wo.cf >= 600) ? 0.3f : 1.0f;
			float curFr = voCf * rate / (voCf + wo.cf);
			float ballFr = 1 - curFr;
			wo.cf = (int) (ballFr * wo.cf + curFr * voCf);
			// wo.cf = (int) (wo.cf * 0.8 + voCf * 0.2);

			wo.dist = (int) dist;
			wo.heading = head;
			wo.lasttime = frame.getTime();
		} else {
			// 入力なし
			wo.distFilter.eval();
			wo.headingFilter.eval();

			// 信頼度を下げておく
			wo.cf *= wo.dist > 500 ? 0.85f : 0.95f;

			// wo.cf *= 0.7;
			// wo.cf *= 0.99;
			// wmballのcfがゼロでなければ
			// 自己位置の修正を考慮してボール位置を再計算
			if (wo.cf > 0) {
				float rad = MathUtils.toRadians(woSelf.worldYaw + wo.heading);
				wo.world.x = woSelf.world.x
						+ (int) (wo.dist * MathUtils.sin(rad));
				wo.world.y = woSelf.world.y
						+ (int) (wo.dist * MathUtils.cos(rad));
				// Log::info(LOG_GPS,"GPS: voCf = 0, recalculate wmball pos
				// (%lf, %lf)",
				// (double)wo.ax, (double)wo.ay);

				// 得られた ball の x, y を元に角度と距離を計算する
				dist = (int) MathUtils.distance(wo.world, woSelf.world);
				if (dist == 0)
					return;

				// caculate ball's info relative to robot's position
				// double angle = RAD2DEG(asin(abs(ballY-ary)/dist));
				float angle = MathUtils.toDegrees(MathUtils.atan2(wo.world.x
						- woSelf.world.x, wo.world.y - woSelf.world.y));

				wo.dist = (int) dist;
				wo.heading = normalizeAngle180(angle - woSelf.worldYaw);
			}
		}

		wo.worldAngle = MathUtils.toDegrees(MathUtils.atan2(wo.world.x,
				wo.world.y));
	}

	@Override
	public synchronized void updateOdometry(float forward, float left,
			float turnCCW) {
		odometryLeft += MathUtils.sin(odometryTurn) * forward
				+ MathUtils.cos(odometryTurn) * left;
		odometryForward += MathUtils.cos(odometryTurn) * forward
				- MathUtils.sin(odometryTurn) * left;
		odometryTurn = normalizeAnglePI(odometryTurn + turnCCW);
	}

	public void updatePosture() {
		self.updatePosture();
	}

	public void startMotion(Motion motion) {
		self.startMotion(motion);
	}

	public void stopMotion(Motion motion) {
		self.stopMotion(motion);
	}

	/**
	 * オドメトリの更新2
	 */
	private void updateOdometryBatch() {
		float forward;
		float left;
		float turnCCW;

		synchronized (this) {
			forward = odometryForward;
			left = odometryLeft;
			turnCCW = odometryTurn;
			odometryForward = 0;
			odometryLeft = 0;
			odometryTurn = 0;
		}

		if (log.isTraceEnabled()) {
			NumberFormat f = NumberFormat.getNumberInstance();
			f.setMaximumFractionDigits(2);
			log.trace("updateOdometry " + f.format(forward) + ", "
					+ f.format(left) + ", " + f.format(turnCCW));
		}

		self.updateOdometry(forward, left, turnCCW);
		// FIXME worldObjectsも更新する.
		float dx = forward + left + 10 * turnCCW;
		if (dx > 100) {
			worldObjects.get(WorldObjects.Ball).cf *= 0.8;
		}
	}

	/**
	 * @return the selfLocalization
	 */
	public SelfLocalization getSelf() {
		return self;
	}

	private void copyWorldToTeamCoord(WorldObject wo, boolean isRed) {
		if (isRed) {
			wo.teamAngle = wo.worldAngle;
			wo.teamYaw = wo.worldYaw;
			wo.team.x = wo.world.x;
			wo.team.y = wo.world.y;
		} else {
			wo.teamAngle = normalizeAngle180(wo.worldAngle - 180);
			wo.teamYaw = normalizeAngle180(wo.worldYaw - 180);
			wo.team.x = -wo.world.x;
			wo.team.y = -wo.world.y;
		}
	}
}
