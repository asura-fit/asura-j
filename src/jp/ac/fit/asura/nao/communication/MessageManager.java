/*
 * 作成日: 2008/06/29
 */
package jp.ac.fit.asura.nao.communication;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import com.cyberbotics.webots.Controller;

import jp.ac.fit.asura.nao.DatagramService;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;

/**
 * @author $Author$
 * 
 * @version $Id$
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

			if (RoboCupGameControlData.hasValidHeader(buf)) {
				RoboCupGameControlData gc = robotContext.getGameControlData();
				gc.update(buf);
				log.trace("update game control data:" + gc.toString());
			} else if(link.hasValidHeader(buf)){
				log.trace("received asura link packet.");
				link.parse(buf);
			} else{
				log.warn("received unknown packet.");
			}
		}
	}

	public void stop() {
	}
}
