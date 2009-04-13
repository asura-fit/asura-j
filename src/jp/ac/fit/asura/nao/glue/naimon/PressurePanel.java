/*
 * 作成日: 2008/11/23
 */
package jp.ac.fit.asura.nao.glue.naimon;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.util.EnumMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.PressureSensor;
import jp.ac.fit.asura.nao.Sensor;
import jp.ac.fit.asura.nao.physical.RobotFrame;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.SomatoSensoryCortex;

/**
 * @author sey
 *
 * @version $Id: $
 *
 */
@Deprecated
class PressurePanel extends JPanel {
	private Sensor sensor;
	private SomatoSensoryCortex ssc;
	private EnumMap<Frames, JLabel> soles;

	class PressureLabel extends JLabel {
		private PressureSensor ts;

		public PressureLabel(PressureSensor ts) {
			this.ts = ts;
		}

		protected void paintComponent(Graphics g) {
			this.setText(Double.toString(sensor.getForce(ts)));
			super.paintComponent(g);
		}
	}

	public PressurePanel(Sensor sensor, SomatoSensoryCortex ssc) {
		this.sensor = sensor;
		this.ssc = ssc;
		setPreferredSize(new Dimension(400, 300));
		soles = new EnumMap<Frames, JLabel>(Frames.class);
		soles.put(Frames.LFsrFL, new PressureLabel(PressureSensor.LFsrFL));
		soles.put(Frames.LFsrFR, new PressureLabel(PressureSensor.LFsrFR));
		soles.put(Frames.LFsrBL, new PressureLabel(PressureSensor.LFsrBL));
		soles.put(Frames.LFsrBR, new PressureLabel(PressureSensor.LFsrBR));
		soles.put(Frames.RFsrFL, new PressureLabel(PressureSensor.RFsrFL));
		soles.put(Frames.RFsrFR, new PressureLabel(PressureSensor.RFsrFR));
		soles.put(Frames.RFsrBL, new PressureLabel(PressureSensor.RFsrBL));
		soles.put(Frames.RFsrBR, new PressureLabel(PressureSensor.RFsrBR));

		setLayout(null);

		// Font font = new Font(Font.SERIF, Font.BOLD, 20);
		Font font = new Font("SERIF", Font.BOLD, 20);
		for (JLabel label : soles.values()) {
			label.setSize(new Dimension(40, 20));
			label.setFont(font);
			add(label);
		}
	}

	protected void paintComponent(Graphics g) {
		// 画像上の中心を計算
		Point c = new Point();
		c.x = getPreferredSize().width / 2;
		c.y = getPreferredSize().height / 2;

		// それぞれの足の現在位置(ボディ座標)を取得
		// 本当はボディ座標からロボット座標に変換して使うべき
		Vector3f tmp = new Vector3f();
		ssc.body2robotCoord(ssc.getContext().get(Frames.LSole)
				.getBodyPosition(), tmp);
		Point lSole = toLocation(tmp);
		ssc.body2robotCoord(ssc.getContext().get(Frames.RSole)
				.getBodyPosition(), tmp);
		Point rSole = toLocation(tmp);

		// Labelの位置をセット
		for (Frames f : soles.keySet()) {
			RobotFrame rf = ssc.getContext().getRobot().get(f);
			Point loc = toLocation(rf.getTranslation());
			Point base;
			if (rf.getParent() == ssc.getContext().getRobot().get(
					Frames.LAnkleRoll)) {
				base = lSole;
			} else if (rf.getParent() == ssc.getContext().getRobot().get(
					Frames.RAnkleRoll)) {
				base = rSole;
			} else {
				assert false : f + " is not a sole parts.";
				base = new Point();
			}
			loc.x += c.x + base.x;
			loc.y += c.y + base.y;
			soles.get(f).setLocation(loc);
		}

		super.paintComponent(g);

		// ボディ中心を描画
		drawCircle(g, c, 5);

		int width = (int) (0.08 * 1000);
		int height = (int) (0.16 * 1000);
		int lx = c.x + lSole.x - width / 2 - 0;
		int ly = c.y + lSole.y - height / 2 - (int) (0.03 * 1000);
		int rx = c.x + rSole.x - width / 2 - 0;
		int ry = c.y + rSole.y - height / 2 - (int) (0.03 * 1000);

		// 足の枠を描画
		g.drawRect(lx, ly, width, height);
		g.drawRect(rx, ry, width, height);

		// 各圧力センサーの点を描画
		for (Frames f : soles.keySet()) {
			assert f.isPressureSensor();
			JLabel l = soles.get(f);
			float force = sensor.getForce(f.toPressureSensor());
			drawCircle(g, l.getLocation(), (int) Math.round(Math
					.sqrt(force * 8)));
		}

		float lf = ssc.getLeftPressure();
		float rf = ssc.getRightPressure();

		Point cop = new Point();
		float force = 0;

		// 左足の圧力中心(測定値)を描画
		if (lf > 0) {
			Point leftCOP = new Point();
			ssc.getLeftCOP(leftCOP);
			leftCOP.x = -leftCOP.x;
			leftCOP.y = -leftCOP.y;

			leftCOP.x += lSole.x;
			leftCOP.y += lSole.y;

			cop.x += leftCOP.x * lf;
			cop.y += leftCOP.y * lf;

			g.setColor(Color.pink);
			g.fillArc(leftCOP.x + c.x, leftCOP.y + c.y, 20, 20, 0, 360);

			force += lf;
		}

		// 右足の圧力中心(測定値)を描画
		if (rf > 0) {
			Point rightCOP = new Point();
			ssc.getRightCOP(rightCOP);
			rightCOP.x = -rightCOP.x;
			rightCOP.y = -rightCOP.y;

			rightCOP.x += rSole.x;
			rightCOP.y += rSole.y;

			cop.x += rightCOP.x * rf;
			cop.y += rightCOP.y * rf;
			g.setColor(Color.yellow);
			g.fillArc(rightCOP.x + c.x, rightCOP.y + c.y, 20, 20, 0, 360);

			force += rf;
		}

		// 圧力中心を描画
		if (force > 0) {
			cop.x /= force;
			cop.y /= force;
			g.setColor(Color.cyan);
			g.fillArc(cop.x + c.x, cop.y + c.y, 20, 20, 0, 360);
		}

		// 重心位置(計算値)を描画
		ssc.body2robotCoord(ssc.getContext().getCenterOfMass(), tmp);
		Point com = toLocation(tmp);
		g.setColor(Color.blue);
		g.fillArc(com.x + c.x, com.y + c.y, 20, 20, 0, 360);

		// Point leftCOM = new Point();
		// leftCOM.x = lSole.x / (lSole.x + rSole.x);
		// leftCOM.y = lSole.y / (lSole.y + rSole.y);
		//
		// Point rightCOM = new Point();
		// rightCOM.x = rSole.x / (lSole.x + rSole.x);
		// rightCOM.y = rSole.y / (lSole.y + rSole.y);
	}

	private Point toLocation(Vector3f vec) {
		return new Point((int) (-vec.x), (int) (-vec.z));
	}

	private void drawCircle(Graphics g, Point p, int radius) {
		g.fillArc(p.x - radius, p.y - radius, radius * 2, radius * 2, 0, 360);
	}
}
