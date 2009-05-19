/*
 * 作成日: 2009/04/11
 */
package jp.ac.fit.asura.nao.glue.naimon;

import static jp.ac.fit.asura.nao.vision.VisualObjects.Ball;
import static jp.ac.fit.asura.nao.vision.VisualObjects.BlueGoal;
import static jp.ac.fit.asura.nao.vision.VisualObjects.YellowGoal;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.SensorContext;
import jp.ac.fit.asura.nao.Switch;
import jp.ac.fit.asura.nao.event.VisualEventListener;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualCortex;
import jp.ac.fit.asura.nao.vision.VisualObjects;
import jp.ac.fit.asura.nao.vision.perception.BallVisualObject;
import jp.ac.fit.asura.nao.vision.perception.GoalVisualObject;
import jp.ac.fit.asura.nao.vision.perception.VisualObject;

import org.apache.log4j.Logger;

/**
 * @author $Author: KEIY $
 *
 * @version $Id: $
 *
 *          いろんな値をXMLで公開する
 *
 */
public class NaimonValuesServlet extends HttpServlet {
	private static final Logger log = Logger
			.getLogger(NaimonValuesServlet.class);
	private RobotContext robotContext;

	public NaimonValuesServlet(RobotContext context) {
		this.robotContext = context;
	}

	protected void doGet(final HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		log.info("process XML request");
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/xml");

		final PrintWriter w = resp.getWriter();

		// 手始めにVisualObjectをXML形式に変換
		final Object lock = new Object();

		synchronized (lock) {
			String xml;
			xml = "<?xml version=\"1.0\" encoding=\"Shift_JIS\" ?>\n";

			xml += "<Values>\n";
			w.print(xml);
			lock.notifyAll();
		}

		VisualEventListener listener = new VisualEventListener() {
			public void updateVision(VisualContext context) {
				String xml = "";
				// xml = "<?xml version=\"1.0\" encoding=\"Shift_JIS\" ?>\n";
				//
				// xml += "<Values>\n";
				for (VisualObjects key : VisualObjects.values()) {
					VisualObject vo = context.get(key);
					xml += "\t<VisualObject name=\"" + vo.getType() + "\">\n";
					xml += "\t\t<CenterX>" + vo.center.x + "</CenterX>\n";
					xml += "\t\t<CenterY>" + vo.center.y + "</CenterY>\n";
					xml += "\t\t<AngleX>" + vo.angle.x + "</AngleX>\n";
					xml += "\t\t<AngleY>" + vo.angle.y + "</AngleY>\n";
					xml += "\t\t<RobotAngleX>" + vo.robotAngle.x
							+ "</RobotAngleX>\n";
					xml += "\t\t<RobotAngleY>" + vo.robotAngle.y
							+ "</RobotAngleY>\n";
					xml += "\t\t<Conf>" + vo.confidence + "</Conf>\n";
					if (key == Ball) {
						xml += "\t\t<Distance>"
								+ ((BallVisualObject) vo).distance
								+ "</Distance>\n";
					} else if (key == BlueGoal || key == YellowGoal) {
						xml += "\t\t<Distance>"
								+ ((GoalVisualObject) vo).distance
								+ "</Distance>\n";
					}

					xml += "\t</VisualObject>\n";
				}
				// xml += "</Values>\n";

				w.print(xml);
				synchronized (lock) {
					lock.notifyAll();
				}
			}
		};

		String xml = "";
		xml += "\t<OtherValues name=\"RobotID\">\n";
		xml += "<Value>" + robotContext.getRobotId() + "</Value>";
		xml += "\t</OtherValues>\n";

		xml += "\t<OtherValues name=\"State\">\n";
		xml += "<Value>" + robotContext.getStrategy().getGameState().name()
				+ "</Value>";
		xml += "\t</OtherValues>\n";
		xml += "\t<OtherValues name=\"Penalize\">\n";
		if (robotContext.getStrategy().isPenalized())
			xml += "<Value>" + "Penalized->"
					+ robotContext.getStrategy().isPenalized() + "</Value>";
		else
			xml += "<Value>" + "UnPenalized" + "</Value>";
		xml += "\t</OtherValues>\n";
		xml += "\t<OtherValues name=\"Role\">\n";
		xml += "<Value>" + robotContext.getStrategy().getRole().name()
				+ "</Value>";
		xml += "\t</OtherValues>\n";
		xml += "\t<OtherValues name=\"Team\">\n";
		xml += "<Value>" + robotContext.getStrategy().getTeam().name()
				+ "</Value>";
		xml += "\t</OtherValues>\n";
		xml += "\t<OtherValues name=\"Scheduler\">\n";
		xml += "<Value>" + robotContext.getStrategy().getScheduler().getName()
				+ "</Value>";
		xml += "\t</OtherValues>\n";
		xml += "\t<OtherValues name=\"Task\">\n";
		try {
			xml += "<Value>"
					+ robotContext.getStrategy().getScheduler()
							.getCurrentTask().getName() + "</Value>";
		} catch (NullPointerException e) {
			xml += "<Value>" + "N/A" + "</Value>";
		}
		xml += "\t</OtherValues>\n";

		SensorContext sensor = robotContext.getSensor().create();
		robotContext.getSensor().update(sensor);
		for (Switch sw : Switch.values()) {
			xml += "\t<OtherValues name=\"" + sw.name() + "Sensor\">\n";
			xml += "<Value>" + !sensor.getSwitch(sw) + "</Value>";
			xml += "\t</OtherValues>\n";
		}

		w.print(xml);

		VisualCortex vc = robotContext.getVision();
		try {
			vc.addEventListener(listener);
			synchronized (lock) {
				lock.wait();

				// String xml;
				xml = "</Values>\n";
				w.print(xml);
			}
		} catch (InterruptedException e) {
		} finally {
			vc.removeEventListener(listener);
		}
		return;
	}
}
