/*
 * 作成日: 2008/10/01
 */
package jp.ac.fit.asura.nao.physical;

import static jp.ac.fit.asura.nao.physical.Robot.Frames.Body;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.CameraSelect;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.HeadPitch;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.HeadYaw;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.LAnklePitch;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.LAnkleRoll;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.LHipPitch;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.LHipRoll;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.LHipYawPitch;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.LKneePitch;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.LSole;
import static jp.ac.fit.asura.nao.physical.Robot.Frames.NaoCam;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import jp.ac.fit.asura.nao.AsuraCoreTest;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.glue.SchemeGlue;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import junit.framework.TestCase;

/**
 * @author sey
 *
 * @version $Id: RobotTest.java 717 2008-12-31 18:16:20Z sey $
 *
 */
public class RobotTest extends TestCase {
	public static Robot createRobot() throws IOException {
		RobotContext context = AsuraCoreTest.createCore().getRobotContext();
		SchemeGlue glue = new SchemeGlue();
		glue.init(context);
		glue.load(new FileReader("scheme/asura-js.scm"));
		glue.load(new FileReader("test/robot-test.scm"));
		Robot robot = (Robot) glue.getValue("robot");
		assert robot != null;
		return robot;
	}

	public void testFindRoute() throws Exception {
		Robot robot = createRobot();
		assertTrue(Arrays.equals(new Frames[] { Body, HeadYaw, HeadPitch,
				CameraSelect, NaoCam }, robot.findRoute(Body, NaoCam)));
		assertTrue(Arrays.equals(new Frames[] { Body, LHipYawPitch, LHipRoll,
				LHipPitch, LKneePitch, LAnklePitch, LAnkleRoll, LSole }, robot
				.findRoute(Body, LSole)));
		assertTrue(Arrays.equals(new Frames[] { LSole, LAnkleRoll, LAnklePitch,
				LKneePitch, LHipPitch, LHipRoll, LHipYawPitch, Body, }, robot
				.findRoute(LSole, Body)));
	}
}
