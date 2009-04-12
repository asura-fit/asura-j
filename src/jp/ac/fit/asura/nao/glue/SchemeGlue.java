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
import java.util.Arrays;

import jp.ac.fit.asura.nao.Image;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.Camera.CameraParam;
import jp.ac.fit.asura.nao.Camera.PixelFormat;
import jp.ac.fit.asura.nao.glue.naimon.Naimon;
import jp.ac.fit.asura.nao.glue.naimon.Naimon.NaimonFrames;
import jp.ac.fit.asura.nao.misc.Pixmap;
import jp.ac.fit.asura.nao.misc.TeeOutputStream;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.MotionFactory;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.motion.MotorCortex;
import jp.ac.fit.asura.nao.physical.Robot;
import jp.ac.fit.asura.nao.physical.RobotFrame;
import jp.ac.fit.asura.nao.physical.Robot.Frames;
import jp.ac.fit.asura.nao.strategy.Role;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.Team;
import jp.ac.fit.asura.nao.strategy.schedulers.Scheduler;
import jp.ac.fit.asura.nao.vision.GCD;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualCortex;
import jscheme.JScheme;
import jsint.BacktraceException;
import jsint.Pair;
import jsint.U;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author sey
 *
 * @version $Id: SchemeGlue.java 717 2008-12-31 18:16:20Z sey $
 *
 */
public class SchemeGlue implements RobotLifecycle {
	private static final Logger log = Logger.getLogger(SchemeGlue.class);

	public enum InterpolationType {
		Raw(1), Liner(2), Compatible(3);
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
	private boolean showNaimon;

	private Naimon naimon;

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

	public void init(RobotContext context) {
		log.info("Init SchemeGlue.");
		this.rctx = context;
		motor = context.getMotor();

		// Declare global values
		js.setGlobalValue("glue", this);

		// Declare joint definition
		for (Frames frame : Frames.values()) {
			js.setGlobalValue(frame.name(), frame.ordinal());
		}

		showNaimon = false;
		saveImageInterval = 0;

		httpd.init(rctx);
	}

	public void start() {
		log.info("Start SchemeGlue.");
		if (showNaimon)
			naimon.start();

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

	public void step() {
		if (saveImageInterval != 0 && rctx.getFrame() % saveImageInterval == 0) {
			log.debug("save image.");
			VisualCortex vc = rctx.getVision();
			VisualContext ctx = vc.getVisualContext();
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
				ppm.write("snapshot/image" + rctx.getFrame() + ".ppm");
			} catch (Exception e) {
				log.error("", e);
			}
		}

		if (showNaimon)
			naimon.step();
	}

	public void stop() {
		if (showNaimon)
			naimon.stop();
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
		js.eval(expression);
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

	@Deprecated
	public void glueSetShowPlane(boolean b) {
		log.debug("glueSetShowPlane is deprecated.");
		glueSetShowNaimon(b);
	}

	public void glueSetShowNaimon(boolean b) {
		// オン>オフになるときに不可視にする
		if (b && naimon == null) {
			naimon = new Naimon();
			naimon.init(rctx);
			naimon.start();
		} else if (!b && showNaimon) {
			naimon.stop();
			naimon.dispose();
			naimon = null;
		}
		showNaimon = b;
	}

	/**
	 * 表示するNaimonのフレームを決定する.
	 *
	 * @param args
	 */
	public void glueNaimonFrames(Pair args) {
		if (!showNaimon) {
			log.info("Naimon is disable.");
			return;
		}

		assert U.isList(args);

		boolean vision = false;
		boolean field = false;
		boolean scheme = false;
		boolean makeMotionHelper = false;
		boolean pressure = false;

		for (Object o : U.listToVector(args)) {
			int i = Integer.parseInt(o.toString());
			switch (i) {
			case 0:
				vision = true;
				break;
			case 1:
				field = true;
				break;
			case 2:
				scheme = true;
				break;
			case 3:
				makeMotionHelper = true;
				break;
			case 4:
				pressure = true;
				break;
			}
		}

		naimon.setEnable(NaimonFrames.VISION, vision);
		naimon.setEnable(NaimonFrames.FIELD, field);
		naimon.setEnable(NaimonFrames.SCHEME, scheme);
		naimon.setEnable(NaimonFrames.MAKEMOTIONHELPER, makeMotionHelper);
		naimon.setEnable(NaimonFrames.PRESSURE, pressure);
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
			assert id >= 0 : "id must be positive or zero.";
			assert type != null : "invalid ip type.";

			Motion motion;
			// 引数の型を変換してモーションを作成
			switch (type) {
			case Raw: {
				float[][] a1 = new float[scmArgs.length][];
				for (int i = 0; i < scmArgs.length; i++) {
					assert scmArgs[i].getClass().isArray();
					a1[i] = array2float((Object[]) scmArgs[i]);
				}
				motion = MotionFactory.Raw.create(a1);
				break;
			}
			case Compatible:
			case Liner: {
				if (scmArgs.length != 2)
					log.error("args must be 2.");
				assert scmArgs.length == 2;
				Object[] frames = (Object[]) scmArgs[0];
				Object[] frameStep = (Object[]) scmArgs[1];

				float[][] a1 = new float[frames.length][];
				for (int i = 0; i < frames.length; i++) {
					assert frames[i].getClass().isArray();
					a1[i] = array2float((Object[]) frames[i]);
				}

				int[] a2 = array2int(frameStep);

				if (a1.length != a2.length)
					log.error("args length must be equal. but " + a1.length
							+ " and " + a2.length);
				assert a1.length == a2.length;

				if (type == InterpolationType.Compatible)
					if (id == Motions.MOTION_YY_FORWARD) {
						motion = MotionFactory.Forward.create(a1, a2);
					} else {
						motion = MotionFactory.Compatible.create(a1, a2);
					}
				else {
					motion = MotionFactory.Liner.create(a1, a2);
				}
				log.debug("new motion " + name + " registered. frames: "
						+ frames.length);

				break;
			}
			default:
				assert false;
				motion = null;
			}
			motion.setName(name);
			motion.setId(id);
			motor.registMotion(motion);
		} catch (Exception e) {
			log.fatal("", e);
			assert false;
		}
	}

	public void mcMakemotion(int id) {
		log.info("makemotion:" + id);
		motor.makemotion(id);
	}

	public void mcMotorPower(boolean sw) {
		if (sw)
			log.info("Motor Power on");
		else
			log.info("Motor Power off");

		rctx.getEffector().setPower(sw);
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
		log.info(args);
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
			log.info("add child " + child.getId() + " to " + parent.getId());
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
		rctx.getStrategy().setRole(role);
	}

	public void ssSetTeam(String teamId) {
		Team team = Team.valueOf(teamId);
		rctx.getStrategy().setTeam(team);
	}

	public int vcGetParam(int controlId) {
		if (controlId < 0 || controlId >= CameraParam.values().length) {
			log.error("vcSetParam: Invalid Control:" + controlId);
			return 0;
		}
		CameraParam cp = CameraParam.values()[controlId];
		if (!rctx.getCamera().isSupported(cp)) {
			log.error("vcSetParam: Unsupported Control:" + cp);
			return 0;
		}
		return rctx.getCamera().getParam(cp);
	}

	public void vcSetParam(int controlId, int value) {
		if (controlId < 0 || controlId >= CameraParam.values().length) {
			log.error("vcSetParam: Invalid Control:" + controlId);
			return;
		}
		CameraParam cp = CameraParam.values()[controlId];
		if (!rctx.getCamera().isSupported(cp)) {
			log.error("vcSetParam: Unsupported Control:" + cp);
			return;
		}
		rctx.getCamera().setParam(cp, value);
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
}
