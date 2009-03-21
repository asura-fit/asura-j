/*
 * 作成日: 2009/03/19
 */
package jp.ac.fit.asura.nao.naoji;

import jp.ac.fit.asura.nao.AsuraCore;
import jp.ac.fit.asura.nao.naoji.DatagramSocketService;
import jp.ac.fit.asura.nao.naoji.NaojiDriver;
import jp.ac.fit.asura.nao.naoji.NaojiPlayer;
import jp.ac.fit.asura.nao.naoji.NaojiDriver.NaojiEffector;
import jp.ac.fit.asura.nao.naoji.NaojiDriver.NaojiSensor;
import jp.ac.fit.asura.naoji.Naoji;
import jp.ac.fit.asura.naoji.NaojiFactory;
import jp.ac.fit.asura.naoji.NaojiModule;

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
	static {
		// Naoji initilization.
		NaojiModule.addFactory(new NaojiFactory() {
			public Naoji create() {
				return new NaojiPlayer();
			}
		});
	}

	private AsuraCore core;

	public void init(Object arg0) {
		NaojiDriver driver = new NaojiDriver();
		NaojiSensor sensor = driver.new NaojiSensor();
		NaojiEffector effector = driver.new NaojiEffector();
		DatagramSocketService dss = new DatagramSocketService();

		core = new AsuraCore(effector, sensor, dss);
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
