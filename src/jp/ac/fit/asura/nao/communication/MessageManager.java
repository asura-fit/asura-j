/*
 * 作成日: 2008/06/29
 */
package jp.ac.fit.asura.nao.communication;

import jp.ac.fit.asura.nao.DatagramService;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 *
 * @version $Id: MessageManager.java 713 2008-11-24 06:27:48Z sey $
 *
 */
public class MessageManager implements RobotLifecycle {
	private Logger log = Logger.getLogger(MessageManager.class);

	private RobotContext robotContext;
	private DatagramService ds;

	private AsuraLink link;

	public MessageManager() {
	}

	public void init(RobotContext rctx) {
		robotContext = rctx;
		ds = robotContext.getDatagramService();
		link = new AsuraLink(rctx);
	}

	public void start() {
	}

	public void step() {
		byte[] buf;
		while ((buf = ds.receive()) != null) {
			log.trace("received packet. size:" + buf.length);

			try {
				if (buf.length >= 4
						&& RoboCupGameControlData.hasValidHeader(buf)) {
					RoboCupGameControlData gc = robotContext
							.getGameControlData();
					gc.update(buf);
					log.trace("update game control data:" + gc.toString());
				} else if (link.hasValidHeader(buf)) {
					log.trace("received asura link packet.");
					link.parse(buf);
				} else {
					log.warn("frame:" + robotContext.getFrame()
							+ "received unknown packet.");
				}
			} catch (IndexOutOfBoundsException e) {
				log.error("frame:" + robotContext.getFrame(), e);
			}
		}
	}

	public void stop() {
	}
}
