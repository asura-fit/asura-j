/*
 * 作成日: 2008/05/05
 */
package jp.ac.fit.asura.nao.glue;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Vector3f;

import jp.ac.fit.asura.nao.Image;
import jp.ac.fit.asura.nao.Joint;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.VisualCycle;
import jp.ac.fit.asura.nao.VisualFrameContext;
import jp.ac.fit.asura.nao.Camera.CameraID;
import jp.ac.fit.asura.nao.Camera.CameraParam;
import jp.ac.fit.asura.nao.Camera.PixelFormat;
import jp.ac.fit.asura.nao.misc.MathUtils;
import jp.ac.fit.asura.nao.misc.Pixmap;
import jp.ac.fit.asura.nao.misc.TeeOutputStream;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.motion.MotorCortex;
import jp.ac.fit.asura.nao.motion.motions.CartesianMotion;
import jp.ac.fit.asura.nao.motion.motions.CompatibleMotion;
import jp.ac.fit.asura.nao.motion.motions.ForwardMotion;
import jp.ac.fit.asura.nao.motion.motions.LinerMotion;
import jp.ac.fit.asura.nao.motion.motions.RawMotion;
import jp.ac.fit.asura.nao.motion.motions.TimedMotion;
import jp.ac.fit.asura.nao.motion.motions.CartesianMotion.ChainFrame;
import jp.ac.fit.asura.nao.motion.motions.CartesianMotion.DataFrame;
import jp.ac.fit.asura.nao.physical.Robot;
import jp.ac.fit.asura.nao.physical.RobotFrame;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.strategy.Role;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.Team;
import jp.ac.fit.asura.nao.strategy.schedulers.Scheduler;
import jp.ac.fit.asura.nao.vision.GCD;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualParam;
import jscheme.JScheme;
import jsint.BacktraceException;
import jsint.Pair;
import jsint.U;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.net.TelnetAppender;

/**
 * @author sey
 *
 * @version $Id: SchemeGlue.java 717 2008-12-31 18:16:20Z sey $
 *
 */
public class SchemeGlue implements VisualCycle {
	private static final Logger log = Logger.getLogger(SchemeGlue.class);

	public enum InterpolationType {
		Raw(1), Liner(2), Compatible(3), Timed(4), TimedDeg(5), Cartesian(6);
		private final int id;

		InterpolationType(int id) {
			this.id = id;
		}

		public static InterpolationType valueOf(int id) {
			for (InterpolationType t : InterpolationType.values()) {
				if (t.id == id)
					return t;
			}
			return null;
		}
	}

	private JScheme js;
	private MotorCortex motor;
	private TinyHttpd httpd;
	private RobotContext rctx;

	private int saveImageInterval;

	private TeeOutputStream outputStreams;
	private TeeOutputStream errorStreams;

	/**
	 *
	 */
	public SchemeGlue() {
		js = new JScheme();
		httpd = new TinyHttpd();
		outputStreams = new TeeOutputStream(System.out);
		errorStreams = new TeeOutputStream(System.err);
		try {
			outputStreams.addStream(new FileOutputStream("output.log"));
		} catch (IOException e) {
			log.error("Can't open output.log");
		}
		try {
			errorStreams.addStream(new FileOutputStream("error.log"));
		} catch (IOException e) {
			log.error("Can't open error.log");
		}
		js.getEvaluator().setOutput(new PrintWriter(outputStreams));
		js.getEvaluator().setError(new PrintWriter(errorStreams));
	}

	@Override
	public void init(RobotContext context) {
		log.info("Init SchemeGlue.");
		this.rctx = context;
		motor = context.getMotor();

		// Declare global values
		js.setGlobalValue("glue", this);
		js.setGlobalValue("robot-context", rctx);

		// Declare joint definition
		for (Frames frame : Frames.values()) {
			js.setGlobalValue(frame.name(), frame.ordinal());
		}

		saveImageInterval = 0;

		httpd.init(rctx);
	}

	@Override
	public void start() {
		log.info("Start SchemeGlue.");

		try {
			log.debug(Charset.defaultCharset());
			ClassLoader cl = getClass().getClassLoader();
			InputStream is = cl.getResourceAsStream("init.scm");
			if (is == null)
				throw new FileNotFoundException("Can't get resource init.scm");
			Reader reader = new InputStreamReader(is, Charset.forName("UTF-8"));
			js.load(reader);
		} catch (BacktraceException e) {
			log.fatal("", e.getBaseException());
		} catch (IOException e) {
			log.fatal("", e);
		}
	}

	@Override
	public void step(VisualFrameContext context) {
		if (saveImageInterval != 0
				&& context.getFrame() % saveImageInterval == 0) {
			log.debug("save image.");
			VisualContext ctx = context.getVisualContext();
			Image image = ctx.image;

			byte[] yvuPlane = new byte[image.getWidth() * image.getHeight() * 3];
			if (image.getPixelFormat() == PixelFormat.RGB444) {
				GCD.rgb2yvu(image.getIntBuffer(), yvuPlane);
			} else if (image.getPixelFormat() == PixelFormat.YUYV) {
				GCD.yuyv2yvu(image.getByteBuffer(), yvuPlane);
			} else {
				assert false;
				yvuPlane = null;
			}
			Pixmap ppm = new Pixmap(yvuPlane, image.getWidth(), image
					.getHeight(), 255);
			try {
				ppm.write("snapshot/image" + context.getFrame() + ".ppm");
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

	@Override
	public void stop() {
		httpd.stop();
	}

	public Object getValue(String key) {
		return js.getGlobalValue(key);
	}

	public <T> T getValue(Class<T> clazz, String key) {
		Object o = js.getGlobalValue(key);
		return clazz.isInstance(o) ? (T) o : null;
	}

	public void setValue(String key, Object obj) {
		js.setGlobalValue(key, obj);
	}

	public void eval(String expression) {
		js.load(expression);
	}

	public void load(Reader reader) {
		js.load(reader);
	}

	public void glueStartHttpd(int port) {
		assert port > 0;
		if (httpd.isRunning()) {
			log.error("httpd is already running");
			return;
		}
		httpd.start(port);
	}

	public void glueStopHttpd() {
		if (!httpd.isRunning()) {
			log.error("httpd isn't running");
			return;
		}
		httpd.stop();
	}

	public void glueStartLogd(int port) {
		TelnetAppender tel = (TelnetAppender) Logger.getRootLogger()
				.getAppender("telnet");
		if (tel != null) {
			tel.close();
		} else {
			tel = new TelnetAppender();
			tel.setName("telnet");
			tel.setLayout(new PatternLayout("%d %5p %c{1} - %m%n"));
		}
		tel.setPort(port);
		tel.activateOptions();
		Logger.getRootLogger().addAppender(tel);
		log.info("logd started");
	}

	public void glueStopLogd() {
		log.info("logd is going to stop");
		TelnetAppender tel = (TelnetAppender) Logger.getRootLogger()
				.getAppender("telnet");
		if (tel != null)
			tel.close();
	}

	public void glueSetSaveImageInterval(int interval) {
		saveImageInterval = interval;
	}

	public void glueSetLogLevel(String loggerName, String levelType) {
		Level level = Level.toLevel(levelType);

		if (level == null) {
			log.error("Illegal level:" + levelType);
			return;
		}

		Logger logger = Logger.getLogger(loggerName);

		if (logger == null) {
			log.error("Logger not found. :" + loggerName);
			return;
		}

		logger.setLevel(level);
	}

	public void glueSetParam(VisualParam.Boolean key, boolean value) {
		log.info("glueSetParam key: " + key + " value: " + value);
		rctx.getVision().setParam(key, value);
	}

	public void glueSetParam(VisualParam.Float key, float value) {
		log.info("glueSetParam key: " + key + " value: " + value);
		rctx.getVision().setParam(key, value);
	}

	public void glueSetParam(VisualParam.Int key, int value) {
		log.info("glueSetParam key: " + key + " value: " + value);
		rctx.getVision().setParam(key, value);
	}

	public void mcRegistmotion(int id, Object obj) {
		if (obj instanceof Motion) {
			Motion motion = (Motion) obj;
			motion.setId(id);
			motor.registMotion(motion);
		}
	}

	public void mcRegistmotion(int id, String name, int interpolationType,
			Object[] scmArgs) {
		try {
			InterpolationType type = InterpolationType
					.valueOf(interpolationType);
			log.trace("mcRegistMotion" + id + ", " + name + "," + type + "("
					+ interpolationType + ")" + Arrays.toString(scmArgs));
			if (id < 0)
				throw new IllegalArgumentException(
						"id must be positive or zero.");
			if (type == null)
				throw new IllegalArgumentException("invalid interpolationType.");

			Motion motion;
			// 引数の型を変換してモーションを作成
			switch (type) {
			case Raw: {
				motion = new RawMotion(matrix2float(scmArgs));
				break;
			}
			case Compatible:
			case Liner:
			case Timed:
			case TimedDeg: {
				if (scmArgs.length != 2)
					throw new IllegalArgumentException("args must be 2.");
				Object[] frames = (Object[]) scmArgs[0];
				Object[] frameStep = (Object[]) scmArgs[1];

				if (frames.length != frameStep.length)
					throw new IllegalArgumentException(
							"args length must be equal. but " + frames.length
									+ " and " + frameStep.length);

				float[] a1 = matrix2float(frames);
				int[] a2 = array2int(frameStep);

				switch (type) {
				case Compatible:
					if (id == Motions.MOTION_YY_FORWARD) {
						motion = new ForwardMotion(a1, a2);
					} else {
						motion = new CompatibleMotion(a1, a2);
					}
					break;
				case TimedDeg:
					toRad(a1);
				case Timed:
					motion = new TimedMotion(a1, a2);
					break;
				case Liner:
				default:
					motion = new LinerMotion(a1, a2);
				}
				log.debug("new motion " + name + " id: " + id
						+ " is registered. frames: " + frames.length);
				break;
			}
			case Cartesian: {
				if (scmArgs.length != 2)
					throw new IllegalArgumentException("args must be 2.");
				Object[] frames = (Object[]) scmArgs[0];
				Object[] frameStep = (Object[]) scmArgs[1];
				// Object[] options = (Object[]) scmArgs[2];

				if (frames.length != frameStep.length)
					throw new IllegalArgumentException(
							"args length must be equal. but " + frames.length
									+ " and " + frameStep.length);

				List<DataFrame> args = new ArrayList<DataFrame>();
				int[] a2 = array2int(frameStep);
				for (int i = 0; i < frames.length; i++) {
					DataFrame data = new DataFrame();
					data.time = a2[i];
					data.chains = new ArrayList<ChainFrame>();
					Object[] frame = (Object[]) frames[i];
					for (Object chainFrameObj : frame) {
						ChainFrame e = new ChainFrame();
						Object[] chainFrame = (Object[]) chainFrameObj;
						if (chainFrame.length != 2)
							throw new IllegalArgumentException(
									"chainFrame length must be 2.");
						Object[] pos = (Object[]) chainFrame[1];
						// Object weight = chainFrame[2];
						if (pos.length != 6)
							throw new IllegalArgumentException(
									"posture length must be 6.");
						Frames chain = Frames.valueOf(chainFrame[0].toString());
						Vector3f v1 = new Vector3f();
						v1.x = Float.parseFloat(pos[0].toString());
						v1.y = Float.parseFloat(pos[1].toString());
						v1.z = Float.parseFloat(pos[2].toString());
						Vector3f v2 = new Vector3f();
						v2.x = Float.parseFloat(pos[3].toString());
						v2.y = Float.parseFloat(pos[4].toString());
						v2.z = Float.parseFloat(pos[5].toString());
						v2.scale(1 / (180.0f / MathUtils.PIf));
						e.chainId = chain;
						e.position = v1;
						e.postureYpr = v2;
						e.positionWeight = new Vector3f(1, 1, 1);
						e.postureWeight = new Vector3f(1, 1, 1);
						data.chains.add(e);
					}
					args.add(data);
				}
				motion = new CartesianMotion(
						rctx.getSensoryCortex().getRobot(), args);
				log.debug("new motion " + name + " id: " + id
						+ " is registered. frames: " + frames.length);
				break;
			}
			default:
				assert false;
				motion = null;
			}
			motion.setName(name);
			motion.setId(id);
			motor.registMotion(motion);
		} catch (NumberFormatException e) {
			log.error("", e);
		} catch (ClassCastException e) {
			log.error("", e);
		} catch (IllegalArgumentException e) {
			log.error("", e);
		} catch (Exception e) {
			log.fatal("", e);
			assert false;
		}
	}

	public void mcMakemotion(int id) {
		if (!motor.hasMotion(id)) {
			log.error("Motion " + id + " notfound.");
			return;
		}
		log.info("makemotion:" + id);
		motor.makemotion(id);
	}

	public void mcMotorPower(float power) {
		log.info("Motor Power " + power * 100 + "%");

		rctx.getEffector().setPower(power);
	}

	public void mcJointPower(String joint, float power) {
		log.info(joint + " Power " + power * 100 + "%");

		Joint j = Joint.valueOf(joint);
		if (j == null) {
			log.error("mcJointPower: Invalid Joint " + j);
			return;
		}
		rctx.getEffector().setPower(j, power);
	}

	public RobotFrame scCreateFrame(int frameId, Pair list) {
		Frames frame = (Frames.values())[frameId];
		RobotFrame rf = new RobotFrame(frame);
		assert U.isList(list.getFirst());

		log.debug("create new frame " + frame);

		for (Object o : U.listToVector(list.getFirst())) {
			assert o instanceof Pair;
			Pair pair = (Pair) o;
			String str = pair.getFirst().toString();
			if (str.equals("translation")) {
				Object[] vec = U.listToVector(pair.getRest());
				rf.getTranslation().x = Float.parseFloat(vec[0].toString());
				rf.getTranslation().y = Float.parseFloat(vec[1].toString());
				rf.getTranslation().z = Float.parseFloat(vec[2].toString());
				log.trace("set " + frame + " translation "
						+ rf.getTranslation());
			} else if (str.equals("axis")) {
				Object[] vec = U.listToVector(pair.getRest());
				rf.getAxis().x = Float.parseFloat(vec[0].toString());
				rf.getAxis().y = Float.parseFloat(vec[1].toString());
				rf.getAxis().z = Float.parseFloat(vec[2].toString());
				log.trace("set " + frame + " axis " + rf.getAxis());
			} else if (str.equals("max")) {
				float max = Float.parseFloat(pair.getRest().toString());
				rf.setMaxAngle(max);
				log.trace("set " + frame + " max angle " + max);
			} else if (str.equals("min")) {
				float min = Float.parseFloat(pair.getRest().toString());
				rf.setMinAngle(min);
				log.trace("set " + frame + " min angle " + min);
			} else if (str.equals("mass")) {
				float mass = Float.parseFloat(pair.getRest().toString());
				rf.setMass(mass);
				log.trace("set " + frame + " mass " + mass);
			} else if (str.equals("centerOfMass")) {
				Object[] vec = U.listToVector(pair.getRest());
				rf.getCenterOfMass().x = Float.parseFloat(vec[0].toString());
				rf.getCenterOfMass().y = Float.parseFloat(vec[1].toString());
				rf.getCenterOfMass().z = Float.parseFloat(vec[2].toString());
				log.trace("set " + frame + " com " + rf.getCenterOfMass());
			} else if (str.equals("angle")) {
				float angle = Float.parseFloat(pair.getRest().toString());
				rf.getAxis().angle = angle;
				log.trace("set " + frame + " angle " + angle);
			} else {
				log.warn("unknown parameter " + str);
				assert false : "unknown parameter " + str;
			}
		}
		return rf;
	}

	public Robot scCreateRobot(Pair args) {
		RobotFrame root = setRobotRecur(args);
		log.info("root:" + root.getId());
		root.calculateGrossMass();
		return new Robot(root);
	}

	public void scSetRobot(Robot robot) {
		rctx.getSensoryCortex().updateRobot(robot);
	}

	private RobotFrame setRobotRecur(Pair list) {
		RobotFrame parent = null;
		assert U.isList(list);
		assert U.isList(list.getRest());
		Object first = list.getFirst();
		Pair rest = (Pair) list.getRest();
		if (U.isList(first)) {
			parent = setRobotRecur((Pair) first);
		} else if (first instanceof RobotFrame) {
			parent = (RobotFrame) first;
		} else {
			assert false;
			throw new IllegalArgumentException("must be List or RobotFrame");
		}

		if (rest != Pair.EMPTY) {
			RobotFrame child = setRobotRecur(rest);
			child.setParent(parent);
			log.debug("add child " + child.getId() + " to " + parent.getId());
			RobotFrame[] children = Arrays.copyOf(parent.getChildren(), parent
					.getChildren().length + 1);
			children[children.length - 1] = child;
			parent.setChildren(children);
		}
		return parent;
	}

	public void ssSetScheduler(String schedulerName) {
		Task task = rctx.getStrategy().getTaskManager().find(schedulerName);
		if (task == null) {
			log.error("task not found:" + schedulerName);
		} else if (task instanceof Scheduler) {
			log.info("set scheduler " + schedulerName);
			rctx.getStrategy().setNextScheduler((Scheduler) task);
		} else {
			log.error("task is not scheduler:" + schedulerName);
		}
	}

	public void ssSetRole(String roleId) {
		Role role = Role.valueOf(roleId);
		if (role == null) {
			log.error("Invalid role:" + roleId);
			return;
		}
		rctx.getStrategy().setRole(role);
	}

	public void ssSetTeam(String teamId) {
		Team team = Team.valueOf(teamId);
		if (team == null) {
			log.error("Invalid team:" + teamId);
			return;
		}
		rctx.getStrategy().setTeam(team);
	}

	@Deprecated
	public int vcGetParam(int controlId) {
		log.warn("vcGetParam without CameraID is not recommended.");
		return vcGetParam2(rctx.getCamera().getSelectedId().name(), controlId);
	}

	public int vcGetParam2(String camera, int controlId) {
		if (controlId < 0 || controlId >= CameraParam.values().length) {
			log.error("vcSetParam: Invalid Control:" + controlId);
			return 0;
		}
		CameraParam cp = CameraParam.values()[controlId];
		if (!rctx.getCamera().isSupported(cp)) {
			log.error("vcSetParam: Unsupported Control:" + cp);
			return 0;
		}
		CameraID id = CameraID.valueOf(camera);
		if (id == null) {
			log.error("Invalid CameraId:" + camera);
			return 0;
		}
		return rctx.getCamera().getParam(id, cp);
	}

	@Deprecated
	public void vcSetParam(int controlId, int value) {
		log.warn("vcSetParam without CameraID is not recommended.");
		vcSetParam2(CameraID.TOP.name(), controlId, value);
		vcSetParam2(CameraID.BOTTOM.name(), controlId, value);
	}

	public void vcSetParam2(String camera, int controlId, int value) {
		if (controlId < 0 || controlId >= CameraParam.values().length) {
			log.error("vcSetParam: Invalid Control:" + controlId);
			return;
		}
		CameraParam cp = CameraParam.values()[controlId];
		if (!rctx.getCamera().isSupported(cp)) {
			log.error("vcSetParam: Unsupported Control:" + cp);
			return;
		}
		CameraID id = CameraID.valueOf(camera);
		if (id == null) {
			log.error("Invalid CameraId:" + camera);
			return;
		}
		rctx.getCamera().setParam(id, cp, value);
	}

	public void vcSelectCamera(String camera) {
		// FIXME VCのスレッドをロックしないと切り替え処理がおかしくなる.
		CameraID id = CameraID.valueOf(camera);
		if (id == null) {
			log.error("Invalid CameraId:" + camera);
			return;
		}
		rctx.getCamera().selectCamera(id);
	}

	public void vcLoadTMap(String fileName) {
		GCD gcd = new GCD();
		try {
			gcd.loadTMap(fileName);
			rctx.getVision().setGCD(gcd);
		} catch (IOException e) {
			log.error("vcLoadTMap failed.", e);
		}
	}

	private float[] matrix2float(Object[] matrix) {
		assert matrix[0].getClass().isArray();
		int rows = matrix.length;
		int cols = ((Object[]) matrix[0]).length;
		float[] a1 = new float[rows * cols];
		for (int i = 0; i < rows; i++) {
			assert matrix[i].getClass().isArray();
			Object[] row = (Object[]) matrix[i];
			for (int j = 0; j < cols; j++) {
				try {
					a1[i * cols + j] = Float.parseFloat(row[j].toString());
				} catch (NumberFormatException nfe) {
					log.error("", nfe);
				}
			}
		}
		return a1;
	}

	private float[] array2float(Object[] array) {
		float[] floatArray = new float[array.length];
		for (int i = 0; i < array.length; i++) {
			try {
				floatArray[i] = Float.parseFloat(array[i].toString());
			} catch (NumberFormatException nfe) {
				log.error("", nfe);
			}
		}
		return floatArray;
	}

	private int[] array2int(Object[] array) {
		int[] floatArray = new int[array.length];
		for (int i = 0; i < array.length; i++) {
			try {
				floatArray[i] = Integer.parseInt(array[i].toString());
			} catch (NumberFormatException nfe) {
				log.error("", nfe);
			}
		}
		return floatArray;
	}

	private void toRad(float[] a) {
		for (int i = 0; i < a.length; i++) {
			a[i] = MathUtils.toRadians(a[i]);
		}
	}
}
