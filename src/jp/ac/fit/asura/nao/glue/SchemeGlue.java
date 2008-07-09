/*
 * 作成日: 2008/05/05
 */
package jp.ac.fit.asura.nao.glue;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import javax.imageio.ImageIO;

import jp.ac.fit.asura.nao.Image;
import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.motion.Motion;
import jp.ac.fit.asura.nao.motion.MotionFactory;
import jp.ac.fit.asura.nao.motion.Motions;
import jp.ac.fit.asura.nao.motion.MotorCortex;
import jp.ac.fit.asura.nao.strategy.Role;
import jp.ac.fit.asura.nao.strategy.Task;
import jp.ac.fit.asura.nao.strategy.Team;
import jp.ac.fit.asura.nao.strategy.schedulers.Scheduler;
import jscheme.JScheme;
import jsint.BacktraceException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class SchemeGlue implements RobotLifecycle {
	private Logger log = Logger.getLogger(SchemeGlue.class);

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

	/**
	 *
	 */
	public SchemeGlue() {
		js = new JScheme();
		httpd = new TinyHttpd(js);
	}

	public void init(RobotContext context) {
		this.rctx = context;
		motor = context.getMotor();
		js.setGlobalValue("glue", this);

		showNaimon = false;
		saveImageInterval = 0;
	}

	public void start() {
		if (showNaimon)
			naimon.start();

		try {
			log.debug(Charset.defaultCharset());
			FileInputStream fis = new FileInputStream("init.scm");
			Reader reader = new InputStreamReader(fis, Charset.forName("UTF-8"));
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
			int[] yvu = rctx.getVision().getGCD().getYvuPlane();
			Image image = rctx.getSensor().getImage();
			try {
				BufferedImage buf = new BufferedImage(image.getWidth(), image
						.getHeight(), BufferedImage.TYPE_INT_RGB);
				int[] pixels = ((DataBufferInt) buf.getRaster().getDataBuffer())
						.getData();
				System.arraycopy(yvu, 0, pixels, 0, image.getData().length);
				ImageIO.write(buf, "BMP", new File("snapshot/image"
						+ rctx.getFrame() + ".bmp"));
			} catch (Exception e) {
				e.printStackTrace();
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

	public void eval(String expression) {
		js.load(expression);
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
				log.debug("new motion registered. frames: " + frames.length);

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

	private float[] array2float(Object[] array) {
		float[] floatArray = new float[array.length];
		for (int i = 0; i < array.length; i++) {
			try {
				floatArray[i] = Float.parseFloat(array[i].toString());
			} catch (NumberFormatException nfe) {
				nfe.printStackTrace();
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
				nfe.printStackTrace();
			}
		}
		return floatArray;
	}
}
