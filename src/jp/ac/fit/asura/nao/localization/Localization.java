/*
 * 作成日: 2008/04/24
 */
package jp.ac.fit.asura.nao.localization;

import static jp.ac.fit.asura.nao.misc.MathUtils.normalizeAngle180;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.event.MotionEventListener;
import jp.ac.fit.asura.nao.localization.self.GPSLocalization;
import jp.ac.fit.asura.nao.localization.self.MonteCarloLocalization;
import jp.ac.fit.asura.nao.localization.self.SelfLocalization;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.strategy.Team;
import jp.ac.fit.asura.nao.vision.VisualCortex;
import jp.ac.fit.asura.nao.vision.VisualObjects;
import jp.ac.fit.asura.nao.vision.VisualObjects.Properties;
import jp.ac.fit.asura.nao.vision.objects.VisualObject;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class Localization implements RobotLifecycle, MotionEventListener {
	private Logger log = Logger.getLogger(Localization.class);

	private SelfLocalization self;

	private Map<WorldObjects, WorldObject> worldObjects;
	private WorldObject woSelf;

	private RobotContext context;

	private int mKeepBallMaxDist = 600;

	public Localization() {
		worldObjects = new HashMap<WorldObjects, WorldObject>();
		worldObjects.put(WorldObjects.Ball, new WorldObject());
		worldObjects.put(WorldObjects.Self, new WorldObject());
		worldObjects.put(WorldObjects.BlueGoal, new WorldObject());
		worldObjects.put(WorldObjects.YellowGoal, new WorldObject());
		self = new MonteCarloLocalization();
		// self = new GPSLocalization();
		woSelf = worldObjects.get(WorldObjects.Self);
	}

	public void init(RobotContext rctx) {
		this.context = rctx;
		context.getMotor().addEventListener(this);
		self.init(rctx);
	}

	public void start() {
		self.start();
	}

	public void step() {
		VisualCortex vc = context.getVision();

		vc.get(VisualObjects.BlueGoal);
		vc.get(VisualObjects.YellowGoal);

		self.step();

		mapSelf();
		mapBall();

		boolean isRed = context.getStrategy().getTeam() == Team.Red;
		for (WorldObject wo : worldObjects.values())
			copyWorldToTeamCoord(wo, isRed);
	}

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
		wo.worldYaw = self.getHeading();
		wo.worldAngle = (float) Math.toDegrees(Math.atan2(wo.world.y,
				wo.world.x));
		wo.cf = self.getConfidence();
		wo.dist = 0;
		wo.heading = 0;
	}

	private void mapBall() {
		VisualCortex vc = context.getVision();
		VisualObject vo = vc.get(VisualObjects.Ball);

		WorldObject wo = worldObjects.get(WorldObjects.Ball);
		wo.setVision(vo);
		int voCf = vo.getInt(Properties.Confidence);
		// find ball coordinate
		// WMObject を更新
		// ボールが見えていれば
		if (voCf > 0) {
			int voDist = vo.getInt(Properties.Distance);
			Point2D angle = vo.get(Point2D.class, Properties.Angle);
			float voHead = (float) Math.toDegrees(angle.getX()
					+ context.getSensor().getJoint(Joint.HeadYaw));
			float woHead = woSelf.worldYaw + voHead;
			double rad = Math.toRadians(woHead);

			// quick hack
			rad = Math.toRadians(woSelf.worldYaw)
					+ vo.get(Float.class, Properties.RobotAngle);

			double bx = (self.getX() + voDist * Math.cos(rad));
			double by = (self.getY() + voDist * Math.sin(rad));
			double bcf = voCf;
			double rate = (wo.cf >= 600) ? 0.4 : 1.0;
			double curFr = bcf * rate / (bcf + wo.cf);
			double ballFr = 1 - curFr;
			wo.world.x = (int) (ballFr * wo.world.x + curFr * bx);
			wo.world.y = (int) (ballFr * wo.world.y + curFr * by);
			wo.cf = (int) (ballFr * wo.cf + curFr * bcf);
			// wo.cf = (int) (wo.cf * 0.8 + voCf * 0.2);
		} else {
			// 信頼度を下げておく
			wo.cf *= 0.9;
			// wo.cf *= 0.7;
			// wo.cf *= 0.99;
			// wmballのcfがゼロでなければ
			// 自己位置の修正を考慮してボール位置を再計算
			if (wo.cf > 0 && wo.dist < mKeepBallMaxDist) {
				double rad = Math.toRadians(woSelf.worldYaw + wo.heading);

				wo.world.x = (int) (woSelf.world.x + wo.dist * Math.cos(rad));
				wo.world.y = (int) (woSelf.world.y + wo.dist * Math.sin(rad));
				// Log::info(LOG_GPS,"GPS: voCf = 0, recalculate wmball pos
				// (%lf, %lf)",
				// (double)wo.ax, (double)wo.ay);
			}
		}

		// 得られた ball の x, y を元に角度と距離を計算する

		double dist = wo.world.distance(woSelf.world);

		// double に対して == 0 の演算は意味を持たない
		if (dist == 0)
			return;

		// caculate ball's info relative to robot's position
		// double angle = RAD2DEG(asin(abs(ballY-ary)/dist));
		double angle = Math.toDegrees(Math.atan2(wo.world.y - woSelf.world.y,
				wo.world.x - woSelf.world.x));

		wo.dist = (int) dist;
		wo.heading = normalizeAngle180((float) angle - self.getHeading());

		wo.worldAngle = (float) Math.toDegrees(Math.atan2(wo.world.y,
				wo.world.x));
	}

	public void updateOdometry(int forward, int left, float turnCCW) {
	}

	public void updatePosture() {
	}

	public void startMotion(Motion motion) {
	}

	public void stopMotion(Motion motion) {
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
