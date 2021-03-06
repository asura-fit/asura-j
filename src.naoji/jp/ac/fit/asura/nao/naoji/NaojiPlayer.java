/*
 * 作成日: 2009/03/19
 */
package jp.ac.fit.asura.nao.naoji;

import jp.ac.fit.asura.nao.AsuraCore;
import jp.ac.fit.asura.nao.glue.SchemeGlue;
import jp.ac.fit.asura.nao.naoji.NaojiDriver.NaojiEffector;
import jp.ac.fit.asura.nao.naoji.NaojiDriver.NaojiSensor;
import jp.ac.fit.asura.naoji.Naoji;
import jp.ac.fit.asura.naoji.NaojiContext;
import jp.ac.fit.asura.naoji.NaojiFactory;

import org.apache.log4j.Logger;

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

	private static final Logger log = Logger.getLogger(NaojiPlayer.class);
	private AsuraCore core;
	private boolean isValid;

	public synchronized void init(NaojiContext arg0) {
		isValid = true;

		NaojiDriver driver = new NaojiDriver(arg0);
		NaojiSensor sensor = driver.new NaojiSensor();
		NaojiEffector effector = driver.new NaojiEffector();
		NaojiCamera camera = new NaojiCamera("/dev/video0", "/dev/i2c-0");
		DatagramSocketService dss = new DatagramSocketService();

		core = new AsuraCore(effector, sensor, dss, camera);
		log.info("NaojiPlayer init.");
		try {
			core.init();
			SchemeGlue glue = core.getRobotContext().getGlue();
			glue.setValue("naoji", arg0);
			glue.setValue("jalmotion", driver.motion);
			glue.setValue("jalmemory", driver.memory);
		} catch (Throwable e) {
			log.fatal("Initialization failed.", e);
			isValid = false;
			assert false;
		}
	}

	public void start() {
		// not implemented.
		log.info("NaojiPlayer start.");
		if (!isValid) {
			log.warn("NaojiPlayer is invalid.");
			return;
		}

		try {
			core.start();
		} catch (Throwable e) {
			log.fatal("Start failed.", e);
			isValid = false;
			assert false;
		}

		if (!isValid) {
			log.warn("NaojiPlayer is invalid.");
			return;
		}
	}

	public void stop() {
		log.info("NaojiPlayer stop.");
		if (!isValid) {
			log.warn("NaojiPlayer is invalid.");
			return;
		}
		try {
			core.stop();
		} catch (Exception e) {
			log.error("", e);
		}
		core.getRobotContext().getEffector().setPower(0.0f);
	}

	public synchronized void exit() {
		log.info("NaojiPlayer exit.");
		// core.exit();
	}
}
