/*
 * 作成日: 2008/06/29
 */
package jp.ac.fit.asura.nao.communication;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jp.ac.fit.asura.nao.DatagramService;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.VisualCycle;
import jp.ac.fit.asura.nao.VisualFrameContext;
import jp.ac.fit.asura.nao.event.RoboCupMessageListener;
import jp.ac.fit.asura.nao.strategy.StrategyContext;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 *
 * @version $Id: MessageManager.java 713 2008-11-24 06:27:48Z sey $
 *
 */
public class MessageManager implements VisualCycle {
	private Logger log = Logger.getLogger(MessageManager.class);

	private RobotContext robotContext;
	private StrategyContext strategyContext;
	private DatagramService ds;

	private AsuraLink link;
	private ByteBuffer dsbuf = ByteBuffer.allocate(2048);

	private List<RoboCupMessageListener> roboCupListeners;

	private RoboCupGameControlData gameData;
	private AsuraLinkStrategySendData strategySendData;
	private AsuraLinkStrategyReceiveData strategyReceiveData;

	public MessageManager() {
		roboCupListeners = new CopyOnWriteArrayList<RoboCupMessageListener>();
		gameData = new RoboCupGameControlData();
	}

	@Override
	public void init(RobotContext rctx) {
		log.info("init Communication.");

		robotContext = rctx;

		ds = robotContext.getDatagramService();
		link = new AsuraLink(rctx);
	}

	@Override
	public void start() {
		strategyContext = robotContext.getStrategy().getContext();

		strategySendData = new AsuraLinkStrategySendData(strategyContext);
		strategyReceiveData = new AsuraLinkStrategyReceiveData(strategyContext);

		strategySendData.init(robotContext);
	}

	@Override
	public void step(VisualFrameContext context) {
		while (true) {
			dsbuf.clear();
			ds.receive(dsbuf);
				dsbuf.flip();

			if (!dsbuf.hasRemaining()) {
				return;
			}

			byte[] buf = dsbuf.array();

			log.trace("received packet. size:" + buf.length);


			try {
				if (buf.length >= 4
						&& RoboCupGameControlData.hasValidHeader(buf)) {
					gameData.update(buf);
					log.trace("update game control data:" + gameData.toString());
					fireUpdateGameData(gameData);
				} else if (link.hasValidHeader(buf)) {
					log.trace("received asura link packet.");
					log.trace("asura link packet. size:" + dsbuf.remaining());

					if (robotContext.getRobotId() == 0 && robotContext.getTeamId() == 0)
						for (int i=0; i<100; i++)
							log.trace("buf[" + i + "]: " + buf[i]);

					link.parse(buf);
				} else {
					log.warn("frame:" + context.getFrame()
							+ "received unknown packet.");
				}
			} catch (IndexOutOfBoundsException e) {
				log.error("frame:" + context.getFrame(), e);
			}
		}
	}

	@Override
	public void stop() {
	}

	private void fireUpdateGameData(RoboCupGameControlData gameData) {
		for (RoboCupMessageListener listener : roboCupListeners)
			listener.update(gameData);
	}

	public void addMessageListener(RoboCupMessageListener listener) {
		roboCupListeners.add(listener);
	}

	public void removeListener(RoboCupMessageListener listener) {
		roboCupListeners.remove(listener);
	}

	public AsuraLinkStrategySendData getStrategySendData() {
		return strategySendData;
	}
}
