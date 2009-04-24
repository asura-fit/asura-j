/*
 * 作成日: 2008/06/29
 */
package jp.ac.fit.asura.naimon;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.EnumMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.Motions;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 *
 * @version $Id: Naimon.java 713 2008-11-24 06:27:48Z sey $
 *
 */
public class Naimon {
	public enum NaimonFrames {
		VISION, FIELD, MAKEMOTIONHELPER, PRESSURE, SCHEME

	};

	private static final Logger log = Logger.getLogger(Naimon.class);

	private RobotContext robotContext;

	private EnumMap<NaimonFrames, JFrame> frames;

	public Naimon() {
		frames = new EnumMap<NaimonFrames, JFrame>(NaimonFrames.class);
	}

	private JFrame createVisionFrame() {
		// String title = "Vision [" + robotContext.getStrategy().getTeam() +
		// ":"
		// + robotContext.getRobotId() + "]";
		String title = "Vision";
		JFrame visionFrame = new JFrame(title);
		visionFrame.setContentPane(new VisionPanel());
		visionFrame.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				setEnable(NaimonFrames.VISION, false);
			}
		});
		visionFrame.pack();
		return visionFrame;
	}

	private JFrame createFieldFrame() {
		JFrame fieldFrame = new JFrame("Field ["
				+ robotContext.getStrategy().getTeam() + ":"
				+ robotContext.getRobotId() + "]");
		fieldFrame.setContentPane(new FieldPanel(robotContext));
		fieldFrame.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				setEnable(NaimonFrames.FIELD, false);
			}
		});
		fieldFrame.pack();
		return fieldFrame;
	}

	private JFrame createSchemeFrame() {
		JFrame schemeFrame = new JFrame("Scheme ["
				+ robotContext.getStrategy().getTeam() + ":"
				+ robotContext.getRobotId() + "]");
		schemeFrame.setPreferredSize(new Dimension(400, 350));
		schemeFrame.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				setEnable(NaimonFrames.SCHEME, false);
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
			}
		});

		panel.add(new JScrollPane(text), BorderLayout.CENTER);
		JPanel control = new JPanel();
		control.add(submit);
		control.add(selectedSubmit);
		control.add(getupButton);
		panel.add(control, BorderLayout.SOUTH);
		schemeFrame.pack();
		return schemeFrame;
	}

	private JFrame createMakeMotionHelperFrame() {
		JFrame makeMotionHelperFrame = new JFrame("makeMotionHelper ["
				+ robotContext.getStrategy().getTeam() + ":"
				+ robotContext.getRobotId() + "]");
		makeMotionHelperFrame.setPreferredSize(new Dimension(500, 120));
		makeMotionHelperFrame.setLayout(null);
		makeMotionHelperFrame.setResizable(false);
		makeMotionHelperFrame.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				setEnable(NaimonFrames.MAKEMOTIONHELPER, false);
			}
		});

		final JLabel mMotionText = new JLabel() {
			protected void paintComponent(Graphics g) {
				String mt = "#( ";
				for (Joint j : Joint.values()) {
//					mt += (int) robotContext.getSensor().getJointDegree(j)
//							+ " ";
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
				robotContext.getEffector().setPower(1.0f);
			}
		});

		// 転んだ
		JButton stopMotorButton = new JButton("モーター停止");
		stopMotorButton.setSize(new Dimension(140, 30));
		stopMotorButton.setLocation(150, 40);
		stopMotorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				robotContext.getEffector().setPower(1.0f);
			}
		});

		makeMotionHelperFrame.add(cbCopyButton);
		makeMotionHelperFrame.add(startMotorButton);
		makeMotionHelperFrame.add(stopMotorButton);

		makeMotionHelperFrame.pack();
		return makeMotionHelperFrame;
	}

	private JFrame createPressureFrame() {
		JFrame pressureFrame = new JFrame("Pressure");
		pressureFrame.setContentPane(new PressurePanel(
				robotContext.getSensor(), robotContext.getSensoryCortex()));
		pressureFrame.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				setEnable(NaimonFrames.PRESSURE, false);
			}
		});
		pressureFrame.pack();
		return pressureFrame;
	}

	public void setEnable(NaimonFrames f, boolean bool) {
		if (frames.containsKey(f) == bool)
			return;
		if (bool) {
			assert !frames.containsKey(f);
			JFrame frame = null;
			switch (f) {
			case FIELD:
				frame = createFieldFrame();
				break;
			case MAKEMOTIONHELPER:
				frame = createMakeMotionHelperFrame();
				break;
			case PRESSURE:
				frame = createPressureFrame();
				break;
			case SCHEME:
				frame = createSchemeFrame();
				break;
			case VISION:
				frame = createVisionFrame();
				break;
			default:
				assert false;
			}
			assert frame != null;
			frame.setVisible(true);
			frames.put(f, frame);
		} else {
			assert frames.containsKey(f);
			frames.get(f).setVisible(false);
			frames.get(f).dispose();
			frames.remove(f);
		}
	}

	public static void main(String[] args) {
		Naimon naimon = new Naimon();
		naimon.setEnable(NaimonFrames.VISION, true);
	}
}
