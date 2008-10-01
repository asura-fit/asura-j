/*
 * 作成日: 2008/10/01
 */
package jp.ac.fit.asura.nao.physical;

import static jp.ac.fit.asura.nao.physical.Nao.Frames.*;

import java.util.Arrays;

import jp.ac.fit.asura.nao.physical.Nao.Frames;
import junit.framework.TestCase;

/**
 * @author sey
 * 
 * @version $Id: $
 * 
 */
public class NaoTest extends TestCase {
	public void testFindRoute() {
		assertTrue(Arrays.equals(new Frames[] { Body, HeadYaw, HeadPitch,
				Camera }, Nao.findRoute(Body, Camera)));
		assertTrue(Arrays.equals(new Frames[] { Body, LHipYawPitch, LHipRoll,
				LHipPitch, LKneePitch, LAnklePitch, LAnkleRoll, LSole }, Nao
				.findRoute(Body, LSole)));
		assertTrue(Arrays.equals(new Frames[] { LSole, LAnkleRoll, LAnklePitch,
				LKneePitch, LHipPitch, LHipRoll, LHipYawPitch, Body, }, Nao
				.findRoute(LSole, Body)));
	}
}
