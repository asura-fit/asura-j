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
import jp.ac.fit.asura.nao.event.VisualEventListener;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualCortex;
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

		VisualEventListener listener = new VisualEventListener() {
			public void updateVision(VisualContext context) {
				final VisualObject[] vos = { context.get(Ball),
						context.get(BlueGoal), context.get(YellowGoal) };
				final String[] vosName = { "Ball", "BlueGoal", "YellowGoal" };
				String xml;
				xml = "<?xml version=\"1.0\" encoding=\"Shift_JIS\" ?>\n";

				xml += "<Values>\n";
				for (int idx = 0; idx < vos.length; idx++) {
					xml += "\t<VisualObject name=\"" + vosName[idx] + "\">\n";
					xml += "\t\t<CenterX>" + vos[idx].center.x + "</CenterX>\n";
					xml += "\t\t<CenterY>" + vos[idx].center.y + "</CenterY>\n";
					xml += "\t\t<AngleX>" + vos[idx].angle.x + "</AngleX>\n";
					xml += "\t\t<AngleY>" + vos[idx].angle.y + "</AngleY>\n";
					xml += "\t\t<RobotAngleX>" + vos[idx].robotAngle.x
							+ "</RobotAngleX>\n";
					xml += "\t\t<RobotAngleY>" + vos[idx].robotAngle.y
							+ "</RobotAngleY>\n";
					xml += "\t\t<Conf>" + vos[idx].confidence + "</Conf>\n";
					xml += "\t</VisualObject>\n";
				}
				xml += "</Values>\n";

				w.print(xml);
				synchronized (lock) {
					lock.notifyAll();
				}
			}
		};

		VisualCortex vc = robotContext.getVision();
		try {
			vc.addEventListener(listener);
			synchronized (lock) {
				lock.wait();
			}
		} catch (InterruptedException e) {
		} finally {
			vc.removeEventListener(listener);
		}
		return;
	}
}
