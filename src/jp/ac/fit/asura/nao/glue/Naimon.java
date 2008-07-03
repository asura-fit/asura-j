/*
 * 作成日: 2008/06/29
 */
package jp.ac.fit.asura.nao.glue;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.localization.Localization;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.localization.WorldObjects;
import jp.ac.fit.asura.nao.localization.self.GPSLocalization;
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

			drawSelf(g, lc.getSelf(), Color.black);
			drawSelf(g, gps, Color.lightGray);

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

	private JFrame visionFrame;
	private JFrame fieldFrame;
	private JFrame schemeFrame;

	public void start() {
		getVisionFrame().setVisible(true);
		getFieldFrame().setVisible(true);
		getSchemeFrame().setVisible(true);
	}

	public void step() {
		visionFrame.repaint();
		fieldFrame.repaint();
	}

	public void stop() {
		getVisionFrame().setVisible(false);
		getFieldFrame().setVisible(false);
		getSchemeFrame().setVisible(false);
	}

	public void init(RobotContext context) {
		robotContext = context;
	}

	public void dispose() {
		getVisionFrame().setVisible(false);
		getFieldFrame().setVisible(false);
		getSchemeFrame().setVisible(false);
		getVisionFrame().dispose();
		getFieldFrame().dispose();
		getSchemeFrame().dispose();
	}

	private JFrame getVisionFrame() {
		if (visionFrame == null) {
			visionFrame = new JFrame("Vision");
			visionFrame
					.setContentPane(new VisionPanel(robotContext.getVision()));
			visionFrame.pack();
		}
		return visionFrame;
	}

	private JFrame getFieldFrame() {
		if (fieldFrame == null) {
			fieldFrame = new JFrame("Field");
			fieldFrame.setContentPane(new FieldPanel(robotContext));
			fieldFrame.pack();
		}
		return fieldFrame;
	}

	public JFrame getSchemeFrame() {
		if (schemeFrame == null) {
			schemeFrame = new JFrame("Scheme");
			schemeFrame.setPreferredSize(new Dimension(250, 150));
			Container panel = schemeFrame.getContentPane();
			panel.setLayout(new BorderLayout());

			// 入力エリアを作成
			final JTextArea text = new JTextArea();
			text.setBackground(new Color(0xC0, 0xC0, 0xC0));

			// ファイルからscheme式を読み込み
			try {
				StringBuilder sb = new StringBuilder();

				BufferedReader br = new BufferedReader(new FileReader(
						"scheme_out.txt"));

				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line);
					sb.append(System.getProperty("line.separator"));
				}
				text.setText(sb.toString());
			} catch (FileNotFoundException e2) {
				log.warn("", e2);
			} catch (IOException e2) {
				log.error("", e2);
			}

			JButton submit = new JButton("実行");
			submit.setPreferredSize(new Dimension(50, 25));
			submit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String scheme = text.getText();

					// ファイルに書き込み
					try {
						FileWriter fw = new FileWriter("scheme_out.txt");
						fw.write(scheme);
						fw.close();
					} catch (IOException e1) {
						log.error("", e1);
					}

					// Scheme式を実行
					robotContext.getGlue().eval(scheme);
				}
			});

			panel.add(new JScrollPane(text), BorderLayout.CENTER);
			panel.add(submit, BorderLayout.SOUTH);
			schemeFrame.pack();
		}
		return schemeFrame;
	}
}
