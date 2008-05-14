/*
 * 作成日: 2008/05/05
 */
package jp.ac.fit.asura.nao.glue;

import java.io.FileReader;
import java.io.IOException;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.MotionFactory;
import jp.ac.fit.asura.nao.motion.MotorCortex;
import jscheme.JScheme;
import jsint.BacktraceException;

/**
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public class SchemeGlue implements RobotLifecycle {
	private JScheme js;
	private MotorCortex motor;
	private TinyHttpd httpd;

	/**
	 * 
	 */
	public SchemeGlue() {
		js = new JScheme();
		httpd = new TinyHttpd(js);
	}

	public void init(RobotContext context) {
		motor = context.getMotor();
		js.setGlobalValue("glue", this);

		try {
			js.load(new FileReader("scheme/init.scm"));
		} catch (BacktraceException e) {
			e.getBaseException().printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void start() {
		// TODO 自動生成されたメソッド・スタブ

	}

	public void stop() {
		// TODO 自動生成されたメソッド・スタブ

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

	public void mcRegistmotion(int id, String name, int motionFactoryType,
			Object[] scmArgs) {
		try {
			MotionFactory.Type type = MotionFactory.Type
					.valueOf(motionFactoryType);
			assert id >= 0;
			assert type != null;

			Object arg;
			// 引数の型を変換
			switch (type) {
			case Raw: {
				float[][] a1 = new float[scmArgs.length][];
				for (int i = 0; i < scmArgs.length; i++) {
					assert scmArgs[i].getClass().isArray();
					a1[i] = array2float((Object[]) scmArgs[i]);
				}
				arg = a1;
				break;
			}
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
				arg = new Object[] { a1, a2 };
				break;
			}
			default:
				assert false;
				arg = null;
			}

			Motion motion = MotionFactory.create(type, arg);
			motion.setName(name);
			motor.registMotion(id, motion);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
}
