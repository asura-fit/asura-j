/**
 *
 */
package jp.ac.fit.asura.nao.glue.naimon;

import static jp.ac.fit.asura.nao.vision.VisualParam.Boolean.USE_HOUGH;

import java.awt.Polygon;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.List;
import java.util.zip.DeflaterOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.SensorContext;
import jp.ac.fit.asura.nao.Switch;
import jp.ac.fit.asura.nao.event.VisualEventListener;
import jp.ac.fit.asura.nao.localization.Localization;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.localization.WorldObjects;
import jp.ac.fit.asura.nao.localization.self.MonteCarloLocalization;
import jp.ac.fit.asura.nao.localization.self.SelfLocalization;
import jp.ac.fit.asura.nao.localization.self.MonteCarloLocalization.Candidate;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;
import jp.ac.fit.asura.nao.vision.GCD;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualCortex;
import jp.ac.fit.asura.nao.vision.VisualObjects;
import jp.ac.fit.asura.nao.vision.perception.BallVisualObject;
import jp.ac.fit.asura.nao.vision.perception.BlobVision;
import jp.ac.fit.asura.nao.vision.perception.GoalVisualObject;
import jp.ac.fit.asura.nao.vision.perception.HoughVision;
import jp.ac.fit.asura.nao.vision.perception.VisualObject;
import jp.ac.fit.asura.nao.vision.perception.BlobVision.Blob;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

/**
 * @author $Author: $
 *
 * @version $Id: $
 *
 */
public class Naimon2Servlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(Naimon2Servlet.class);
	private RobotContext robotContext;
	private Document document;
	private Element root;

	public Naimon2Servlet(RobotContext context) {
		this.robotContext = context;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		log
				.trace("request :" + req.getRemoteHost() + ":"
						+ req.getRemotePort());
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("text/xml; charset=UTF-8");
		req.setCharacterEncoding("UTF-8");

		final PrintWriter pw = resp.getWriter();
		final VisualCortex vc = robotContext.getVision();
		final Object lock = new Object();

		DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docbuilder = null;
		document = null;
		try {
			docbuilder = dbfactory.newDocumentBuilder();
			document = docbuilder.newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		// ルートノードを作成
		root = document.createElement("NaimonFrame");
		document.appendChild(root);

		VisualEventListener listener = new VisualEventListener() {
			@Override
			public void updateVision(VisualContext context) {

				// Visionエレメントを追加
				root.appendChild(buildVisionElement(context));

				// WorldObjectsエレメントを追加
				root.appendChild(buildWorldObjectsElement(context));

				// Localizationエレメントを追加
				root.appendChild(buildLocalizationElement(context));

				// Valuesエレメントを追加
				root.appendChild(buildValuesElement());

				synchronized (lock) {
					lock.notifyAll();
				}
			}
		};

		try {
			vc.addEventListener(listener);
			synchronized (lock) {
				lock.wait();

				TransformerFactory tfactory = TransformerFactory.newInstance();
				Transformer transformer = null;
				try {
					transformer = tfactory.newTransformer();
					transformer.transform(new DOMSource(document),
							new StreamResult(pw));
				} catch (TransformerException e) {
					e.printStackTrace();
				}

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			vc.removeEventListener(listener);
		}

		return;
	}

	/**
	 * Visionのエレメントノードを構成します
	 *
	 * @param context
	 * @return
	 */
	private Element buildVisionElement(VisualContext context) {
		Element vision = document.createElement("Vision");

		Element gcd = document.createElement("GCD");
		gcd.setAttribute("width", String.valueOf(context.image.getWidth()));
		gcd.setAttribute("height", String.valueOf(context.image.getHeight()));
		gcd.setAttribute("length", String.valueOf(context.gcdPlane.length));

		// gcdPlaneを圧縮
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DeflaterOutputStream dout = new DeflaterOutputStream(bout);
		try {
			dout.write(context.gcdPlane);
			dout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Base64エンコードする
		gcd.setTextContent(Base64.encode(bout.toByteArray()));

		// GCDエレメントを加える
		vision.appendChild(gcd);

		if (context.getParam(USE_HOUGH)) {
			// USE_HOUGHが有効ならHoughエレメントを加える
			Element hough = document.createElement("Hough");
			hough.setAttribute("width", String.valueOf(HoughVision.RHO_MAX));
			hough.setAttribute("height", String.valueOf(HoughVision.THETA_MAX));
			hough.setAttribute("length", String
					.valueOf(context.houghPlane.length));

			// houghPlaneを圧縮
			ByteArrayOutputStream bout2 = new ByteArrayOutputStream();
			DeflaterOutputStream dout2 = new DeflaterOutputStream(bout2);
			try {
				dout2.write(context.houghPlane);
				dout2.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			// Base64エンコードする
			hough.setTextContent(Base64.encode(bout2.toByteArray()));

			// Houghエレメントを加える
			vision.appendChild(hough);
		}

		// Blobsエレメントを加える
		int threshold = 5;
		vision.appendChild(buildBlobsElement(context, GCD.cORANGE, threshold));
		vision.appendChild(buildBlobsElement(context, GCD.cCYAN, threshold));
		vision.appendChild(buildBlobsElement(context, GCD.cYELLOW, threshold));
		vision.appendChild(buildBlobsElement(context, GCD.cWHITE, threshold));

		// VisualObjectsエレメントを加える
		vision.appendChild(buildVisualObjectsElement(context));

		return vision;
	}

	/**
	 *
	 * @param context
	 * @return
	 */
	private Element buildLocalizationElement(VisualContext context) {
		Element locElement = document.createElement("Localization");

		Localization loc = robotContext.getLocalization();

		// World Objects
		for (WorldObjects e : WorldObjects.values()) {
			Element woElement = document.createElement("WorldObject");
			WorldObject wo = loc.get(e);
			woElement.setAttribute("type", String.valueOf(e.ordinal()));
			woElement.setAttribute("worldX", String.valueOf(wo.getWorldX()));
			woElement.setAttribute("worldY", String.valueOf(wo.getWorldY()));
			woElement
					.setAttribute("distance", String.valueOf(wo.getDistance()));
			woElement.setAttribute("heading", String.valueOf(wo.getHeading()));
			woElement.setAttribute("confidence", String.valueOf(wo
					.getConfidence()));
			woElement.setAttribute("yaw", String.valueOf(wo.getYaw()));

			locElement.appendChild(woElement);
		}

		// SelfLocalization
		SelfLocalization self = loc.getSelf();
		if (self instanceof MonteCarloLocalization) {
			MonteCarloLocalization mcl = (MonteCarloLocalization) self;
			Candidate[] candidates = mcl.getCandidates();
			int size = 32; // samples;
			if (candidates.length < size)
				size = candidates.length;

			for (int i = 0; i < size; i++) {
				Element cElement = document.createElement("Candidates");
				Candidate c = candidates[i];
				cElement.setAttribute("x", String.valueOf(c.x));
				cElement.setAttribute("y", String.valueOf(c.y));
				cElement.setAttribute("h", String.valueOf(c.h));
				cElement.setAttribute("w", String.valueOf(c.w));

				locElement.appendChild(cElement);
			}
		}

		return locElement;
	}

	/**
	 *
	 * @param context
	 * @return
	 */
	private Element buildWorldObjectsElement(VisualContext context) {
		Element woElement = document.createElement("WorldObjects");
		Localization loc = robotContext.getLocalization();
		for (WorldObjects key : WorldObjects.values()) {
			Element wobj = document.createElement("Item");

			WorldObject wo = loc.get(key);

			NumberFormat f = NumberFormat.getInstance();
			f.setMaximumFractionDigits(2);
			wobj.setAttribute("name", wo.getType().name());
			wobj.setAttribute("X", String.valueOf(wo.getX()));
			wobj.setAttribute("Y", String.valueOf(wo.getY()));
			wobj.setAttribute("Heading", f.format(wo.getHeading()));
			wobj.setAttribute("Yaw", f.format(wo.getYaw()));
			wobj.setAttribute("Confidence", String.valueOf(wo.getConfidence()));
			wobj.setAttribute("Distance", String.valueOf(wo.getDistance()));

			woElement.appendChild(wobj);
		}

		return woElement;
	}

	/**
	 *
	 * @param context
	 * @return
	 */
	private Element buildVisualObjectsElement(VisualContext context) {
		Element voElement = document.createElement("VisualObjects");

		for (VisualObjects key : VisualObjects.values()) {
			Element vobj = document.createElement("Item");

			VisualObject vo = context.get(key);

			NumberFormat f = NumberFormat.getInstance();
			f.setMaximumFractionDigits(2);
			vobj.setAttribute("name", vo.getType().toString());
			vobj.setAttribute("CenterX", String.valueOf(vo.center.x));
			vobj.setAttribute("CenterY", String.valueOf(vo.center.y));
			vobj.setAttribute("AngleX", f.format(MathUtils
					.toDegrees(vo.angle.x)));
			vobj.setAttribute("AngleY", f.format(MathUtils
					.toDegrees(vo.angle.y)));
			vobj.setAttribute("RobotAngleX", f.format(MathUtils
					.toDegrees(vo.robotAngle.x)));
			vobj.setAttribute("RobotAngleY", f.format(MathUtils
					.toDegrees(vo.robotAngle.y)));
			vobj.setAttribute("Confidence", String.valueOf(vo.confidence));
			if (key == VisualObjects.Ball) {
				vobj.setAttribute("Distance", String
						.valueOf(((BallVisualObject) vo).distance));
			} else if (key == VisualObjects.BlueGoal
					|| key == VisualObjects.YellowGoal) {
				vobj.setAttribute("Distance", String
						.valueOf(((GoalVisualObject) vo).distance));
				Polygon p = vo.polygon;
				for (int i = 0; i < p.npoints; i++) {
					Element polygon = document.createElement("Polygon");
					polygon.setAttribute("x", String.valueOf(p.xpoints[i]));
					polygon.setAttribute("y", String.valueOf(p.ypoints[i]));
					vobj.appendChild(polygon);
				}
			}

			voElement.appendChild(vobj);
		}

		return voElement;
	}

	/**
	 *
	 * @param context
	 * @param colorIndex
	 * @param threshold
	 * @return
	 */
	private Element buildBlobsElement(VisualContext context, byte colorIndex,
			int threshold) {
		Element blobs = document.createElement("Blobs");
		blobs.setAttribute("colorIndex", String.valueOf(colorIndex));
		blobs.setAttribute("threshold", String.valueOf(threshold));

		List<Blob> list = context.blobVision.findBlobs(colorIndex,
				BlobVision.MAX_BLOBS, threshold);

		for (Blob b : list) {
			Element blob = document.createElement("Blob");
			blob.setAttribute("xmin", String.valueOf(b.xmin));
			blob.setAttribute("xmax", String.valueOf(b.xmax));
			blob.setAttribute("ymin", String.valueOf(b.ymin));
			blob.setAttribute("ymax", String.valueOf(b.ymax));
			blobs.appendChild(blob);
		}

		return blobs;
	}

	/**
	 *
	 * @return
	 */
	private Element buildValuesElement() {
		Element values = document.createElement("Values");

		// Robot ID
		Element item;
		item = document.createElement("Item");
		item.setAttribute("name", "RobotID");
		item.setAttribute("value", String.valueOf(robotContext.getRobotId()));
		values.appendChild(item);

		// Game status
		item = document.createElement("Item");
		item.setAttribute("name", "GameStatus");
		item.setAttribute("value", robotContext.getStrategy().getGameState()
				.toString());
		values.appendChild(item);

		// Penalized
		item = document.createElement("Item");
		item.setAttribute("name", "Penalized");
		item.setAttribute("value", String.valueOf(robotContext.getStrategy()
				.isPenalized()));
		values.appendChild(item);

		// Current role
		item = document.createElement("Item");
		item.setAttribute("name", "Role");
		item.setAttribute("value", robotContext.getStrategy().getRole()
				.toString());
		values.appendChild(item);

		// Current team color
		item = document.createElement("Item");
		item.setAttribute("name", "Team");
		item.setAttribute("value", robotContext.getStrategy().getTeam()
				.toString());
		values.appendChild(item);

		// Current Scheduler
		item = document.createElement("Item");
		item.setAttribute("name", "Scheduler");
		item.setAttribute("value", robotContext.getStrategy().getScheduler()
				.getName());
		values.appendChild(item);

		// Current task
		item = document.createElement("Item");
		item.setAttribute("name", "CurrentTask");
		String task = "N/A";
		try {
			task = robotContext.getStrategy().getScheduler().getCurrentTask()
					.getName();
		} catch (NullPointerException e) {
		}
		item.setAttribute("value", task);
		values.appendChild(item);

		// Current Motion
		item = document.createElement("Item");
		item.setAttribute("name", "CurrentMotion");
		String motion = "null";
		try {
			motion = robotContext.getMotor().getCurrentMotion().getName();
		} catch (NullPointerException e) {
		}
		item.setAttribute("value", motion);
		values.appendChild(item);

		// Sensors
		SensorContext sensor = robotContext.getSensor().create();
		robotContext.getSensor().update(sensor);
		for (Switch sw : Switch.values()) {
			item = document.createElement("Item");
			item.setAttribute("name", sw.toString());
			item.setAttribute("value", String.valueOf(sensor.getSwitch(sw)));
			values.appendChild(item);
		}

		BallTrackingTask tracking = (BallTrackingTask) robotContext
				.getStrategy().getTaskManager().find("BallTracking");
		if (tracking != null) {
			String modeName = tracking.getModeName();
			item = document.createElement("Item");
			item.setAttribute("name", "Tracking Mode");
			item.setAttribute("value", modeName);
			values.appendChild(item);
		}
		
		return values;
	}

}
