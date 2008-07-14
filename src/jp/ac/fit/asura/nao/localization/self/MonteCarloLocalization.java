/*
 * 作成日: 2008/06/23
 */
package jp.ac.fit.asura.nao.localization.self;

import static jp.ac.fit.asura.nao.misc.MathUtils.clipping;
import static jp.ac.fit.asura.nao.misc.MathUtils.gaussian;
import static jp.ac.fit.asura.nao.misc.MathUtils.normalizeAngle180;
import static jp.ac.fit.asura.nao.misc.MathUtils.rand;
import static jp.ac.fit.asura.nao.misc.MathUtils.square;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Map;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.misc.PhysicalConstants;
import jp.ac.fit.asura.nao.misc.PhysicalConstants.Field;
import jp.ac.fit.asura.nao.misc.PhysicalConstants.Goal;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualObjects;
import jp.ac.fit.asura.nao.vision.VisualObjects.Properties;
import jp.ac.fit.asura.nao.vision.objects.GoalVisualObject;
import jp.ac.fit.asura.nao.vision.objects.VisualObject;

import org.apache.log4j.Logger;

/**
 * モンテカルロ法を使った自己位置同定.
 * 
 * いじるパラメータ
 * 
 * -standardWeight: 候補点の重みがどれくらいでリサンプルの対象にするかとか
 * 
 * -HisteresisSensorResettingsのなかとbeta: 重みの変動がどれくらいでリセットするかとか
 * 
 * -dDistの分母: 候補点の距離の違いへの感度
 * 
 * -dHeadの分母: 候補点の角度の違いへの感度
 * 
 * -gaussian関連: リサンプルした候補点へのノイズ量
 * 
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class MonteCarloLocalization extends SelfLocalization {
	private Logger log = Logger.getLogger(MonteCarloLocalization.class);

	class HisteresisSensorResettings {
		static final double longEta = 0.1;
		static final double shortEta = 0.9;
		double beta;
		double longAlpha;
		double shortAlpha;
		double threshold;

		HisteresisSensorResettings() {
			beta = 0.0;
			longAlpha = 1.0;
			shortAlpha = 1.0;
			threshold = 0.001;
		}

		double getBeta() {
			return beta;
		}

		void update(double alpha) {
			longAlpha += longEta * (alpha - longAlpha);
			shortAlpha += shortEta * (alpha - shortAlpha);
			assert longAlpha * threshold != 0;
			beta = 1.0 - shortAlpha / (longAlpha * threshold);
		}

		void reset() {
			longAlpha = 1.0;
			shortAlpha = 1.0;
		}

		double getLongAlpha() {
			return longAlpha;
		}

	}

	private static class Position extends Point {
		float h;

		public void clear() {
			x = y = 0;
			h = 0.0f;
		}
	}

	private static class Candidate extends Position {
		double w;
	}

	private HisteresisSensorResettings resettings;
	private double standardWeight;

	private Sensor sensor;
	private RobotContext robotContext;
	private Candidate[] candidates;

	private Position position;
	private Position variance;
	private int confidence;

	public MonteCarloLocalization() {
		resettings = new HisteresisSensorResettings();
		candidates = new Candidate[256];
		for (int i = 0; i < candidates.length; i++)
			candidates[i] = new Candidate();
		position = new Position();
		variance = new Position();
		standardWeight = (1.0 / candidates.length * 1e-6);
	}

	public void init(RobotContext rctx) {
		robotContext = rctx;
		sensor = rctx.getSensor();
	}

	public void start() {
		reset();
	}

	public void step() {
	}

	public void stop() {
	}

	public void reset() {
		resettings.reset();
		randomSampling();
	}

	public void updateVision(VisualContext context) {
		int resampled = 0;
		Map<VisualObjects, VisualObject> vobj = context.objects;
		VisualObject bg = vobj.get(VisualObjects.BlueGoal);
		VisualObject yg = vobj.get(VisualObjects.YellowGoal);
		if (localizeByGoal((GoalVisualObject) yg)
				|| localizeByGoal((GoalVisualObject) bg)) {
			fieldClipping();
			resampled = resampleCandidates();
		}

		estimateCurrentPosition();

		if (robotContext.getFrame() % 100 == 0) {
			log.debug(String.format(
					"MCL: current position x:%d y:%d h:%f, cf:%d", position.x,
					position.y, position.h, confidence));
		}
		if (resampled > 0)
			log.debug("MCL:  resample " + resampled);
	}

	public void updateOdometry(int forward, int left, float turnCCW) {
		for (Candidate c : candidates) {
			c.x += Math.cos(Math.toRadians(c.h)) * forward
					- Math.sin(Math.toRadians(c.h)) * left;
			c.y += Math.sin(Math.toRadians(c.h)) * forward
					+ Math.cos(Math.toRadians(c.h)) * left;
			c.h = normalizeAngle180(c.h + turnCCW);
		}
		position.x += Math.cos(Math.toRadians(position.h)) * forward
				- Math.sin(Math.toRadians(position.h)) * left;
		position.y += Math.sin(Math.toRadians(position.h)) * forward
				+ Math.cos(Math.toRadians(position.h)) * left;
		position.h = normalizeAngle180(position.h + turnCCW);
	}

	public int getConfidence() {
		return confidence;
	}

	public float getHeading() {
		return position.h;
	}

	public int getX() {
		return position.x;
	}

	public int getY() {
		return position.y;
	}

	/**
	 * ビーコンベースで候補点の確からしさを求め、 Weightを更新する。
	 */
	private boolean localizeByGoal(GoalVisualObject vo) {
		// 候補点の位置がどの程度正しいのかをチェックする。
		// 正しければ、weightを上げる。
		// 正しくなければ、weightを下げる。
		// beaconとの位置関係によって正しさを求める。

		// 前判定
		if (vo.getInt(Properties.Confidence) < 200)
			return false;
		boolean useDist = vo.getBoolean(Properties.DistanceUsable);
		int voDist = useDist ? vo.getInt(Properties.Distance) : -1;
		Point2D angle = vo.get(Point2D.class, Properties.Angle);
		float voHead = (float) Math.toDegrees(angle.getX()
				+ (double) sensor.getJoint(Joint.HeadYaw));

		int goalX = vo.getType() == VisualObjects.YellowGoal ? Goal.YellowGoalX
				: Goal.BlueGoalX;
		int goalY = vo.getType() == VisualObjects.YellowGoal ? Goal.YellowGoalY
				: Goal.BlueGoalY;
		double alpha = 0.0;

		Point goal = new Point(goalX, goalY);
		Point leftPost = new Point(goalX - Goal.HalfWidth + Goal.PoleRadius,
				goalY);
		Point rightPost = new Point(goalX + Goal.HalfWidth - Goal.PoleRadius,
				goalY);

		Point[] beacons;
		if (vo.getBoolean(Properties.IsLeftPost)) {
			if (vo.getBoolean(Properties.IsRightPost))
				beacons = new Point[] { leftPost, rightPost };
			else
				beacons = new Point[] { leftPost };
		} else if (vo.getBoolean(Properties.IsRightPost))
			beacons = new Point[] { rightPost };
		else
			beacons = new Point[] { goal };

		for (Candidate c : candidates) {
			for (Point beacon : beacons) {
				int dx = beacon.x - c.x; // ビーコンとの距離
				int dy = beacon.y - c.y;

				double dDist = useDist ? square(Math.sqrt(square(dx)
						+ square(dy))
						- voDist) : 0;
				assert !Double.isNaN(dDist) && !Double.isInfinite(dDist);

				double theta = Math.atan2(dy, dx);
				double dHead = square(normalizeAngle180(c.h + voHead
						- (float) Math.toDegrees(theta)));

				// dDist *= 0.5;

				double a = Math.abs(dDist) / (2.0 * square(512));
				double b = Math.abs(dHead) / (2.0 * square(24));
				c.w *= Math.max(Math.exp(-(a + b)), 1e-9);
			}
			alpha += c.w;
			assert !Double.isNaN(c.w) && !Double.isInfinite(c.w);
		}
		resettings.update(alpha);

		if (alpha == 0.0) {
			randomSampling();
			log.error(" warning alpha is zero");
			assert false;
			return false;
		}
		assert alpha != 0.0 && !Double.isInfinite(alpha)
				&& !Double.isNaN(alpha);

		// Σc.w = 1になるよう正規化
		for (Candidate c : candidates) {
			c.w /= alpha;
		}

		// standardWeight = (1 / candidates.length * 1e-6);
		return true;
	}

	private int fieldClipping() {
		int count = 0;
		for (Candidate c : candidates) {
			int dx = Math.max(c.x - Field.MaxX, 0)
					- Math.min(c.x - Field.MinX, 0);
			int dy = Math.max(c.y - Field.MaxY, 0)
					- Math.min(c.y - Field.MinY, 0);
			if (dx + dy > 0) {
				count++;
				c.w *= Math
						.max(Math.exp(-(dx + dy) / (2.0 * square(20))), 1e-4);
			}
		}
		return count;
	}

	private int resampleCandidates() {
		// この識別境界面の決定は見直す必要がある
		// 平均のwよりもとっても低いやつはリサンプル
		// このへんのメモリの使い方は最適化できそう
		double beta = resettings.getBeta();

		// Q: 汚染されてる状況ならリセットすべきか

		// ASSERT(finite(beta));
		if (beta > 0.8) {
			log.debug("beta " + beta);
			// なんかおかしいのでリセットする
			// Log::info(LOG_GPS, "MCLocalization: I've been kidnapped!
			// beta:%f", beta
			// );
			// resetしたらもう一度beacon評価やり直したほうがいいんじゃね?
			if (beta > 0.9) {
				// SR
				randomSampling();
			} else {
				randomSampling();
				// ER
				// expandSampling(beta * 4);
			}

			resettings.reset();
			return (int) (candidates.length * beta);
		}

		Candidate[] new_c = new Candidate[candidates.length];
		double score[] = new double[candidates.length];
		int resamples = 0; // リサンプルする候補数
		for (int i = 0; i < candidates.length; i++) {
			if (candidates[i].w > standardWeight)
				continue;
			resamples++;
			while (true) {
				int r = rand(0, candidates.length);
				score[r] += candidates[r].w;
				assert !Double.isNaN(score[r]);
				assert !Double.isInfinite(score[r]);

				if (score[r] >= standardWeight) {
					score[r] -= standardWeight;

					float dh = clipping((float) gaussian(0.0, 17.0), -180.0f,
							179.0f);
					int dx = (int) (clipping(gaussian(0.0, 200.0),
							PhysicalConstants.Field.MinX,
							PhysicalConstants.Field.MaxX));
					int dy = (int) (clipping(gaussian(0.0, 200.0),
							PhysicalConstants.Field.MinY,
							PhysicalConstants.Field.MaxY));
					new_c[i] = new Candidate();
					new_c[i].x = candidates[r].x + dx;
					new_c[i].y = candidates[r].y + dy;
					new_c[i].h = normalizeAngle180(candidates[r].h + dh);
					new_c[i].w = candidates[r].w
							* Math
									.exp(-(square(dx / 10) + square(dy / 10) + square(dh))
											/ (2 * square(32.0)));
					break;
				}
			}
		}
		// 重みが低いのはリサンプル
		double cfSum = 0.0, wsum = 0.0;
		for (int i = 0; i < candidates.length; i++) {
			if (candidates[i].w < standardWeight) {
				candidates[i] = new_c[i];
			}
			cfSum += candidates[i].w;
		}

		if (cfSum < 1.0e-100) {
			// cfSumがあまりにも低い>とりあえずリセット
			// Log::error("MCLocalization: cfsum eq %.20f", cfSum);
			randomSampling();
			return 0;
		}
		for (int i = 0; i < candidates.length; i++) {
			candidates[i].w /= cfSum;
			wsum += candidates[i].w;
		}

		// Log::debug(LOG_GPS, "MCLocalization: candidate %d/%d:resampled.",
		// resamples,
		// numberOfCandidates);
		return resamples;
	}

	private void randomSampling() {
		log.info("randomSampling();");
		for (Candidate c : candidates) {
			// maxExclusiveだけどいいか.
			c.x = rand(PhysicalConstants.Field.MinX,
					PhysicalConstants.Field.MaxX);
			c.y = rand(PhysicalConstants.Field.MinY,
					PhysicalConstants.Field.MaxY);
			c.h = rand(-180, 180);
			c.w = 1.0 / candidates.length;
		}
	}

	private void estimateCurrentPosition() {
		calculatePosition();
		calculateVariance();
		float d = (float) Math.sqrt(variance.x + variance.y + 50 * variance.h);
		// log.debug("MCL: var:" + d);
		float f = clipping(1000 - d / 10, 0f, 1000f);
		confidence = (int) (confidence * 0.6f + f * 0.4f);
	}

	private void calculatePosition() {
		Position s = new Position();
		Candidate base = candidates[0];

		for (Candidate c : candidates) {
			s.x += c.x;
			s.y += c.y;
			if (base.w < c.w) {
				base = c;
			}
		}

		// wのもっとも高いものを基準に角度の平均をとる
		for (Candidate c : candidates) {
			s.h += normalizeAngle180(c.h - base.h);
		}
		position.x = s.x / candidates.length;
		position.y = s.y / candidates.length;
		position.h = normalizeAngle180(s.h / candidates.length + base.h);
		assert 10 * 1000 > Math.abs(position.x);
		assert 10 * 1000 > Math.abs(position.y);
		assert 180 >= Math.abs(position.h);
	}

	private void calculateVariance() {
		Position s = new Position();
		for (Candidate c : candidates) {
			s.x += square(c.x - position.x);
			s.y += square(c.y - position.y);
			s.h += square(c.h - position.h);
		}

		// 角度の分散ってどうしよう?
		variance.x = s.x / candidates.length;
		variance.y = s.y / candidates.length;
		variance.h = s.h / candidates.length;

		assert variance.x >= 0 : variance.x;
		assert variance.y >= 0 : variance.y;
		assert variance.h >= 0 : variance.h;

		variance.y = Math.max(variance.y, 0);
		variance.x = Math.max(variance.x, 0);
		variance.h = Math.max(variance.h, 0);
	}
}
