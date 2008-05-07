/*
 * 作成日: 2008/05/05
 */
package jp.ac.fit.asura.nao.glue;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.MotorCortex;
import jscheme.JScheme;

/**
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public class SchemeGlue implements RobotLifecycle {
	JScheme js;
	MotorCortex motor;

	/**
	 * 
	 */
	public SchemeGlue() {
		js = new JScheme();

	}

	public void init(RobotContext context) {
		motor = context.getMotor();
		js.setGlobalValue("glue", this);

		try {
			js.load(new FileReader("init.scm"));
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

	public void mcRegistmotion(int id, String name, Object[] frames,
			Object[] frameStep) {
		Motion motion = new Motion();
		motion.setName(name);
		List<float[]> data = new ArrayList<float[]>(frames.length);

		for (Object obj : frames) {
			assert obj.getClass().isArray();
			Object[] frame = (Object[]) obj;
			data.add(array2float(frame));
		}
		motion.setData(data);
		motion.setFrameStep(array2int(frameStep));
		motor.registMotion(id, motion);
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
