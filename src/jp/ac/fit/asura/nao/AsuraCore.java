/*
 * 作成日: 2008/04/12
 */
package jp.ac.fit.asura.nao;

import java.util.ArrayList;
import java.util.List;

import jp.ac.fit.asura.nao.communication.MessageManager;
import jp.ac.fit.asura.nao.communication.RoboCupGameControlData;
import jp.ac.fit.asura.nao.glue.SchemeGlue;
import jp.ac.fit.asura.nao.localization.Localization;
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

	/**
	 * @param <T>
	 *
	 */
	public AsuraCore(Effector effector, Sensor sensor, DatagramService ds,
			Camera camera) {
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

		robotContext = new RobotContext(sensor, effector, ds, camera, motor,
				vision, glue, strategy, gameControlData, localization,
				communication, sensoryCortex);

		int queueSize = 5;
		activeQueue = new FrameQueue<MotionFrameContext>(queueSize);
		idleQueue = new ArrayList<MotionFrameContext>(queueSize + 2);
		for (int i = 0; i < queueSize + 2; i++)
			idleQueue.add(new MotionFrameContext(robotContext));

		threads = new ThreadGroup("Asura");

		Runnable motionTask = new Runnable() {
			@Override
			public void run() {
				log.info("Start MotionThread.");
				try {
					int frame = 0;
					while (isActive) {
						AsuraCore.this.effector.before();
						AsuraCore.this.sensor.before();
						log.trace("MotionThread: polling sensor.");
						AsuraCore.this.sensor.poll();
						log.debug("MotionThread: process motion frame queue");

						// センサーQueueを処理.
						MotionFrameContext context;
						synchronized (activeQueue) {
							assert !idleQueue.isEmpty();
							context = idleQueue.remove(idleQueue.size() - 1);
							assert context != null;

							AsuraCore.this.sensor.update(context
									.getSensorContext());
							context.setActive(true);
							MotionFrameContext out = activeQueue
									.enqueue(context);
							if (out != null) {
								out.setActive(false);
								if (!out.isInUse())
									idleQueue.add(out);
							}
						}

						context.setFrame(frame++);

						if (log.isTraceEnabled())
							log.trace(String.format(
									"MotionThread: step frame %d at %d ms",
									context.getFrame(), context.getTime()));

						for (MotionCycle cycle : motionGroup) {
							if (log.isTraceEnabled())
								log.trace("MotionThread: call step "
										+ cycle.toString());

							try {
								cycle.step(context);
							} catch (RuntimeException e) {
								log.error("MotionThread:", e);
								assert false;
							}
						}

						AsuraCore.this.sensor.after();
						AsuraCore.this.effector.after();
					}
				} catch (Exception e) {
					log.fatal("MotionThread is dead with exception.", e);
				} finally {
					AsuraCore.this.effector.setPower(0);
				}
				log.info("MotionThread stopped.");
			}
		};

		Runnable visionTask = new Runnable() {
			@Override
			public void run() {
				log.info("Start VisualThread");
				try {
					VisualFrameContext context = new VisualFrameContext(
							robotContext);
					int frame = 0;
					Image image = AsuraCore.this.camera.createImage();
					while (isActive) {
						context.setFrame(frame++);
						AsuraCore.this.camera.before();
						log.debug("VisualThread:Step visual frame " + frame);
						AsuraCore.this.camera.updateImage(image);
						context.setImage(image);
						long imageTime = image.getTimestamp() / 1000;

						MotionFrameContext motionFrame;
						synchronized (activeQueue) {
							log.trace("VisualThread:Find motionFrame nearest "
									+ imageTime);
							motionFrame = activeQueue.findNearest(imageTime);
							if (motionFrame == null) {
								log
										.info("VisualThread:Can't retrieve motionFrame. skip visual frame "
												+ context.getFrame());
								image.dispose();
								continue;
							}
							motionFrame.setInUse(true);
						}
						log.trace("VisualThread:Set MotionFrame "
								+ motionFrame.getTime());
						context.setMotionFrame(motionFrame);

						for (VisualCycle cycle : visionGroup) {
							if (log.isTraceEnabled())
								log.trace("VisualThread: call step "
										+ cycle.toString());

							try {
								cycle.step(context);
							} catch (RuntimeException e) {
								log.error("VisualThread:", e);
								assert false;
							}
						}

						image.dispose();
						synchronized (activeQueue) {
							motionFrame.setInUse(false);
							if (!motionFrame.isActive())
								idleQueue.add(motionFrame);
						}
						AsuraCore.this.camera.after();
					}
				} catch (Exception e) {
					log.fatal("VisualThread is dead with exception.", e);
				}
				log.info("VisualThread stopped.");

			}
		};
		Runnable miscTask = new Runnable() {
			@Override
			public void run() {
				log.info("Start MiscThread.");
				log.info("MiscThread stopped.");
			}
		};

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
}
