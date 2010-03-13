/**
 *
 */
package jp.ac.fit.asura.nao.glue;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.misc.AttributesImpl;
import jp.ac.fit.asura.nao.misc.Kinematics;
import jp.ac.fit.asura.nao.misc.MatrixUtils;
import jp.ac.fit.asura.nao.misc.SingularPostureException;
import jp.ac.fit.asura.nao.misc.XMLWriter;
import jp.ac.fit.asura.nao.physical.Robot;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.sensation.FrameState;
import jp.ac.fit.asura.nao.sensation.SomaticContext;
import jp.ac.fit.asura.vecmathx.GfVector;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author sey
 *
 */
public class KinematicsServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(KinematicsServlet.class);
	private RobotContext robotContext;

	public KinematicsServlet(RobotContext context) {
		this.robotContext = context;
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		processRequest(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		processRequest(request, response);
	}

	protected void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document document = null;
		try {
			builder = dbf.newDocumentBuilder();
			document = builder.parse(request.getInputStream());

			Element root = document.getDocumentElement();
			NodeList list = root.getElementsByTagName("ProcessIK");
			Element item = (Element) list.item(0);

			Element targetElement = (Element) item.getElementsByTagName(
					"FrameState").item(0);
			Element posElement = (Element) targetElement.getElementsByTagName(
					"position").item(0);
			Element rotElement = (Element) targetElement.getElementsByTagName(
					"rotation").item(0);

			String frameName = targetElement.getAttribute("name");
			float x = Float.valueOf(posElement.getAttribute("x"));
			float y = Float.valueOf(posElement.getAttribute("y"));
			float z = Float.valueOf(posElement.getAttribute("z"));
			float pitch = Float.valueOf(rotElement.getAttribute("pitch"));
			float yaw = Float.valueOf(rotElement.getAttribute("yaw"));
			float roll = Float.valueOf(rotElement.getAttribute("roll"));

			Frames frame = Frames.valueOf(frameName);

			Robot robot = robotContext.getSensoryCortex().getRobot();
			FrameState target = new FrameState(robot.get(frame));
			Vector3f pyr = new Vector3f(pitch, yaw, roll);
			MatrixUtils.pyr2rot(pyr, target.getBodyRotation());
			target.getBodyPosition().set(x, y, z);

			GfVector weight = new GfVector(6);
			NodeList nodes = item.getElementsByTagName("WeightVector");
			Element weightElement = (Element) nodes.item(0);
			weight
					.setElement(0, Float.valueOf(weightElement
							.getAttribute("x")));
			weight
					.setElement(1, Float.valueOf(weightElement
							.getAttribute("y")));
			weight
					.setElement(2, Float.valueOf(weightElement
							.getAttribute("z")));
			weight.setElement(3, Float.valueOf(weightElement
					.getAttribute("pitch")));
			weight.setElement(4, Float.valueOf(weightElement
					.getAttribute("yaw")));
			weight.setElement(5, Float.valueOf(weightElement
					.getAttribute("roll")));

			// log.info(target.getBodyPosition());
			// log.info(target.getBodyRotation());
			// log.info(weight);

			SomaticContext sc = new SomaticContext(robot);
			XMLWriter w = new XMLWriter(response.getWriter());
			try {
				Kinematics.calculateInverse(sc, Frames.Body, target, weight);
			} catch (SingularPostureException e) {
				log.error("", e);
				w.startDocument();
				w.startElement("exception");
				if (e.getMessage() != null)
					w.characters(e.getMessage());
				else
					w.characters(e.toString());
				w.endElement();
				w.endDocument();
				return;
			}
			Frames[] frames = robot.findRoute(Frames.Body, target.getId());

			w.startDocument();
			w.startElement("ResultIK");
			for (Frames f : frames) {
				FrameState fs = sc.get(f);
				w.startElement("FrameState", new AttributesImpl("name", fs
						.getId()));

				// bodyPosition
				Vector3f pos = fs.getBodyPosition();
				w.emptyElement("position", new AttributesImpl("x", pos.x, "y",
						pos.y, "z", pos.z));

				// bodyRotation (Pitch-Yaw-Roll表現)
				Matrix3f rot = fs.getBodyRotation();
				MatrixUtils.rot2pyr(rot, pyr);
				w.emptyElement("rotation", new AttributesImpl("pitch", pyr.x,
						"yaw", pyr.y, "roll", pyr.z));

				w.emptyElement("angle", new AttributesImpl("min", fs.getFrame()
						.getMinAngle(), "max", fs.getFrame().getMaxAngle(),
						"value", fs.getAngle()));

				w.endElement("FrameState");
			}
			w.endElement("ResultIK");
			w.endDocument();
			return;
		} catch (ParserConfigurationException e) {
			log.error("", e);
		} catch (SAXException e) {
			log.error("", e);
		}
	}
}
