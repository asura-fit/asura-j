/*
 * 作成日: 2009/03/19
 */
package jp.ac.fit.asura.nao.naoji;

import jp.ac.fit.asura.nao.AsuraCore;
import jp.ac.fit.asura.nao.naoji.NaojiDriver.NaojiEffector;
import jp.ac.fit.asura.nao.naoji.NaojiDriver.NaojiSensor;
import jp.ac.fit.asura.naoji.Naoji;
import jp.ac.fit.asura.naoji.NaojiContext;
import jp.ac.fit.asura.naoji.NaojiFactory;

/**
 *
 * Naoji用のbootstrapクラス.
 *
 * @author $Author: sey $
 *
 * @version $Id: $
 *
 */
public class NaojiPlayer implements Naoji {
	public static class Factory implements NaojiFactory {
		public Naoji create() {
			return new NaojiPlayer();
		}
	}

	private AsuraCore core;

	public void init(NaojiContext arg0) {
		NaojiDriver driver = new NaojiDriver();
		NaojiSensor sensor = driver.new NaojiSensor();
		NaojiEffector effector = driver.new NaojiEffector();
		NaojiCamera camera = new NaojiCamera("/dev/video0", "/dev/i2c-0");
		DatagramSocketService dss = new DatagramSocketService();

		core = new AsuraCore(effector, sensor, dss,camera);
		core.init();
	}

	public void start() {
		// not implemented.
		// core.run(40);
	}

	public void stop() {
	}

	public void exit() {
		// core.exit();
	}
}
