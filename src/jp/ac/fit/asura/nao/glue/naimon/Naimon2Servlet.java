/**
 *
 */
package jp.ac.fit.asura.nao.glue.naimon;

import static jp.ac.fit.asura.nao.vision.VisualParam.Boolean.USE_HOUGH;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.List;
import java.util.Queue;
import java.util.zip.DeflaterOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.Image;
import jp.ac.fit.asura.nao.MotionFrameContext;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.SensorContext;
import jp.ac.fit.asura.nao.Switch;
import jp.ac.fit.asura.nao.Camera.PixelFormat;
import jp.ac.fit.asura.nao.event.VisualEventListener;
import jp.ac.fit.asura.nao.localization.Localization;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.localization.WorldObjects;
import jp.ac.fit.asura.nao.localization.self.MonteCarloLocalization;
import jp.ac.fit.asura.nao.localization.self.SelfLocalization;
import jp.ac.fit.asura.nao.localization.self.MonteCarloLocalization.Candidate;
import jp.ac.fit.asura.nao.misc.AttributesImpl;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.misc.MatrixUtils;
import jp.ac.fit.asura.nao.misc.Pixmap;
import jp.ac.fit.asura.nao.misc.XMLWriter;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.FrameState;
import jp.ac.fit.asura.nao.sensation.SomaticContext;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.permanent.BallTrackingTask;
import jp.ac.fit.asura.nao.vision.GCD;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualCortex;
import jp.ac.fit.asura.nao.vision.VisualObjects;
import jp.ac.fit.asura.nao.vision.perception.BallVisualObject;
import jp.ac.fit.asura.nao.vision.perception.BlobVision;
import jp.ac.fit.asura.nao.vision.perception.GoalVisualObject;
import jp.ac.fit.asura.nao.vision.perception.HoughVision;
import jp.ac.fit.asura.nao.vision.perception.RobotVisualObject;
import jp.ac.fit.asura.nao.vision.perception.VisualObject;
import jp.ac.fit.asura.nao.vision.perception.BlobVision.Blob;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

/**
 *
 * Naimon2ServletのDOMを使わないバージョン.
 *
 * 名前がださいので，問題ないならNaimon2Servletに統合しましょう.
 *
 * @author $Author: $
 *
 * @version $Id: $
 *
 */
public class Naimon2Servlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(Naimon2Servlet.class);
	private RobotContext robotContext;

	private static final int BLOB_THRESHOLD_DEFAULT = 10;
	private int blobThreshold;
	private boolean isCamImage = false;

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

		String param = null;
		param = req.getParameter("blob_threshold");
		if (param != null) {
			blobThreshold = Integer.parseInt(param);
		} else {
			blobThreshold = BLOB_THRESHOLD_DEFAULT;
		}
		param = req.getParameter("camera_image");
		if (param != null && param.equals("true")) {
			isCamImage = true;
		} else {
			isCamImage = false;
		}

		final PrintWriter pw = resp.getWriter();
		final VisualCortex vc = robotContext.getVision();
		final Object lock = new Object();

		// ルートノードを作成
		final XMLWriter w = new XMLWriter(pw);

		VisualEventListener listener = new VisualEventListener() {
			@Override
			public void updateVision(VisualContext context) {
				try {
					long beginTime = System.nanoTime();

					w.startDocument();
					w.startElement("NaimonFrame");
					// Visionエレメントを追加
					writeVisionElement(w, context);

					// WorldObjectsエレメントを追加
					writeWorldObjectsElement(w, context);

					// Localizationエレメントを追加
					writeLocalizationElement(w, context);

					// Valuesエレメントを追加
					writeValuesElement(w, context);

					// CamImageエレメントを追加
					if (isCamImage)
						writeCamImageElement(w, context);
					writeSomaticContextElement(w, context);
					w.endElement("NaimonFrame");
					w.endDocument();
					long endTime = System.nanoTime();

					long time = endTime - beginTime;
					// だいたい4.7msぐらいかかる(Core2Duo 2.26 GHz)
					// DOMだと9.7msぐらいかかる
					log.trace("elapsedTime " + time / 1000000.0 + " [ms]");
				} catch (SAXException e) {
					log.error("", e);
				}

				synchronized (lock) {
					lock.notifyAll();
				}
			}
		};

		try {
			vc.addEventListener(listener);
			synchronized (lock) {
				lock.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			vc.removeEventListener(listener);
		}

		return;
	}

	private void writeSomaticContextElement(XMLWriter w, VisualContext context)
			throws SAXException {
		w.startElement("SomaticContext");
		MotionFrameContext mc = context.getFrameContext().getMotionFrame();
		SomaticContext sc = mc.getSomaticContext();

		{
			// 現在の姿勢
			w.startElement("Robot");

			w.emptyElement("position", new AttributesImpl("y", sc
					.getBodyHeight()));

			Matrix3f rot = sc.getBodyPosture();
			Vector3f pyr = new Vector3f();
			MatrixUtils.rot2pyr(rot, pyr);
			w.emptyElement("rotation", new AttributesImpl("pitch", pyr.x,
					"yaw", pyr.y, "roll", pyr.z));

			Vector3f com = sc.getCenterOfMass();
			w.emptyElement("comPosition", new AttributesImpl("x", com.x, "y",
					com.y, "z", com.z));

			w.endElement("Robot");
		}

		for (Frames f : Frames.values()) {
			FrameState fs = sc.get(f);
			w
					.startElement("FrameState", new AttributesImpl("name", fs
							.getId()));

			// bodyPosition
			Vector3f pos = fs.getBodyPosition();
			w.emptyElement("position", new AttributesImpl("x", pos.x, "y",
					pos.y, "z", pos.z));

			// bodyRotation (Pitch-Yaw-Roll表現)
			Matrix3f rot = fs.getBodyRotation();
			Vector3f pyr = new Vector3f();
			MatrixUtils.rot2pyr(rot, pyr);
			w.emptyElement("rotation", new AttributesImpl("pitch", pyr.x,
					"yaw", pyr.y, "roll", pyr.z));

			w.emptyElement("angle", new AttributesImpl("min", fs.getFrame()
					.getMinAngle(), "max", fs.getFrame().getMaxAngle(),
					"value", fs.getAngle()));

			w.endElement("FrameState");
		}
		w.endElement("SomaticContext");
	}

	private void writeCamImageElement(XMLWriter w, VisualContext context)
			throws SAXException {
		Image image = context.image;
		byte[] yvuPlane = new byte[image.getWidth() * image.getHeight() * 3];
		if (image.getPixelFormat() == PixelFormat.RGB444) {
			GCD.rgb2yvu(image.getIntBuffer(), yvuPlane);
		} else if (image.getPixelFormat() == PixelFormat.YUYV) {
			GCD.yuyv2yvu(image.getByteBuffer(), yvuPlane);
		} else {
			assert false;
			yvuPlane = null;
		}
		Pixmap ppm = new Pixmap(yvuPlane, image.getWidth(), image.getHeight(),
				255);
		String encodedImage;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DeflaterOutputStream dout = new DeflaterOutputStream(bout);
		try {
			dout.write(ppm.getData());
			dout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		encodedImage = Base64.encode(bout.toByteArray());

		AttributesImpl attrs = new AttributesImpl();
		attrs.addAttribute("width", image.getWidth());
		attrs.addAttribute("height", image.getHeight());
		attrs.addAttribute("frame", context.getFrameContext().getFrame());
		attrs.addAttribute("length", ppm.getData().length);
		w.startElement("CamImage", attrs);
		w.characters(encodedImage);
		w.endElement("CamImage");
	}

	/**
	 * Visionのエレメントノードを構成します
	 *
	 * @param context
	 * @return
	 */
	private void writeVisionElement(XMLWriter w, VisualContext context)
			throws SAXException {
		w.startElement("Vision");

		AttributesImpl gcdAttrs = new AttributesImpl();
		gcdAttrs.addAttribute("width", context.image.getWidth());
		gcdAttrs.addAttribute("height", context.image.getHeight());
		gcdAttrs.addAttribute("length", context.gcdPlane.length);
		// GCDエレメントを加える
		w.startElement("GCD", gcdAttrs);

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
		w.characters(Base64.encode(bout.toByteArray()));

		// GCDエレメント終わり
		w.endElement("GCD");

		if (context.getParam(USE_HOUGH)) {
			// USE_HOUGHが有効ならHoughエレメントを加える
			AttributesImpl houghAttrs = new AttributesImpl();
			houghAttrs.addAttribute("width", String
					.valueOf(HoughVision.RHO_MAX));
			houghAttrs.addAttribute("height", String
					.valueOf(HoughVision.THETA_MAX));
			houghAttrs.addAttribute("length", String
					.valueOf(context.houghPlane.length));
			// Houghエレメントを加える
			w.startElement("Hough", houghAttrs);

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
			w.characters(Base64.encode(bout2.toByteArray()));

			w.endElement("Hough");
		}

		// Blobsエレメントを加える
		writeBlobsElement(w, context, GCD.cORANGE, blobThreshold);
		writeBlobsElement(w, context, GCD.cCYAN, blobThreshold);
		writeBlobsElement(w, context, GCD.cYELLOW, blobThreshold);
		writeBlobsElement(w, context, GCD.cRED, blobThreshold);
		writeBlobsElement(w, context, GCD.cBLUE, blobThreshold);
		writeBlobsElement(w, context, GCD.cWHITE, blobThreshold);

		// VisualObjectsエレメントを加える
		writeVisualObjectsElement(w, context);
		w.endElement("Vision");
	}

	/**
	 *
	 * @param context
	 * @return
	 */
	private void writeLocalizationElement(XMLWriter w, VisualContext context)
			throws SAXException {
		w.startElement("Localization");

		Localization loc = robotContext.getLocalization();

		// World Objects
		for (WorldObjects e : WorldObjects.values()) {
			AttributesImpl attrs = new AttributesImpl();
			WorldObject wo = loc.get(e);
			attrs.addAttribute("type", String.valueOf(e.ordinal()));
			attrs.addAttribute("worldX", String.valueOf(wo.getWorldX()));
			attrs.addAttribute("worldY", String.valueOf(wo.getWorldY()));
			attrs.addAttribute("distance", String.valueOf(wo.getDistance()));
			attrs.addAttribute("heading", String.valueOf(wo.getHeading()));
			attrs
					.addAttribute("confidence", String.valueOf(wo
							.getConfidence()));
			attrs.addAttribute("yaw", String.valueOf(wo.getYaw()));

			w.emptyElement("WorldObject", attrs);
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
				Candidate c = candidates[i];
				w.emptyElement("Candidates", new AttributesImpl("x", c.x, "y",
						c.y, "h", c.h, "w", c.w));
			}
		}

		w.endElement("Localization");
	}

	/**
	 *
	 * @param context
	 * @return
	 * @throws SAXException
	 */
	private void writeWorldObjectsElement(XMLWriter w, VisualContext context)
			throws SAXException {
		w.startElement("WorldObjects");

		Localization loc = robotContext.getLocalization();
		for (WorldObjects key : WorldObjects.values()) {
			WorldObject wo = loc.get(key);

			NumberFormat f = NumberFormat.getInstance();
			f.setMaximumFractionDigits(2);
			AttributesImpl attrs = new AttributesImpl(7);
			attrs.addAttribute("name", wo.getType());
			attrs.addAttribute("X", wo.getX());
			attrs.addAttribute("Y", wo.getY());
			attrs.addAttribute("Heading", f.format(wo.getHeading()));
			attrs.addAttribute("Yaw", f.format(wo.getYaw()));
			attrs.addAttribute("Confidence", wo.getConfidence());
			attrs.addAttribute("Distance", wo.getDistance());
			w.emptyElement("Item", attrs);
		}
		w.endElement("WorldObjects");
	}

	/**
	 *
	 * @param context
	 * @return
	 */
	private void writeVisualObjectsElement(XMLWriter w, VisualContext context)
			throws SAXException {
		w.startElement("VisualObjects");

		for (VisualObjects key : VisualObjects.values()) {
			VisualObject vo = context.get(key);

			NumberFormat f = NumberFormat.getInstance();
			f.setMaximumFractionDigits(2);

			AttributesImpl attrs = new AttributesImpl();
			attrs.addAttribute("name", vo.getType().toString());
			attrs.addAttribute("CenterX", String.valueOf(vo.center.x));
			attrs.addAttribute("CenterY", String.valueOf(vo.center.y));
			attrs.addAttribute("AngleX", f.format(MathUtils
					.toDegrees(vo.angle.x)));
			attrs.addAttribute("AngleY", f.format(MathUtils
					.toDegrees(vo.angle.y)));
			attrs.addAttribute("RobotAngleX", f.format(MathUtils
					.toDegrees(vo.robotAngle.x)));
			attrs.addAttribute("RobotAngleY", f.format(MathUtils
					.toDegrees(vo.robotAngle.y)));
			attrs.addAttribute("Confidence", String.valueOf(vo.confidence));
			if (key == VisualObjects.Ball) {
				attrs
						.addAttribute("Distance",
								((BallVisualObject) vo).distance);
			} else if (key == VisualObjects.RedNao
					|| key == VisualObjects.BlueNao) {
				attrs.addAttribute("Distance",
						((RobotVisualObject) vo).distance);
			} else if (key == VisualObjects.BlueGoal
					|| key == VisualObjects.YellowGoal) {
				attrs
						.addAttribute("Distance",
								((GoalVisualObject) vo).distance);
			}

			w.startElement("Item", attrs);

			if (key == VisualObjects.BlueGoal
					|| key == VisualObjects.YellowGoal) {
				Polygon p = vo.polygon;
				for (int i = 0; i < p.npoints; i++) {
					w.emptyElement("Polygon", new AttributesImpl("x",
							p.xpoints[i], "y", p.ypoints[i]));
				}
			}

			if (vo.confidence > 0) {
				Rectangle r = vo.area;
				w.emptyElement("Polygon", new AttributesImpl("x", r.x, "y",
						r.y, "width", r.width, "height", r.height));
			}

			w.endElement("Item");
		}

		w.endElement("VisualObjects");
	}

	/**
	 *
	 * @param context
	 * @param colorIndex
	 * @param threshold
	 * @return
	 * @throws SAXException
	 */
	private void writeBlobsElement(XMLWriter w, VisualContext context,
			byte colorIndex, int threshold) throws SAXException {
		w.startElement("Blobs", new AttributesImpl("colorIndex", colorIndex,
				"threshold", threshold));

		List<Blob> list = context.blobVision.findBlobs(colorIndex,
				BlobVision.MAX_BLOBS, threshold);

		for (Blob b : list) {
			w.emptyElement("Blob", new AttributesImpl("xmin", b.xmin, "xmax",
					b.xmax, "ymin", b.ymin, "ymax", b.ymax));
		}

		w.endElement("Blobs");
	}

	/**
	 *
	 * @return
	 * @throws SAXException
	 */
	private void writeValuesElement(XMLWriter w, VisualContext context)
			throws SAXException {
		w.startElement("Values");

		// Frame Count
		w.emptyElement("Item", new AttributesImpl("name", "FrameCount",
				"value", context.getFrameContext().getFrame()));

		// Robot ID
		w.emptyElement("Item", new AttributesImpl("name", "RobotID", "value",
				robotContext.getRobotId()));

		// Game status
		w.emptyElement("Item", new AttributesImpl("name", "GameStatus",
				"value", robotContext.getStrategy().getGameState()));

		// Penalized
		w.emptyElement("Item", new AttributesImpl("name", "Penalized", "value",
				robotContext.getStrategy().isPenalized()));

		// Current team color
		w.emptyElement("Item", new AttributesImpl("name", "Team", "value",
				robotContext.getStrategy().getTeam()));

		// Current role
		w.emptyElement("Item", new AttributesImpl("name", "Role", "value",
				robotContext.getStrategy().getRole()));

		// Current Scheduler
		w.emptyElement("Item", new AttributesImpl("name", "Scheduler", "value",
				robotContext.getStrategy().getScheduler().getName()));

		// Current task
		String task = "N/A";
		try {
			task = robotContext.getStrategy().getScheduler().getCurrentTask()
					.getName();
		} catch (NullPointerException e) {
		}
		w.emptyElement("Item", new AttributesImpl("name", "CurrentTask",
				"value", task));

		// Current task TTL
		int ttl = -1;
		try {
			ttl = robotContext.getStrategy().getScheduler().getTTL();
		} catch (NullPointerException e) {
		}
		w.emptyElement("Item", new AttributesImpl("name", "TimeToLive",
				"value", ttl));

		// NextTask
		task = "N/A";
		try {
			Queue<Task> queue = robotContext.getStrategy().getScheduler()
					.getQueue();
			if (queue.size() > 0) {
				task = queue.peek().getName();
			}
		} catch (NullPointerException e) {
		}
		w.emptyElement("Item", new AttributesImpl("name", "NextTask", "value",
				task));

		// Current tracking mode
		BallTrackingTask tracking = (BallTrackingTask) robotContext
				.getStrategy().getTaskManager().find("BallTracking");
		if (tracking != null) {
			w.emptyElement("Item", new AttributesImpl("name", "TrackingMode",
					"value", tracking.getModeName()));
		}

		// Current Motion
		String motion = "null";
		try {
			motion = robotContext.getMotor().getCurrentMotion().getName();
		} catch (NullPointerException e) {
		}
		w.emptyElement("Item", new AttributesImpl("name", "CurrentMotion",
				"value", motion));

		// Sensors
		SensorContext sensor = context.getFrameContext().getMotionFrame()
				.getSensorContext();
		for (Switch sw : Switch.values()) {
			w.emptyElement("Item", new AttributesImpl("name", sw, "value",
					sensor.getSwitch(sw)));
		}
		w.endElement("Values");
	}

}
