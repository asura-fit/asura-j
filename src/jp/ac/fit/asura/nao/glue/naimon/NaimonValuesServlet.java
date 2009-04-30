/*
 * 作成日: 2009/04/11
 */
package jp.ac.fit.asura.nao.glue.naimon;

import static jp.ac.fit.asura.nao.vision.VisualObjects.Ball;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.event.VisualEventListener;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.strategy.StrategyContext;
import jp.ac.fit.asura.nao.vision.GCD;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualCortex;
import jp.ac.fit.asura.nao.vision.perception.BallVisualObject;
import jp.ac.fit.asura.nao.vision.perception.BlobVision;
import jp.ac.fit.asura.nao.vision.perception.VisualObject;
import jp.ac.fit.asura.nao.vision.perception.BlobVision.Blob;

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
		final VisualCortex vc = robotContext.getVision();

		final PrintWriter w = resp.getWriter();

		// 手始めにVisualObjectをXML形式に変換
		VisualContext vct = vc.getVisualContext();
		final VisualObject[] vos = { vct.get(Ball) };
		final String[] vosName = { "Ball" };
		String xml;
		xml = "<?xml version=\"1.0\" encoding=\"Shift_JIS\" ?>\n";
		for (int idx = 0; idx < vos.length; idx++) {
			xml += "<VisualObject name=\"" + vosName[idx] + "\">\n";
			xml += "\t<CenterX>" + vos[idx].center.x + "</CenterX>\n";
			xml += "\t<CenterY>" + vos[idx].center.y + "</CenterY>\n";
			xml += "\t<AngleX>" + vos[idx].angle.x + "</AngleX>\n";
			xml += "\t<AngleY>" + vos[idx].angle.y + "</AngleY>\n";
			xml += "\t<RobotAngleX>" + vos[idx].robotAngle.x
					+ "</RobotAngleX>\n";
			xml += "\t<RobotAngleY>" + vos[idx].robotAngle.y
					+ "</RobotAngleY>\n";
			xml += "\t<Conf>" + vos[idx].confidence + "</Conf>\n";
			xml += "</VisualObject>\n";
		}

		w.print(xml);

		return;
	}

}
