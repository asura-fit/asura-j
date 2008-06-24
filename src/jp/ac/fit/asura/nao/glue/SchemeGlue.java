/*
 * 作成日: 2008/05/05
 */
package jp.ac.fit.asura.nao.glue;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import jp.ac.fit.asura.nao.Image;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.MotionFactory;
import jp.ac.fit.asura.nao.motion.MotorCortex;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jscheme.JScheme;
import jsint.BacktraceException;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class SchemeGlue implements RobotLifecycle {
	public enum InterpolationType {
		Raw(1), Liner(2), Compatible(3);
		private final int id;

		InterpolationType(int id) {
			this.id = id;
		}

		public static InterpolationType valueOf(int id) {
			for (InterpolationType t : InterpolationType.values()) {
				if (t.id == id)
					return t;
			}
			return null;
		}
	}

	private JScheme js;
	private MotorCortex motor;
	private TinyHttpd httpd;
	private RobotContext rctx;

	private int saveImageInterval;
	private boolean showPlane;

	private JFrame jFrame;
	private JLabel gcdLabel;
	private ImageIcon imageIcon;
	private BufferedImage image;

	/**
	 * 
	 */
	public SchemeGlue() {
		js = new JScheme();
		httpd = new TinyHttpd(js);
	}

	public void init(RobotContext context) {
		this.rctx = context;
		motor = context.getMotor();
		js.setGlobalValue("glue", this);

		showPlane = false;
		saveImageInterval = 0;
		image = null;

		try {
			js.load(new FileReader("init.scm"));
		} catch (BacktraceException e) {
			e.getBaseException().printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start() {
	}

	public void step() {
		if (saveImageInterval != 0 && rctx.getFrame() % saveImageInterval == 0) {
			System.out.println("save image.");
			int[] yvu = rctx.getVision().getGCD().getYvuPlane();
			Image image = rctx.getSensor().getImage();
			try {
				BufferedImage buf = new BufferedImage(image.getWidth(), image
						.getHeight(), BufferedImage.TYPE_INT_RGB);
				int[] pixels = ((DataBufferInt) buf.getRaster().getDataBuffer())
						.getData();
				System.arraycopy(yvu, 0, pixels, 0, image.getData().length);
				ImageIO.write(buf, "BMP", new File("snapshot/image"
						+ rctx.getFrame() + ".bmp"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (showPlane)
			drawPlane();
	}

	public void stop() {
	}

	public void glueStartHttpd(int port) {
		assert port > 0;
		if (httpd.isRunning()) {
			System.out.println("httpd is already running");
			return;
		}
		httpd.start(port);
	}

	public void glueStopHttpd() {
		if (!httpd.isRunning()) {
			System.out.println("httpd isn't running");
			return;
		}
		httpd.stop();
	}

	public void glueSetShowPlane(boolean b) {
		// オン>オフになるときに不可視にする
		if (showPlane && !b) {
			getJFrame().dispose();
			jFrame = null;
			gcdLabel = null;
			imageIcon = null;
			image = null;
		}
		showPlane = b;
	}

	public void glueSetSaveImageInterval(int interval) {
		saveImageInterval = interval;
	}

	public void mcRegistmotion(int id, String name, int interpolationType,
			Object[] scmArgs) {
		try {
			InterpolationType type = InterpolationType
					.valueOf(interpolationType);
			assert id >= 0;
			assert type != null;

			Motion motion;
			// 引数の型を変換してモーションを作成
			switch (type) {
			case Raw: {
				float[][] a1 = new float[scmArgs.length][];
				for (int i = 0; i < scmArgs.length; i++) {
					assert scmArgs[i].getClass().isArray();
					a1[i] = array2float((Object[]) scmArgs[i]);
				}
				motion = MotionFactory.Raw.create(a1);
				break;
			}
			case Compatible:
			case Liner: {
				assert scmArgs.length == 2;
				Object[] frames = (Object[]) scmArgs[0];
				Object[] frameStep = (Object[]) scmArgs[1];

				float[][] a1 = new float[frames.length][];
				for (int i = 0; i < frames.length; i++) {
					assert frames[i].getClass().isArray();
					a1[i] = array2float((Object[]) frames[i]);
				}

				int[] a2 = array2int(frameStep);
				if (type == InterpolationType.Compatible)
					motion = MotionFactory.Compatible.create(a1,
							a2);
				else
					motion = MotionFactory.Liner.create(a1, a2);

				System.out.println("Scheme::new motion registered. frames: "
						+ frames.length);

				break;
			}
			default:
				assert false;
				motion = null;
			}
			motion.setName(name);
			motion.setId(id);
			motor.registMotion(motion);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void mcMakemotion(int id) {
		System.out.println("makemotion:" + id);
		motor.makemotion(id, null);
	}

	public void drawPlane() {
		VisualContext vc = rctx.getVision().getVisualContext();
		byte[] plane = vc.gcdPlane;
		if (image == null)
			image = new BufferedImage(vc.camera.width, vc.camera.height,
					BufferedImage.TYPE_INT_RGB);
		int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer())
				.getData();
		rctx.getVision().getGCD().gcd2rgb(plane, pixels);

		if (imageIcon == null) {
			imageIcon = new ImageIcon();
			getJFrame().pack();
			getJFrame().setVisible(true);
			getJFrame().setAlwaysOnTop(true);
			getGcdLabel().setIcon(imageIcon);
		}
		imageIcon.setImage(image);
		getGcdLabel().repaint();
	}

	private float[] array2float(Object[] array) {
		float[] floatArray = new float[array.length];
		for (int i = 0; i < array.length; i++) {
			try {
				floatArray[i] = Float.parseFloat(array[i].toString());
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}
		return floatArray;
	}

	private int[] array2int(Object[] array) {
		int[] floatArray = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			try {
				floatArray[i] = Integer.parseInt(array[i].toString());
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
			}
		}
		return floatArray;
	}

	private JFrame getJFrame() {
		if (jFrame == null) {
			jFrame = new JFrame();
			jFrame.setPreferredSize(new Dimension(200, 200));
		}
		return jFrame;
	}

	private JLabel getGcdLabel() {
		if (gcdLabel == null) {
			gcdLabel = new JLabel();
			getJFrame().add(gcdLabel);
		}
		return gcdLabel;
	}
}
