/*
 * 作成日: 2008/11/23
 */
package jp.ac.fit.asura.naimon;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.localization.Localization;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.localization.WorldObjects;
import jp.ac.fit.asura.nao.localization.self.GPSLocalization;
import jp.ac.fit.asura.nao.localization.self.MonteCarloLocalization;
import jp.ac.fit.asura.nao.localization.self.SelfLocalization;
import jp.ac.fit.asura.nao.localization.self.MonteCarloLocalization.Candidate;
import jp.ac.fit.asura.nao.physical.Field;

/**
 * @author sey
 * 
 * @version $Id: $
 * 
 */
class FieldPanel extends JPanel {
	private Localization lc;
	private GPSLocalization gps;

	public FieldPanel(RobotContext context) {
		lc = context.getLocalization();
		gps = new GPSLocalization();
		gps.init(context);
		setPreferredSize(new Dimension((Field.MaxX - Field.MinX) / 10,
				(Field.MaxY - Field.MinY) / 10));
	}

	protected void paintComponent(Graphics g) {
		int width = getWidth();
		int height = getHeight();

		g.setColor(Color.green);
		g.fillRect(0, 0, width, height);

		g.setColor(Color.white);

		// フィールド中央の横線
		g.drawLine(20, height / 2, width - 20, height / 2);

		// ブルーゴール横線
		g.drawLine(20, 30, width - 20, 30);

		// イエローゴール横線
		g.drawLine(20, 570, width - 20, 570);

		// 左縦線
		g.drawLine(20, 30, 20, height - 30);

		// 右縦線
		g.drawLine(380, 30, 380, height - 30);

		// 中央サークル
		g.drawArc(200 - 130 / 2, 300 - 130 / 2, 130, 130, 0, 360);

		// ブルーゴールライン
		g.drawLine(100, 30 + 60, 300, 30 + 60);
		g.drawLine(100, 30, 100, 30 + 60);
		g.drawLine(300, 30, 300, 30 + 60);

		// イエローゴールライン
		g.drawLine(100, 570 - 60, 300, 570 - 60);
		g.drawLine(100, 570, 100, 570 - 60);
		g.drawLine(300, 570, 300, 570 - 60);

		g.setColor(Color.cyan);
		g.fillRect(200 - 150 / 2, 0, 150, 30);
		g.setColor(Color.yellow);
		g.fillRect(200 - 150 / 2, 570, 150, 30);

		// MCLの候補点を描画
		if (lc.getSelf() instanceof MonteCarloLocalization) {
			// Thread-safeでない
			MonteCarloLocalization mcl = (MonteCarloLocalization) lc.getSelf();
			Candidate[] c = mcl.getCandidates();
			for (int i = 0; i < c.length; i++)
				if (i % 10 == 0)
					drawCandidate(g, c[i], Color.LIGHT_GRAY);
		}

		// 自己位置を描画
		drawSelf(g, lc.getSelf(), Color.black);

		// GPS上の自己位置を描画
		drawSelf(g, gps, Color.GRAY);

		g.setColor(Color.orange);
		drawObject(g, lc.get(WorldObjects.Ball));
	}

	private void drawSelf(Graphics graphics, SelfLocalization self, Color c) {
		Graphics2D g = (Graphics2D) graphics;

		int x = (self.getX() - Field.MinX) / 10;
		int y = (-self.getY() - Field.MinY) / 10;
		double r = Math.toRadians(self.getHeading());

		g.setColor(c);
		g.fillArc(x - 25 / 2, y - 25 / 2, 25, 25, 0, 360);
		g.setColor(Color.red);
		g.drawLine(x, y, x + (int) (20 * Math.cos(r)), y
				- (int) (20 * Math.sin(r)));

		// double a = Math.toRadians(MathUtils.normalizeAngle180((float)
		// Math
		// .toDegrees(Math.atan2(Goal.BlueGoalY - self.getY(),
		// Goal.BlueGoalX - self.getX()))
		// ));
		// g.setColor(Color.cyan);
		// g.drawLine(x, y, x + (int) (20 * Math.cos(a)), y
		// - (int) (20 * Math.sin(a)));
	}

	private void drawCandidate(Graphics graphics,
			MonteCarloLocalization.Candidate self, Color c) {
		Graphics2D g = (Graphics2D) graphics;

		int x = (self.x - Field.MinX) / 10;
		int y = (-self.y - Field.MinY) / 10;
		double r = Math.toRadians(self.h);

		g.setColor(c);
		g.fillArc(x - 25 / 2, y - 25 / 2, 25, 25, 0, 360);
		g.setColor(Color.red);
		g.drawLine(x, y, x + (int) (20 * Math.cos(r)), y
				- (int) (20 * Math.sin(r)));
	}

	private void drawObject(Graphics graphics, WorldObject wo) {
		Graphics2D g = (Graphics2D) graphics;

		int x = (wo.getWorldX() - Field.MinX) / 10;
		int y = (-wo.getWorldY() - Field.MinY) / 10;

		g.fillArc(x - 15 / 2, y - 15 / 2, 15, 15, 0, 360);
	}
}
