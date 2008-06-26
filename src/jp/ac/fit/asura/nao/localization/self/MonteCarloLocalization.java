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
import jp.ac.fit.asura.nao.event.MotionEventListener;
import jp.ac.fit.asura.nao.localization.Localization;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.misc.PhysicalConstants;
import jp.ac.fit.asura.nao.misc.PhysicalConstants.Goal;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.vision.VisualCortex;
import jp.ac.fit.asura.nao.vision.VisualObjects;
import jp.ac.fit.asura.nao.vision.VisualObjects.Properties;
import jp.ac.fit.asura.nao.vision.objects.GoalVisualObject;
import jp.ac.fit.asura.nao.vision.objects.VisualObject;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class MonteCarloLocalization extends SelfLocalization implements
		MotionEventListener {

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
	private Localization localization;
	private VisualCortex vision;
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
		localization = rctx.getLocalization();
		vision = rctx.getVision();
		sensor = rctx.getSensor();
		rctx.getMotor().addEventListener(this);
	}

	public void start() {
		reset();
	}

	public void step() {
		int resampled = 0;
		Map<VisualObjects, VisualObject> vobj = vision.getVisualContext().objects;
		VisualObject bg = vobj.get(VisualObjects.BlueGoal);
		VisualObject yg = vobj.get(VisualObjects.YellowGoal);
		if (localizeByGoal((GoalVisualObject) yg)
				|| localizeByGoal((GoalVisualObject) bg)) {
			resampled = resampleCandidates();
		}

		estimateCurrentPosition();

//		if (MathUtils.rand(0, 20) == 0) {
//			System.out.print(String.format(
//					"MCL: current position x:%d y:%d h:%f, cf:%d\n",
//					position.x, position.y, position.h, confidence));
//			System.out.println("MCL:  resample " + resampled);
//		}
	}

	public void stop() {
	}

	public void reset() {
		resettings.reset();
		randomSampling();
	}

	public void updateOdometry(int forward, int left, float turnCCW) {
		for (Candidate c : candidates) {
			c.x += left;
			c.y += forward;
			c.h = normalizeAngle180(c.h + turnCCW);
		}
		position.x += left;
		position.y += forward;
		position.h = normalizeAngle180(position.h + turnCCW);
	}

	public void updatePosture() {
	}

	public void startMotion(Motion motion) {
	}

	public void stopMotion(Motion motion) {
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

		for (Candidate c : candidates) {
			int dx = goalX - c.x; // ビーコンとの距離
			int dy = goalY - c.y;

			double dDist = useDist ? square(Math.sqrt(square(dx) + square(dy))
					- voDist) : 0;
			assert !Double.isNaN(dDist) && !Double.isInfinite(dDist);

			double theta = Math.atan2(dy, dx);
			double dHead = square(normalizeAngle180(c.h + voHead
					- (float) Math.toDegrees(theta)));

			// dDist *= 0.5;

			double a = Math.abs(dDist) / (2.0 * square(512));
			double b = Math.abs(dHead) / (2.0 * square(24));
			c.w *= Math.exp(-(a + b));
			alpha += c.w;
			assert !Double.isNaN(c.w) && !Double.isInfinite(c.w);
		}
		resettings.update(alpha);

		if (alpha == 0.0) {
			randomSampling();
			System.out.println("MCL: warning alpha is zero");
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

	private int resampleCandidates() {
		// この識別境界面の決定は見直す必要がある
		// 平均のwよりもとっても低いやつはリサンプル
		// このへんのメモリの使い方は最適化できそう
		double beta = resettings.getBeta();

		// Q: 汚染されてる状況ならリセットすべきか

		// ASSERT(finite(beta));
		if (beta > 0.5) {
			System.out.println("MCL:beta " + beta);
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

					float dh = clipping((float) gaussian(0.0, 18.0), -180.0f,
							179.0f);
					int dx = (int) (clipping(gaussian(0.0, 250.0),
							PhysicalConstants.Field.MinX,
							PhysicalConstants.Field.MaxX));
					int dy = (int) (clipping(gaussian(0.0, 250.0),
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
		System.out.println("MCL:randomSampling();");
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
		// System.out.println("MCL: var:" + d);
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

		assert variance.x >= 0;
		assert variance.y >= 0;
		assert variance.h >= 0;
	}
}
