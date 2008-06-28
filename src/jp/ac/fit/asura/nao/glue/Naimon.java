/*
 * 作成日: 2008/06/29
 */
package jp.ac.fit.asura.nao.glue;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;
import javax.swing.JPanel;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.localization.Localization;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.localization.WorldObjects;
import jp.ac.fit.asura.nao.localization.self.SelfLocalization;
import jp.ac.fit.asura.nao.misc.PhysicalConstants.Field;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualCortex;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class Naimon implements RobotLifecycle {
	private Logger log = Logger.getLogger(Naimon.class);

	private class FieldPanel extends JPanel {
		private Localization lc;

		public FieldPanel(RobotContext context) {
			lc = context.getLocalization();
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

			drawSelf(g, lc.getSelf());

			g.setColor(Color.orange);
			drawObject(g, lc.get(WorldObjects.Ball));
		}

		private void drawSelf(Graphics graphics, SelfLocalization self) {
			Graphics2D g = (Graphics2D) graphics;

			int x = (self.getX() - Field.MinX) / 10;
			int y = (-self.getY() - Field.MinY) / 10;
			double r = Math.toRadians(self.getHeading());

			g.setColor(Color.black);
			g.fillArc(x - 25 / 2, y - 25 / 2, 25, 25, 0, 360);
			g.setColor(Color.red);
			g.drawLine(x, y, x + (int) (20 * Math.cos(r)), y
					- (int) (20 * Math.sin(r)));
		}

		private void drawObject(Graphics graphics, WorldObject wo) {
			Graphics2D g = (Graphics2D) graphics;

			int x = (wo.getX() - Field.MinX) / 10;
			int y = (-wo.getY() - Field.MinY) / 10;

			g.fillArc(x - 15 / 2, y - 15 / 2, 15, 15, 0, 360);
		}
	}

	private class VisionPanel extends JPanel {
		private VisualCortex vision;
		private BufferedImage image = null;

		public VisionPanel(VisualCortex vision) {
			this.vision = vision;
			VisualContext vc = vision.getVisualContext();
			image = new BufferedImage(160, 120, BufferedImage.TYPE_INT_RGB);
			setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
		}

		protected void paintComponent(Graphics g) {
			VisualContext vc = vision.getVisualContext();
			byte[] plane = vc.gcdPlane;
			if (plane == null)
				return;

			int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer())
					.getData();
			vision.getGCD().gcd2rgb(plane, pixels);
			g.drawImage(image, 0, 0, Color.black, null);
		}
	}

	private RobotContext robotContext;

	private JFrame jFrame;
	private VisionPanel visionPanel;
	private FieldPanel fieldPanel;

	public void start() {
		getJFrame().setVisible(true);
		getJFrame().setAlwaysOnTop(true);
	}

	public void step() {
		visionPanel.repaint();
		fieldPanel.repaint();
	}

	public void stop() {
		getJFrame().setVisible(false);
	}

	public void init(RobotContext context) {
		robotContext = context;
	}

	public void dispose() {
		getJFrame().setVisible(false);
		jFrame.dispose();
		jFrame = null;
	}

	private JFrame getJFrame() {
		if (jFrame == null) {
			try {
				jFrame = new JFrame("Naimon");
				jFrame.getContentPane()
						.add(getVisionPanel(), BorderLayout.WEST);
				jFrame.getContentPane().add(getFieldPanel(),
						BorderLayout.CENTER);
			} catch (RuntimeException e) {
				log.error("", e);
				throw e;
			}
			jFrame.pack();
		}
		return jFrame;
	}

	private VisionPanel getVisionPanel() {
		if (visionPanel == null) {
			visionPanel = new VisionPanel(robotContext.getVision());
		}
		return visionPanel;
	}

	private FieldPanel getFieldPanel() {
		if (fieldPanel == null) {
			fieldPanel = new FieldPanel(robotContext);
		}
		return fieldPanel;
	}
}
