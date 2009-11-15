/**
 *
 */
package jp.ac.fit.asura.nao.misc;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.physical.RobotTest;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.FrameState;
import jp.ac.fit.asura.nao.sensation.SomaticContext;

/**
 * @author sey
 *
 */
public class KinematicsFrame extends JFrame {
	private SomaticContext sc;

	public KinematicsFrame() {
		valuePanel = new ValuePanel();
		controlPanel = new ControlPanel();
		textArea = new JTextArea();

		try {
			sc = new SomaticContext(RobotTest.createRobot());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		Kinematics.calculateForward(sc);
		valuePanel.addChain(sc.get(Frames.HeadPitch));
		valuePanel.addChain(sc.get(Frames.LElbowRoll));
		valuePanel.addChain(sc.get(Frames.LAnkleRoll));
		valuePanel.addChain(sc.get(Frames.RAnkleRoll));
		valuePanel.addChain(sc.get(Frames.RElbowRoll));
		for (Frames f : Frames.values())
			valuePanel.addFrame(sc.get(f));

		Container cpane = this.getContentPane();
		BoxLayout layout = new BoxLayout(cpane, BoxLayout.Y_AXIS);
		cpane.setLayout(layout);
		cpane.add(valuePanel);
		cpane.add(controlPanel);
		cpane.add(textArea);

		setPreferredSize(layout.preferredLayoutSize(this.getContentPane()));
		pack();

		Kinematics.SCALE = 0.125f;
		Kinematics.LANGLE = MathUtils.PIf / 16;
	}

	private ValuePanel valuePanel;
	private ControlPanel controlPanel;
	private JTextArea textArea;

	class ValuePanel extends JPanel {
		private JTable chainTable;
		private JTable frameTable;
		protected DefaultTableModel chainModel;
		protected DefaultTableModel frameModel;
		private List<Frames> chains;
		private List<Frames> frames;

		public ValuePanel() {
			chains = new ArrayList<Frames>();
			chainModel = new DefaultTableModel(0, 6);
			String[] labels = { "Name", "x", "y", "z", "Pitch", "Yaw", "Roll" };
			chainModel.setColumnIdentifiers(labels);
			chainTable = new JTable(chainModel);
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			JScrollPane chainScroll = new JScrollPane(chainTable);
			add(chainScroll);

			frames = new ArrayList<Frames>();
			frameModel = new DefaultTableModel(0, 6);
			String[] labels2 = { "Name", "θ[deg]", "x", "y", "z", "Pitch",
					"Yaw", "Roll" };
			frameModel.setColumnIdentifiers(labels2);
			frameTable = new JTable(frameModel);
			JScrollPane scroll2 = new JScrollPane(frameTable);
			add(scroll2);

			setPreferredSize(new Dimension(640, 640));
		}

		public void addChain(FrameState fs) {
			NumberFormat format = NumberFormat.getInstance();
			format.setMaximumFractionDigits(2);
			Vector<Object> row = new Vector<Object>();
			row.add(fs.getId().name());
			row.add(format.format(fs.getBodyPosition().x));
			row.add(format.format(fs.getBodyPosition().y));
			row.add(format.format(fs.getBodyPosition().z));
			Vector3f vec = new Vector3f();
			MatrixUtils.rot2pyr(fs.getBodyRotation(), vec);
			row.add(format.format(MathUtils.toDegrees(vec.x)));
			row.add(format.format(MathUtils.toDegrees(vec.y)));
			row.add(format.format(MathUtils.toDegrees(vec.z)));
			chainModel.addRow(row);
			chains.add(fs.getId());
		}

		public void addFrame(FrameState fs) {
			NumberFormat format = NumberFormat.getInstance();
			format.setMaximumFractionDigits(2);
			Vector<Object> row = new Vector<Object>();
			row.add(fs.getId().name());
			row.add(format.format(MathUtils.toDegrees(fs.getAngle())));
			row.add(format.format(fs.getBodyPosition().x));
			row.add(format.format(fs.getBodyPosition().y));
			row.add(format.format(fs.getBodyPosition().z));
			Vector3f vec = new Vector3f();
			MatrixUtils.rot2pyr(fs.getBodyRotation(), vec);
			row.add(format.format(MathUtils.toDegrees(vec.x)));
			row.add(format.format(MathUtils.toDegrees(vec.y)));
			row.add(format.format(MathUtils.toDegrees(vec.z)));
			frameModel.addRow(row);
			frames.add(fs.getId());
		}

		public void setFrame(FrameState fs) {
			int i = frames.indexOf(fs.getId());
			NumberFormat format = NumberFormat.getInstance();
			format.setMaximumFractionDigits(2);
			int j = 1;
			frameModel.setValueAt(format.format(MathUtils.toDegrees(fs
					.getAngle())), i, j++);
			frameModel
					.setValueAt(format.format(fs.getBodyPosition().x), i, j++);
			frameModel
					.setValueAt(format.format(fs.getBodyPosition().y), i, j++);
			frameModel
					.setValueAt(format.format(fs.getBodyPosition().z), i, j++);
			Vector3f vec = new Vector3f();
			MatrixUtils.rot2pyr(fs.getBodyRotation(), vec);
			frameModel.setValueAt(format.format(MathUtils.toDegrees(vec.x)), i,
					j++);
			frameModel.setValueAt(format.format(MathUtils.toDegrees(vec.y)), i,
					j++);
			frameModel.setValueAt(format.format(MathUtils.toDegrees(vec.z)), i,
					j++);
		}
	}

	class ControlPanel extends JPanel {
		public ControlPanel() {
			BoxLayout layout = new BoxLayout(this, BoxLayout.X_AXIS);
			setLayout(layout);

			JButton forwardButton = new JButton("ForwardK");
			forwardButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					// 現在位置を取得
					for (Frames frame : valuePanel.frames) {
						DefaultTableModel model = valuePanel.frameModel;
						int i = valuePanel.frames.indexOf(frame);
						sc.get(frame).setAngle(
								MathUtils.toRadians(Float.parseFloat(model
										.getValueAt(i, 1).toString())));
					}
					Kinematics.calculateForward(sc);
					for (Frames frame : valuePanel.frames) {
						valuePanel.setFrame(sc.get(frame));
					}
					printScheme();
				}
			});
			add(forwardButton);

			JButton invButton = new JButton("InverseK");
			invButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					// 現在位置を取得
					Kinematics.calculateForward(sc);

					SomaticContext sc2 = new SomaticContext(sc);
					// 最初に取得した値を目標に逆運動学計算
					for (Frames chain : valuePanel.chains) {
						FrameState fs = sc2.get(chain).clone();
						DefaultTableModel model = valuePanel.chainModel;
						int i = valuePanel.chains.indexOf(chain);
						int j = 1;
						fs.getBodyPosition().x = Float.parseFloat(model
								.getValueAt(i, j++).toString());
						fs.getBodyPosition().y = Float.parseFloat(model
								.getValueAt(i, j++).toString());
						fs.getBodyPosition().z = Float.parseFloat(model
								.getValueAt(i, j++).toString());
						float wx = Float.parseFloat(model.getValueAt(i, j++)
								.toString());
						float wy = Float.parseFloat(model.getValueAt(i, j++)
								.toString());
						float wz = Float.parseFloat(model.getValueAt(i, j++)
								.toString());
						MatrixUtils.pyr2rot(new Vector3f(wx, wy, wz), fs
								.getBodyRotation());

						try {
							float err = Kinematics.calculateInverse2(sc2,
									Frames.Body, fs);
							System.out.println("err:" + err);
						} catch (Exception e) {
							textArea.setText("Error! " + e.getMessage());
							return;
						}
					}
					sc = sc2;
					for (Frames frame : valuePanel.frames) {
						valuePanel.setFrame(sc.get(frame));
					}
					printScheme();
				}
			});
			add(invButton);
			setMaximumSize(layout.preferredLayoutSize(this));
		}

		private void printScheme() {
			StringBuilder text = new StringBuilder();
			text.append("#(");
			NumberFormat format = NumberFormat.getInstance();
			format.setMaximumFractionDigits(3);
			for (Frames f : Frames.values()) {
				if (f.isJoint() && f != Frames.HeadPitch && f != Frames.HeadYaw) {
					text.append(format.format(MathUtils.toDegrees(sc.get(f)
							.getAngle())));
					text.append(" ");
				}
			}
			text.append(")");
			textArea.setText(text.toString());
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		KinematicsFrame main = new KinematicsFrame();
		main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		main.pack();
		main.setVisible(true);
	}

}
