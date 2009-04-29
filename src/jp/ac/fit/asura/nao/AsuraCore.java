/*
 * 作成日: 2008/04/12
 */
package jp.ac.fit.asura.nao;

import java.util.ArrayList;
import java.util.List;

import jp.ac.fit.asura.nao.Camera.CameraType;
import jp.ac.fit.asura.nao.communication.MessageManager;
import jp.ac.fit.asura.nao.communication.RoboCupGameControlData;
import jp.ac.fit.asura.nao.glue.SchemeGlue;
import jp.ac.fit.asura.nao.localization.Localization;
import jp.ac.fit.asura.nao.misc.FrameException;
import jp.ac.fit.asura.nao.misc.FrameQueue;
import jp.ac.fit.asura.nao.motion.MotorCortex;
import jp.ac.fit.asura.nao.sensation.SomatoSensoryCortex;
import jp.ac.fit.asura.nao.strategy.StrategySystem;
import jp.ac.fit.asura.nao.strategy.Team;
import jp.ac.fit.asura.nao.vision.VisualCortex;

import org.apache.log4j.Logger;

/**
 * @author $Author: sey $
 *
 * @version $Id: AsuraCore.java 713 2008-11-24 06:27:48Z sey $
 *
 */
public class AsuraCore {
	private static final Logger log = Logger.getLogger(AsuraCore.class);

	private class MotionRunnable implements Runnable {
		private final Logger log = Logger.getLogger(MotionRunnable.class);

		/**
		 * MotionThreadの動作.
		 *
		 * {@link MotionCortex}, {@link SomatoSensoryCortex}などを処理する.
		 *
		 * naojiの場合はDCM/Timeの周期で動作する.
		 */
		@Override
		public void run() {
			log.info("Start MotionThread.");
			try {
				int frame = 0;
				while (isActive) {
					AsuraCore.this.effector.before();
					AsuraCore.this.sensor.before();
					log.trace("polling sensor.");
					AsuraCore.this.sensor.poll();
					log.trace("process motion frame queue");

					// センサーQueueを処理.
					MotionFrameContext context;
					synchronized (idleQueue) {
						assert !idleQueue.isEmpty();
						context = idleQueue.remove(idleQueue.size() - 1);
					}
					assert context != null;
					context.setActive(true);
					AsuraCore.this.sensor.update(context.getSensorContext());
					context.setFrame(frame++);

					if (log.isTraceEnabled())
						log.trace(String.format("step frame %d at %d ms",
								context.getFrame(), context.getTime()));

					for (MotionCycle cycle : motionGroup) {
						if (log.isTraceEnabled())
							log.trace("call step " + cycle.toString());

						try {
							cycle.step(context);
						} catch (RuntimeException e) {
							log.error("MotionThread:", e);
							assert false;
						}
					}

					AsuraCore.this.sensor.after();
					AsuraCore.this.effector.after();

					MotionFrameContext out;
					synchronized (activeQueue) {
						out = activeQueue.enqueue(context);
					}
					synchronized (idleQueue) {
						if (out != null) {
							out.setActive(false);
							if (!out.isInUse())
								idleQueue.add(out);
						}
					}
				}
			} catch (Exception e) {
				log.fatal("MotionThread is dead with exception.", e);
			} finally {
				AsuraCore.this.effector.setPower(0);
			}
			log.info("MotionThread stopped.");
		}
	}

	private class VisionRunnable implements Runnable {
		private final Logger log = Logger.getLogger(VisionRunnable.class);

		/**
		 * VisualThreadの動作.
		 *
		 * {@link VisualCortex}, {@link StrategySystem}などを処理する.
		 *
		 * {@link #targetVisualCycleTime}によって動作間隔を調整可能.
		 */
		@Override
		public void run() {
			log.info("Start VisualThread");
			try {
				VisualFrameContext context = new VisualFrameContext(
						robotContext);
				int frame = 0;
				Image image = AsuraCore.this.camera.createImage();
				long lastImageTime = 0;
				while (isActive) {
					long before = System.currentTimeMillis();
					context.setFrame(frame++);
					AsuraCore.this.camera.before();

					log.debug("Step visual frame " + frame);
					AsuraCore.this.camera.updateImage(image);
					context.setImage(image);

					try {
						long imageTime = image.getTimestamp();
						long timeDiff = imageTime - lastImageTime;

						if (log.isTraceEnabled())
							log.trace("image updated. time:" + imageTime + " ["
									+ timeDiff + " ms]");
						if (timeDiff < 0)
							throw new FrameException("Past image received:"
									+ timeDiff);
						lastImageTime = imageTime;

						MotionFrameContext motionFrame;
						synchronized (activeQueue) {
							log.trace("Find motionFrame nearest " + imageTime);
							motionFrame = activeQueue.findNearest(imageTime);
							if (motionFrame == null)
								throw new FrameException(
										"Can't retrieve motionFrame. ");
							motionFrame.setInUse(true);
						}

						long timeDiff2 = motionFrame.getTime() - imageTime;
						if (log.isTraceEnabled())
							log.trace("Set MotionFrame "
									+ motionFrame.getTime() + " (time diff "
									+ timeDiff2 + ")");
						if (Math.abs(timeDiff2) > 200) {
							log.warn("time diff too large:" + timeDiff2);
						}

						context.setMotionFrame(motionFrame);

						for (VisualCycle cycle : visionGroup) {
							if (log.isTraceEnabled())
								log.trace("call step " + cycle.toString());

							cycle.step(context);
						}

						synchronized (idleQueue) {
							motionFrame.setInUse(false);
							if (!motionFrame.isActive()) {
								idleQueue.add(motionFrame);
							}
						}
					} catch (FrameException e) {
						log.warn("frame " + context.getFrame(), e);
					}
					image.dispose();
					AsuraCore.this.camera.after();

					long after = System.currentTimeMillis();

					if (targetVisualCycleTime == 0) {
						Thread.yield();
					} else {
						long wait = Math.max(targetVisualCycleTime
								- (after - before), 0);
						if (log.isTraceEnabled())
							log.trace("wait " + wait + "[ms] (cunsumed "
									+ (after - before) + " [ms])");
						Thread.sleep(wait);
					}

					if (log.isDebugEnabled()) {
						Runtime rt = Runtime.getRuntime();
						log.debug("Free memory:" + rt.freeMemory() / 1024);
					}
				}
			} catch (Exception e) {
				log.fatal("VisualThread is dead with exception.", e);
			}
			log.info("VisualThread stopped.");

		}
	}

	private class MiscRunnable implements Runnable {
		private final Logger log = Logger.getLogger(MiscRunnable.class);

		@Override
		public void run() {
			log.info("Start MiscThread.");
			log.info("MiscThread stopped.");
		}
	}

	private Effector effector;
	private Sensor sensor;
	private Camera camera;

	private MotorCortex motor;

	private VisualCortex vision;

	private SchemeGlue glue;

	private StrategySystem strategy;

	private RobotContext robotContext;

	private RoboCupGameControlData gameControlData;

	private Localization localization;

	private MessageManager communication;

	private SomatoSensoryCortex sensoryCortex;

	private FrameQueue<MotionFrameContext> activeQueue;
	private List<MotionFrameContext> idleQueue;

	private List<MotionCycle> motionGroup;
	private List<VisualCycle> visionGroup;
	private List<RobotLifecycle> miscGroup;

	private ThreadGroup threads;
	private Thread motionThread;
	private Thread visualThread;
	private Thread miscThread;

	private boolean isActive;

	// Visionの目標動作サイクルをmsで指定.
	private long targetVisualCycleTime;

	public AsuraCore(Effector effector, Sensor sensor, DatagramService ds,
			Camera camera) {
		// FIXME
		if (camera.getType() == CameraType.WEBOTS6)
			targetVisualCycleTime = 0;
		else
			targetVisualCycleTime = 100;

		this.gameControlData = new RoboCupGameControlData();
		this.effector = effector;
		this.sensor = sensor;
		this.camera = camera;
		glue = new SchemeGlue();
		motor = new MotorCortex();
		vision = new VisualCortex();
		strategy = new StrategySystem();
		localization = new Localization();
		communication = new MessageManager();
		sensoryCortex = new SomatoSensoryCortex();

		motionGroup = new ArrayList<MotionCycle>();
		motionGroup.add(sensoryCortex);
		motionGroup.add(motor);

		miscGroup = new ArrayList<RobotLifecycle>();
		// miscGroup.add(communication);
		// miscGroup.add(glue);

		visionGroup = new ArrayList<VisualCycle>();
		visionGroup.add(communication);
		visionGroup.add(vision);
		visionGroup.add(localization);
		visionGroup.add(strategy);
		visionGroup.add(glue);

		robotContext = new RobotContext(this, sensor, effector, ds, camera,
				motor, vision, glue, strategy, gameControlData, localization,
				communication, sensoryCortex);

		int queueSize = 25;
		activeQueue = new FrameQueue<MotionFrameContext>(queueSize);
		idleQueue = new ArrayList<MotionFrameContext>(queueSize + 2);
		for (int i = 0; i < queueSize + 2; i++)
			idleQueue.add(new MotionFrameContext(robotContext));

		threads = new ThreadGroup("Asura");

		Runnable motionTask = new MotionRunnable();
		Runnable visionTask = new VisionRunnable();
		Runnable miscTask = new MiscRunnable();

		motionThread = new Thread(threads, motionTask, "MotionThread");
		visualThread = new Thread(threads, visionTask, "VisionThread");
		miscThread = new Thread(threads, miscTask, "MiscThread");

	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		log.info("Robot set new id:" + id);
		robotContext.setRobotId(id);
	}

	public void setTeam(Team team) {
		log.info("Robot set new team:" + team);
		strategy.setTeam(team);
	}

	public void init() throws Exception {
		log.info("Init AsuraCore");

		effector.init();
		sensor.init();
		camera.init();
		init(motionGroup);
		init(visionGroup);
		init(miscGroup);
	}

	public void start() throws Exception {
		log.info("Start AsuraCore");
		start(motionGroup);
		start(visionGroup);
		start(miscGroup);

		isActive = true;
		motionThread.start();
		visualThread.start();
		miscThread.start();
	}

	private <T extends RobotLifecycle> void init(Iterable<T> it) {
		for (RobotLifecycle rl : it) {
			log.debug("init " + rl.toString());
			rl.init(robotContext);
		}
	}

	private <T extends RobotLifecycle> void start(Iterable<T> it) {
		for (RobotLifecycle rl : it) {
			log.debug("start " + rl.toString());
			rl.start();
		}
	}

	public void stop() throws Exception {
		isActive = false;
		motionThread.join();
		visualThread.join();
		miscThread.join();
	}

	public RobotContext getRobotContext() {
		return robotContext;
	}

	public long getTargetVisualCycleTime() {
		return targetVisualCycleTime;
	}

	public void setTargetVisualCycleTime(long targetVisualCycleTime) {
		this.targetVisualCycleTime = targetVisualCycleTime;
	}
}
