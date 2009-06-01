/**
 *
 */
package jp.ac.fit.asura.nao;

import java.util.ArrayList;
import java.util.List;

import jp.ac.fit.asura.nao.AsuraCore.Controller;
import jp.ac.fit.asura.nao.misc.FrameException;

import org.apache.log4j.Logger;

/**
 * シングルスレッドのAsuraCoreコントローラ. 主にWebots用.
 *
 * @author sey
 *
 */
public class SingleThreadController extends Controller {
	private static final Logger log = Logger
			.getLogger(SingleThreadController.class);

	public SingleThreadController(RobotContext robotContext) {
		this.robotContext = robotContext;
		this.effector = robotContext.getEffector();
		this.sensor = robotContext.getSensor();
		this.camera = robotContext.getCamera();

		singleGroup = new ArrayList<RobotLifecycle>();
		singleGroup.add(robotContext.getSensoryCortex());
		singleGroup.add(robotContext.getMotor());
		singleGroup.add(robotContext.getCommunication());
		singleGroup.add(robotContext.getVision());
		singleGroup.add(robotContext.getLocalization());
		singleGroup.add(robotContext.getStrategy());
		singleGroup.add(robotContext.getGlue());

		Runnable task = new SingleRunnable();

		thread = new Thread(null, task, "SingleThread");
	}

	private class SingleRunnable implements Runnable {
		private final Logger log = Logger.getLogger(SingleRunnable.class);

		@Override
		public void run() {
			log.info("Start SingleThread");
			try {
				VisualFrameContext context = new VisualFrameContext(
						robotContext);
				MotionFrameContext motionFrame = new MotionFrameContext(
						robotContext);
				int frame = 0;
				Image image = camera.createImage();
				long lastImageTime = 0;
				while (isActive) {
					long before = System.currentTimeMillis();
					context.setFrame(frame);

					effector.before();
					sensor.before();
					log.trace("polling sensor.");
					sensor.poll();

					motionFrame.setActive(true);
					sensor.update(motionFrame.getSensorContext());
					motionFrame.setFrame(frame);

					if (log.isTraceEnabled())
						log.trace(String.format("step frame %d at %d ms",
								context.getFrame(), context.getTime()));
					camera.before();

					log.debug("Step frame " + frame);
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

						motionFrame.setInUse(true);
						context.setMotionFrame(motionFrame);

						for (RobotLifecycle cycle : singleGroup) {
							if (log.isTraceEnabled())
								log.trace("call step " + cycle.toString());

							if (cycle instanceof VisualCycle)
								((VisualCycle) cycle).step(context);
							else if (cycle instanceof MotionCycle)
								((MotionCycle) cycle).step(motionFrame);
							else
								assert false;
						}
					} catch (FrameException e) {
						log.warn("frame " + context.getFrame(), e);
					}
					image.dispose();
					motionFrame.setInUse(false);
					motionFrame.setActive(false);
					camera.after();
					sensor.after();
					effector.after();

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
					frame++;
				}
			} catch (Exception e) {
				log.fatal("SingleThread is dead with exception.", e);
			}
			log.info("SingleThread stopped.");
		}

	}

	private RobotContext robotContext;
	private Effector effector;
	private Camera camera;
	private Sensor sensor;

	private List<RobotLifecycle> singleGroup;
	private Thread thread;

	// Visionの目標動作サイクルをmsで指定.
	private long targetVisualCycleTime;
	private boolean isActive;

	private boolean stopExceptionOccurs = false;

	@Override
	public void init() throws Exception {
		for (RobotLifecycle rl : singleGroup) {
			log.debug("init " + rl.toString());
			rl.init(robotContext);
		}
	}

	@Override
	public void start() throws Exception {
		for (RobotLifecycle rl : singleGroup) {
			log.debug("start " + rl.toString());
			rl.start();
		}

		isActive = true;
		thread.start();
	}

	@Override
	public void stop() throws Exception {
		isActive = false;
		thread.join();
	}

	public long getTargetVisualCycleTime() {
		return targetVisualCycleTime;
	}

	public void setTargetVisualCycleTime(long targetVisualCycleTime) {
		this.targetVisualCycleTime = targetVisualCycleTime;
	}
}
