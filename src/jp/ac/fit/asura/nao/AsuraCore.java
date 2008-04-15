/*
 * 作成日: 2008/04/12
 */
package jp.ac.fit.asura.nao;

import jp.ac.fit.asura.nao.motion.MotorCortex;

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

    private Effector effector;

    private MotorCortex motion;

    private int camera;

    /**
     * 
     */
    public AsuraCore() {
	effector = new WebotsEffector();
	motion = new MotorCortex(effector);
    }

    /**
     * @param id
     *                the id to set
     */
    public void setId(int id) {
	this.id = id;
    }

    /**
     * @param team
     *                the team to set
     */
    public void setTeam(Team team) {
	this.team = team;
    }

    public void init() {
	time = 0;

	motion.init();

	camera = Controller.robot_get_device("camera");
	Controller.camera_enable(camera, 4 * 40);
    }

    public void run(int ts) {
	int[] image = Controller.camera_get_image(camera);
	int width = Controller.camera_get_width(camera);
	int height = Controller.camera_get_height(camera);
	int orangeCount = 0;
	double cx = 0, cy = 0;
	for (int i = 0; i < image.length; i++) {
	    int pixel = image[i];
	    int r = Controller.camera_pixel_get_red(pixel);
	    int g = Controller.camera_pixel_get_green(pixel);
	    int b = Controller.camera_pixel_get_blue(pixel);
	    if (r > 0xA0 && g > 0x50 && g < 0xC0 && b > 0x20 && b < 0x40) {
		cx += i % width - width / 2;
		cy += i / width - height / 2;
		orangeCount++;
	    }
	}

	motion.makemotion("walk", null);
	if (orangeCount > 10) {
	    cx /= orangeCount;
	    cy /= orangeCount;
//	    System.out.println("cx:" + cx);
//	    System.out.println("cy:" + cy);
	    double angle1 = 0.8;
	    double angle2 = angle1 * height / width;
	    double aw = cx / width;
	    double ah = cy / height;
	    motion.makemotion_head_rel((float) (-0.6 * angle1 * aw),
		    (float) (0.6 * angle2 * ah));
//	    motion.makemotion(null, null);

	} else {
	    float yaw = (float) (Math.sin(time * Math.PI / 4000.0
		    * Math.toRadians(60.0)));
	    float pitch = (float) (Math.sin(time * Math.PI / 4000.0
		    * Math.toRadians(20.0)) + Math.toRadians(40.0));
	    motion.makemotion_head(yaw, pitch);
	}
	motion.step(ts);
	time += ts;
    }
}
