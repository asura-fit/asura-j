/**
 *
 */
package jp.ac.fit.asura.nao;

import java.util.ArrayList;
import java.util.List;

import jp.ac.fit.asura.nao.AsuraCore.Controller;
import jp.ac.fit.asura.nao.misc.FrameException;
import jp.ac.fit.asura.nao.misc.FrameQueue;
import jp.ac.fit.asura.nao.sensation.SomatoSensoryCortex;
import jp.ac.fit.asura.nao.strategy.StrategySystem;
import jp.ac.fit.asura.nao.vision.VisualCortex;

import org.apache.log4j.Logger;

/**
 * シングルスレッドのAsuraCoreコントローラ. 主にNaoji用.
 *
 * @author sey
 *
 */
public class MultiThreadController extends Controller {
	private static final Logger log = Logger
			.getLogger(MultiThreadController.class);

	public MultiThreadController(RobotContext robotContext) {
		this.robotContext = robotContext;
		this.effector = robotContext.getEffector();
		this.sensor = robotContext.getSensor();
		this.camera = robotContext.getCamera();

		motionGroup = new ArrayList<MotionCycle>();
		motionGroup.add(robotContext.getSensoryCortex());
		motionGroup.add(robotContext.getMotor());

		miscGroup = new ArrayList<RobotLifecycle>();
		// miscGroup.add(communication);
		// miscGroup.add(glue);

		visionGroup = new ArrayList<VisualCycle>();
		visionGroup.add(robotContext.getCommunication());
		visionGroup.add(robotContext.getVision());
		visionGroup.add(robotContext.getLocalization());
		visionGroup.add(robotContext.getStrategy());
		visionGroup.add(robotContext.getGlue());

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
					try {
						effector.before();
						sensor.before();
						log.trace("polling sensor.");
						sensor.poll();
						log.trace("process motion frame queue");

						// センサーQueueを処理.
						MotionFrameContext context;
						synchronized (idleQueue) {
							assert !idleQueue.isEmpty();
							context = idleQueue.remove(idleQueue.size() - 1);
						}
						assert context != null;
						context.setActive(true);
						sensor.update(context.getSensorContext());
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

						sensor.after();
						effector.after();

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
					} catch (Exception e) {
						if (stopExceptionOccurs)
							throw e;
						else
							log.error("MotionThread:exception occurred.", e);
					}
				}
			} catch (Exception e) {
				log.fatal("MotionThread is dead with exception.", e);
			} finally {
				effector.setPower(0);
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
				Image image = camera.createImage();
				long lastImageTime = 0;
				while (isActive) {
					long before = System.currentTimeMillis();
					context.setFrame(frame++);
					camera.before();

					log.debug("Step visual frame " + frame);
					camera.updateImage(image);
					context.setImage(image);

					try {
						long imageTime = image.getTimestamp();
						long timeDiff = imageTime - lastImageTime;
						lastImageTime = imageTime;

						if (log.isTraceEnabled())
							log.trace("image updated. time:" + imageTime + " ["
									+ timeDiff + " ms]");
						if (timeDiff < 0)
							throw new FrameException("Past image received:"
									+ timeDiff);

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
					camera.after();

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

	private RobotContext robotContext;
	private Effector effector;
	private Camera camera;
	private Sensor sensor;

	private FrameQueue<MotionFrameContext> activeQueue;
	private List<MotionFrameContext> idleQueue;

	private List<MotionCycle> motionGroup;
	private List<VisualCycle> visionGroup;
	private List<RobotLifecycle> miscGroup;

	private ThreadGroup threads;
	private Thread motionThread;
	private Thread visualThread;
	private Thread miscThread;

	// Visionの目標動作サイクルをmsで指定.
	private long targetVisualCycleTime;
	private boolean isActive;

	private boolean stopExceptionOccurs = false;

	@Override
	public void init() throws Exception {
		init(motionGroup);
		init(visionGroup);
		init(miscGroup);
	}

	@Override
	public void start() throws Exception {
		start(motionGroup);
		start(visionGroup);
		start(miscGroup);

		isActive = true;
		motionThread.start();
		visualThread.start();
		miscThread.start();
	}

	@Override
	public void stop() throws Exception {
		isActive = false;
		motionThread.join();
		visualThread.join();
		miscThread.join();
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

	public long getTargetVisualCycleTime() {
		return targetVisualCycleTime;
	}

	public void setTargetVisualCycleTime(long targetVisualCycleTime) {
		this.targetVisualCycleTime = targetVisualCycleTime;
	}
}
