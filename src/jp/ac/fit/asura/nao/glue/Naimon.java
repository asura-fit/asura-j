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
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.localization.Localization;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.localization.WorldObjects;
import jp.ac.fit.asura.nao.localization.self.GPSLocalization;
import jp.ac.fit.asura.nao.localization.self.SelfLocalization;
import jp.ac.fit.asura.nao.misc.PhysicalConstants.Field;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.Motions;
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
	private JFrame makeMotionHelperFrame;

	private boolean enableVision;
	private boolean enableField;
	private boolean enableScheme;
	private boolean enableMakeMotionHelper;

	private boolean getup = false;

	public void start() {
	}

	public void step() {
		if (enableVision)
			getVisionFrame().repaint();
		if (enableField)
			getFieldFrame().repaint();
		if (enableMakeMotionHelper)
			getMakeMotionHelperFrame().repaint();

		if (getup) {
			Motion current = robotContext.getMotor().getCurrentMotion();
			if (current == null) {
				robotContext.getMotor()
						.makemotion(Motions.MOTION_YY_GETUP_BACK);
				return;
			}
			if (current.getId() == Motions.MOTION_YY_GETUP_BACK) {
				robotContext.getMotor().makemotion(Motions.MOTION_GETUP);
			} else if (current.getId() == Motions.MOTION_GETUP) {
				robotContext.getMotor().makemotion(Motions.MOTION_STOP2);
				getup = false;
			} else {
				robotContext.getMotor()
						.makemotion(Motions.MOTION_YY_GETUP_BACK);
			}
		}
	}

	public void stop() {
		setEnableField(false);
		setEnableVision(false);
		setEnableScheme(false);
		setEnableMakeMotionHelper(false);
	}

	public void init(RobotContext context) {
		robotContext = context;
	}

	public void dispose() {
	}

	private JFrame getVisionFrame() {
		if (visionFrame == null) {
			visionFrame = new JFrame("Vision [" + robotContext.getStrategy().getTeam() + ":" + robotContext.getRobotId() + "]");
			visionFrame
					.setContentPane(new VisionPanel(robotContext.getVision()));
			visionFrame.addWindowListener(new WindowAdapter() {
				public void windowClosed(WindowEvent e) {
					setEnableVision(false);
				}
			});
			visionFrame.pack();
		}
		return visionFrame;
	}

	private JFrame getFieldFrame() {
		if (fieldFrame == null) {
			fieldFrame = new JFrame("Field [" + robotContext.getStrategy().getTeam() + ":" + robotContext.getRobotId() + "]");
			fieldFrame.setContentPane(new FieldPanel(robotContext));
			fieldFrame.addWindowListener(new WindowAdapter() {
				public void windowClosed(WindowEvent e) {
					setEnableField(false);
				}
			});
			fieldFrame.pack();
		}
		return fieldFrame;
	}

	private JFrame getSchemeFrame() {
		if (schemeFrame == null) {
			schemeFrame = new JFrame("Scheme [" + robotContext.getStrategy().getTeam() + ":" + robotContext.getRobotId() + "]");
			schemeFrame.setPreferredSize(new Dimension(400, 350));
			schemeFrame.addWindowListener(new WindowAdapter() {
				public void windowClosed(WindowEvent e) {
					setEnableScheme(false);
				}
			});

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

			// 書かれているやつ全部実行
			JButton submit = new JButton("全部実行");
			submit.setPreferredSize(new Dimension(120, 40));
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

			// 選択されているやつだけ実行
			JButton selectedSubmit = new JButton("選択範囲実行");
			selectedSubmit.setPreferredSize(new Dimension(120, 40));
			selectedSubmit.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String scheme = text.getText();
					String slscheme = text.getSelectedText();

					// ファイルに書き込み
					try {
						FileWriter fw = new FileWriter("scheme_out.txt");
						fw.write(scheme);
						fw.close();
					} catch (IOException e1) {
						log.error("", e1);
					}

					// Scheme式を実行
					robotContext.getGlue().eval(slscheme);
				}
			});

			JButton getupButton = new JButton("起き上がる");
			getupButton.setPreferredSize(new Dimension(120, 40));
			getupButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					getup = true;
				}
			});

			panel.add(new JScrollPane(text), BorderLayout.CENTER);
			JPanel control = new JPanel();
			control.add(submit);
			control.add(selectedSubmit);
			control.add(getupButton);
			panel.add(control, BorderLayout.SOUTH);
			schemeFrame.pack();
		}
		return schemeFrame;
	}

	private JFrame getMakeMotionHelperFrame() {
		if (makeMotionHelperFrame == null) {
			makeMotionHelperFrame = new JFrame("makeMotionHelper [" + robotContext.getStrategy().getTeam() + ":" + robotContext.getRobotId() + "]");
			makeMotionHelperFrame.setPreferredSize(new Dimension(500, 120));
			makeMotionHelperFrame.setLayout(null);
			makeMotionHelperFrame.setResizable(false);
			makeMotionHelperFrame.addWindowListener(new WindowAdapter() {
				public void windowClosed(WindowEvent e) {
					setEnableMakeMotionHelper(false);
				}
			});

			final JLabel mMotionText = new JLabel() {
				protected void paintComponent(Graphics g) {
					String mt = "#( ";
					for (Joint j : Joint.values()) {
						mt += (int) robotContext.getSensor().getJointDegree(j)
								+ " ";
					}
					mt += ")\n";
					setText(mt);
					super.paintComponent(g);
				}
			};
			mMotionText.setName("motionText");
			mMotionText.setLocation(0, 10);
			mMotionText.setSize(new Dimension(500, 30));
			makeMotionHelperFrame.add(mMotionText);

			JButton cbCopyButton = new JButton("クリップボードにコピー");
			cbCopyButton.setSize(new Dimension(180, 30));
			cbCopyButton.setLocation(300, 40);
			cbCopyButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					// コピー
					Clipboard clipboard = Toolkit.getDefaultToolkit()
							.getSystemClipboard();
					StringSelection selection = new StringSelection(mMotionText
							.getText());
					clipboard.setContents(selection, selection);
				}
			});

			// だるまさんが
			JButton startMotorButton = new JButton("モーター作動");
			startMotorButton.setSize(new Dimension(140, 30));
			startMotorButton.setLocation(10, 40);
			startMotorButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					robotContext.getEffector().setPower(true);
				}
			});

			// 転んだ
			JButton stopMotorButton = new JButton("モーター停止");
			stopMotorButton.setSize(new Dimension(140, 30));
			stopMotorButton.setLocation(150, 40);
			stopMotorButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					robotContext.getEffector().setPower(false);
				}
			});

			makeMotionHelperFrame.add(cbCopyButton);
			makeMotionHelperFrame.add(startMotorButton);
			makeMotionHelperFrame.add(stopMotorButton);

			makeMotionHelperFrame.pack();
		}
		return makeMotionHelperFrame;
	}

	public void setEnableVision(boolean bool) {
		if (this.enableVision == bool)
			return;
		this.enableVision = bool;
		if (enableVision) {
			getVisionFrame().setVisible(true);
		} else {
			getVisionFrame().setVisible(false);
			getVisionFrame().dispose();
			visionFrame = null;
		}
	}

	public void setEnableField(boolean bool) {
		if (this.enableField == bool)
			return;
		this.enableField = bool;
		if (enableField) {
			getFieldFrame().setVisible(true);
		} else {
			getFieldFrame().setVisible(false);
			getFieldFrame().dispose();
			fieldFrame = null;
		}
	}

	public void setEnableScheme(boolean bool) {
		if (this.enableScheme == bool)
			return;
		this.enableScheme = bool;
		if (enableScheme) {
			getSchemeFrame().setVisible(true);
		} else {
			getSchemeFrame().setVisible(false);
			getSchemeFrame().dispose();
			schemeFrame = null;
		}
	}

	public void setEnableMakeMotionHelper(boolean bool) {
		if (this.enableMakeMotionHelper == bool)
			return;
		this.enableMakeMotionHelper = bool;
		if (enableMakeMotionHelper) {
			getMakeMotionHelperFrame().setVisible(true);
		} else {
			getMakeMotionHelperFrame().setVisible(false);
			getMakeMotionHelperFrame().dispose();
			makeMotionHelperFrame = null;
		}
	}
}
