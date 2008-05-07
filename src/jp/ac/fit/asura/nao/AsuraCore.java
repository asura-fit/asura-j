/*
 * 作成日: 2008/04/12
 */
package jp.ac.fit.asura.nao;

import java.util.ArrayList;
import java.util.List;

import jp.ac.fit.asura.nao.glue.SchemeGlue;
import jp.ac.fit.asura.nao.motion.MotorCortex;
import jp.ac.fit.asura.nao.vision.VisualCortex;
import jp.ac.fit.asura.nao.vision.VisualObject;
import jp.ac.fit.asura.nao.vision.VisualCortex.VisualObjects;

import com.cyberbotics.webots.Controller;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class AsuraCore {
	public enum Team {
		Red, Blue
	};

	private int id;

	private Team team;

	private int time;

	private List<RobotLifecycle> lifecycleListeners;

	private Effector effector;
	private Sensor sensor;

	private MotorCortex motor;

	private VisualCortex vision;

	private SchemeGlue glue;

	private RobotContext robotContext;

	/**
	 * 
	 */
	public AsuraCore() {
		lifecycleListeners = new ArrayList<RobotLifecycle>();
		glue = new SchemeGlue();
		effector = new WebotsEffector();
		motor = new MotorCortex();
		sensor = new WebotsSensor();
		vision = new VisualCortex();
		lifecycleListeners.add(glue);
		// lifecycleListeners.add(effector);
		lifecycleListeners.add(motor);
		// lifecycleListeners.add(vision);
		robotContext = new RobotContext(this, motor, vision, sensor, effector,
				glue);
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @param team
	 *            the team to set
	 */
	public void setTeam(Team team) {
		this.team = team;
	}

	public void init() {
		time = 0;

		for (RobotLifecycle rl : lifecycleListeners) {
			rl.init(robotContext);
		}
	}

	public void start() {
		for (RobotLifecycle rl : lifecycleListeners) {
			rl.start();
		}
	}

	public void run(int ts) {
		Image image = sensor.getImage();

		int width = image.getWidth();
		int height = image.getHeight();
		vision.updateImage(image);

		motor.makemotion(1, null);

		VisualObject vo = vision.get(VisualObjects.Ball);
		if (vo.cf > 0) {
			double angle1 = 0.8;
			double angle2 = angle1 * height / width;
			double aw = vo.center.getX() / width;
			double ah = vo.center.getY() / height;
			motor.makemotion_head_rel((float) (-0.6 * angle1 * aw),
					(float) (0.6 * angle2 * ah));
		} else {
			float yaw = (float) (Math.sin(time * Math.PI / 4000.0
					* Math.toRadians(60.0)));
			float pitch = (float) (Math.sin(time * Math.PI / 4000.0
					* Math.toRadians(20.0)) + Math.toRadians(40.0));
			motor.makemotion_head(yaw, pitch);
		}
		motor.step(ts);
		time += ts;
	}
}
