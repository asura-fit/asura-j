/*
 * 作成日: 2008/11/24
 */
package jp.ac.fit.asura.nao.glue.naimon;

import static jp.ac.fit.asura.nao.AsuraCoreTest.createDatagramServiceStub;
import static jp.ac.fit.asura.nao.AsuraCoreTest.createEffectorStub;
import static jp.ac.fit.asura.nao.AsuraCoreTest.createSensorStub;

import javax.swing.JFrame;

import jp.ac.fit.asura.nao.AsuraCore;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.motion.MotionUtils;
import junit.framework.TestCase;

/**
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public class PressurePanelTest extends TestCase {

	public void testPressurePanel() throws Exception {
		float[] joints = new float[Joint.values().length];
		AsuraCore core = new AsuraCore(createEffectorStub(),
				createSensorStub(joints), createDatagramServiceStub());
		core.init();
		core.start();
		RobotContext context = core.getRobotContext();
		JFrame jf = create(context);
		jf.setVisible(true);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		for (int i = 0; i < 1000; i++) {
			System.out.println("step");
			context.getSensoryCortex().step();
			context.getGlue().step();
			jf.repaint();
			Thread.sleep(200);
			double s = 2 * Math.PI * i / 300.0;
			// joints[Joint.LKneePitch.ordinal()] = (float) s;
			joints[Joint.LAnklePitch.ordinal()] = (float) -s / 2;
			//joints[Joint.LHipPitch.ordinal()] = (float) -s / 2;
			// joints[Joint.RKneePitch.ordinal()] = joints[Joint.LKneePitch
			// .ordinal()];
			joints[Joint.RAnklePitch.ordinal()] = joints[Joint.LAnklePitch
					.ordinal()];
			// joints[Joint.RHipPitch.ordinal()] = joints[Joint.LHipPitch
			// .ordinal()];
			for(Joint j : Joint.values()){
//				joints[j.ordinal()] = MotionUtils.clipping(j, joints[j.ordinal()]);
			}
		}
	}

	private JFrame create(RobotContext context) {
		JFrame fieldFrame = new JFrame();
		fieldFrame.setContentPane(new PressurePanel(context.getSensor(),
				context.getSensoryCortex()));
		fieldFrame.pack();
		return fieldFrame;
	}
}
