/*
 * 作成日: 2008/06/23
 */
package jp.ac.fit.asura.nao.localization.self;

import static jp.ac.fit.asura.nao.misc.MathUtils.clipping;
import static jp.ac.fit.asura.nao.misc.MathUtils.gaussian;
import static jp.ac.fit.asura.nao.misc.MathUtils.normalizeAnglePI;
import static jp.ac.fit.asura.nao.misc.MathUtils.rand;
import static jp.ac.fit.asura.nao.misc.MathUtils.square;

import java.awt.Point;

import javax.vecmath.Point2f;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.VisualFrameContext;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.physical.Field;
import jp.ac.fit.asura.nao.physical.Goal;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualObjects;
import jp.ac.fit.asura.nao.vision.perception.GoalVisualObject;
import jp.ac.fit.asura.nao.vision.perception.VisualObject;

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
 * @version $Id: MonteCarloLocalization.java 717 2008-12-31 18:16:20Z sey $
 *
 */
public class MonteCarloLocalization extends SelfLocalization {
	private Logger log = Logger.getLogger(MonteCarloLocalization.class);

	class HisteresisSensorResettings {
		static final float longEta = 0.125f;
		static final float shortEta = 0.9f;
		float beta;
		float longAlpha;
		float shortAlpha;
		float threshold;

		HisteresisSensorResettings() {
			beta = 0.0f;
			longAlpha = 1.0f;
			shortAlpha = 1.0f;
			threshold = 0.001f;
		}

		float getBeta() {
			return beta;
		}

		void update(double alpha) {
			longAlpha += longEta * (alpha - longAlpha);
			shortAlpha += shortEta * (alpha - shortAlpha);
			assert longAlpha * threshold != 0;
			beta = 1.0f - shortAlpha / (longAlpha * threshold);
		}

		void reset() {
			longAlpha = 1.0f;
			shortAlpha = 1.0f;
		}

		float getLongAlpha() {
			return longAlpha;
		}

	}

	public static class Position {
		public int x;
		public int y;
		public float h;

		public void clear() {
			x = y = 0;
			h = 0.0f;
		}
	}

	public static class Candidate extends Position {
		public float w;
	}

	private HisteresisSensorResettings resettings;
	private float standardWeight;

	private Candidate[] candidates;

	private Position position;
	private Position variance;
	private int confidence;

	public MonteCarloLocalization() {
		resettings = new HisteresisSensorResettings();
		candidates = new Candidate[128];
		for (int i = 0; i < candidates.length; i++)
			candidates[i] = new Candidate();
		position = new Position();
		variance = new Position();
		standardWeight = (1e-6f / candidates.length);
	}

	@Override
	public void init(RobotContext rctx) {
	}

	@Override
	public void start() {
		reset();
	}

	@Override
	public void step(VisualFrameContext context) {
	}

	@Override
	public void stop() {
	}

	public void reset() {
		resettings.reset();
		randomSampling();
	}

	public void updateVision(VisualContext context) {
		int resampled = 0;
		VisualObject bg = context.get(VisualObjects.BlueGoal);
		VisualObject yg = context.get(VisualObjects.YellowGoal);
		if (localizeByGoal((GoalVisualObject) yg)
				|| localizeByGoal((GoalVisualObject) bg)) {
			fieldClipping();
			resampled = resampleCandidates();
		}

		estimateCurrentPosition();

		if (log.isDebugEnabled()) {
			if (context.getFrameContext().getFrame() % 25 == 0) {
				log.debug(String.format(
						"MCL: current position x:%d y:%d h:%f, cf:%d",
						position.x, position.y, position.h, confidence));
			}
		}
		if (resampled > 0)
			log.debug("MCL:  resample " + resampled);
	}

	public void updateOdometry(int forward, int left, float turnCCW) {
		assert Math.abs(forward) < 1e4 : forward;
		assert Math.abs(left) < 1e4 : left;
		assert Math.abs(turnCCW) < 1e4 : turnCCW;

		turnCCW = MathUtils.toRadians(turnCCW);

		for (Candidate c : candidates) {
			c.x += Math.cos(c.h) * forward - Math.sin(c.h) * left;
			c.y += Math.sin(c.h) * forward + Math.cos(c.h) * left;
			c.h = normalizeAnglePI(c.h + turnCCW);
		}
		position.x += Math.cos(position.h) * forward - Math.sin(position.h)
				* left;
		position.y += Math.sin(position.h) * forward + Math.cos(position.h)
				* left;
		position.h = normalizeAnglePI(position.h + turnCCW);
	}

	public int getConfidence() {
		return confidence;
	}

	public float getHeading() {
		return MathUtils.toDegrees(position.h);
	}

	public int getX() {
		return position.x;
	}

	public int getY() {
		return position.y;
	}

	public Candidate[] getCandidates() {
		return candidates;
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
		if (vo.confidence < 200)
			return false;
		boolean useDist = vo.distanceUsable;
		int voDist = useDist ? vo.distance : -1;
		Point2f angle = vo.angle;
		Point2f robotAngle = vo.robotAngle;

		int goalX = vo.getType() == VisualObjects.YellowGoal ? Goal.YellowGoalX
				: Goal.BlueGoalX;
		int goalY = vo.getType() == VisualObjects.YellowGoal ? Goal.YellowGoalY
				: Goal.BlueGoalY;
		float alpha = 0.0f;

		Point goal = new Point(goalX, goalY);
		Point leftPost = new Point(goalX - Goal.HalfWidth + Goal.PoleRadius,
				goalY);
		Point rightPost = new Point(goalX + Goal.HalfWidth - Goal.PoleRadius,
				goalY);

		Point[] beacons;
		if (vo.isLeftPost) {
			if (vo.isRightPost)
				beacons = new Point[] { leftPost, rightPost };
			else
				beacons = new Point[] { leftPost };
		} else if (vo.isRightPost)
			beacons = new Point[] { rightPost };
		else
			beacons = new Point[] { goal };

		for (Candidate c : candidates) {
			for (Point beacon : beacons) {
				int dx = beacon.x - c.x; // ビーコンとの距離
				int dy = beacon.y - c.y;

				float dDist = useDist ? square((float) Math.sqrt(square(dx)
						+ square(dy))
						- voDist) : 0;
				assert !Float.isNaN(dDist) && !Float.isInfinite(dDist);

				float theta = (float) Math.atan2(dy, dx);
				float dHead = square(normalizeAnglePI(c.h + robotAngle.x
						- theta));

				// dDist *= 0.5;

				float a = dDist / (2.0f * square(512));
				float b = dHead / 0.125f;
				c.w *= Math.max((float) Math.exp(-(a + b)), 1e-6f);
			}
			alpha += c.w;
			assert !Float.isNaN(c.w) && !Float.isInfinite(c.w);
		}
		resettings.update(alpha);

		if (alpha == 0.0f) {
			randomSampling();
			log.error(" warning alpha is zero");
			assert false;
			return false;
		}
		assert alpha != 0.0f && !Float.isInfinite(alpha) && !Float.isNaN(alpha);

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
		float beta = resettings.getBeta();

		// Q: 汚染されてる状況ならリセットすべきか

		// ASSERT(finite(beta));
		if (beta > 0.8f) {
			log.debug("beta " + beta);
			// なんかおかしいのでリセットする
			// Log::info(LOG_GPS, "MCLocalization: I've been kidnapped!
			// beta:%f", beta
			// );
			// resetしたらもう一度beacon評価やり直したほうがいいんじゃね?
			if (beta > 0.9f) {
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
		float score[] = new float[candidates.length];
		int resamples = 0; // リサンプルする候補数
		for (int i = 0; i < candidates.length; i++) {
			if (candidates[i].w > standardWeight)
				continue;
			resamples++;
			for (int j = 0; j < Integer.MAX_VALUE; j++) {
				if (log.isTraceEnabled() && j % 100 == 0)
					log.trace("" + j);
				int r = rand(0, candidates.length);
				score[r] += candidates[r].w;
				assert !Double.isNaN(score[r]);
				assert !Double.isInfinite(score[r]);

				if (score[r] >= standardWeight) {
					score[r] -= standardWeight;

					float dh = clipping((float) gaussian(0.0, 0.125f),
							-MathUtils.PIf, MathUtils.PIf);
					int dx = (int) (clipping(gaussian(0.0, 200.0), Field.MinX,
							Field.MaxX));
					int dy = (int) (clipping(gaussian(0.0, 200.0), Field.MinY,
							Field.MaxY));
					new_c[i] = new Candidate();
					new_c[i].x = candidates[r].x + dx;
					new_c[i].y = candidates[r].y + dy;
					new_c[i].h = normalizeAnglePI(candidates[r].h + dh);
					assert Math.abs(new_c[i].x) < 1e4 : new_c[i].x;
					assert Math.abs(new_c[i].y) < 1e4 : new_c[i].y;
					assert Math.abs(new_c[i].h) < 1e1 : new_c[i].h;
					new_c[i].w = (float) (candidates[r].w * Math
							.exp(-(square(dx / 10) + square(dy / 10) + square(dh))
									/ (2 * square(32.0f))));
					break;
				}
			}
		}
		// 重みが低いのはリサンプル
		float cfSum = 0.0f, wsum = 0.0f;
		for (int i = 0; i < candidates.length; i++) {
			if (candidates[i].w < standardWeight) {
				candidates[i] = new_c[i];
			}
			cfSum += candidates[i].w;
		}

		if (cfSum < 1.0e-9f) {
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
			c.x = rand(Field.MinX, Field.MaxX);
			c.y = rand(Field.MinY, Field.MaxY);
			c.h = rand(-MathUtils.PIf, MathUtils.PIf);
			assert Math.abs(c.x) < 1e4 : c.x;
			assert Math.abs(c.y) < 1e4 : c.y;
			assert Math.abs(c.h) < 1e2 : c.h;
			c.w = 1.0f / candidates.length;
		}
	}

	private void estimateCurrentPosition() {
		calculatePosition();
		calculateVariance();
		float d = (float) Math.sqrt(variance.x + variance.y + 50 * 180
				/ MathUtils.PIf * variance.h);
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
			s.h += normalizeAnglePI(c.h - base.h);
		}
		position.x = s.x / candidates.length;
		position.y = s.y / candidates.length;
		position.h = normalizeAnglePI(s.h / candidates.length + base.h);
		assert Math.abs(position.x) < 1e5;
		assert Math.abs(position.y) < 1e5;
	}

	private void calculateVariance() {
		Position s = new Position();
		for (Candidate c : candidates) {
			assert s.x < Integer.MAX_VALUE / 2 : s.x;
			assert s.y < Integer.MAX_VALUE / 2 : s.y;
			assert s.h < Integer.MAX_VALUE / 2 : s.h;
			assert c.x < 1e4 : c.x;
			assert c.y < 1e4 : c.y;
			assert c.h < 1e2 : c.h;
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
