/*
 * 作成日: 2009/03/30
 */
package jp.ac.fit.asura.nao.webots;

import java.nio.IntBuffer;

import jp.ac.fit.asura.nao.Image;

import com.cyberbotics.webots.controller.Camera;
import com.cyberbotics.webots.controller.Robot;
import com.cyberbotics.webots.controller.Servo;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
class Webots6Camera implements jp.ac.fit.asura.nao.Camera {
	private Camera camera;
	private Servo selector;
	private CameraID selectedID;
	private int width;
	private int height;
	private float hFov;
	private float vFov;

	private TimeBarier motionBarier;
	private TimeBarier visualBarier;

	private int time;

	public Webots6Camera(Robot robot, TimeBarier motionBarier,
			TimeBarier visualBarier) {
		this.motionBarier = motionBarier;
		this.visualBarier = visualBarier;
		camera = robot.getCamera("camera");
		selector = robot.getServo("CameraSelect");
		time = 0;
	}

	public void after() {
		motionBarier.notifyTime(time);
	}

	public void before() {
		time += Webots6Player.SIMULATION_STEP;
		visualBarier.waitTime(time);
	}

	public void init() {
		camera.enable(Webots6Player.SIMULATION_STEP);
		width = camera.getWidth();
		height = camera.getHeight();
		hFov = (float) camera.getFov();
		vFov = hFov * height / width;
		selector.enablePosition(Webots6Player.SIMULATION_STEP);
		selector.setPosition(0);
		selectedID = CameraID.TOP;
	}

	public CameraType getType() {
		return CameraType.WEBOTS6;
	}

	public Image createImage() {
		return new Webots6Image();
	}

	@Override
	public int getParam(CameraID camera, CameraParam id) {
		return 0;
	}

	public Resolution getResolution() {
		return Resolution.QQVGA;
	}

	public CameraID getSelectedId() {
		return selectedID;
	}

	public boolean isSupported(CameraParam id) {
		return false;
	}

	public boolean isSupported(Resolution id) {
		return id == Resolution.QQVGA;
	}

	public boolean isSupportedFPS(int fps) {
		return fps == 25;
	}

	public void selectCamera(CameraID id) {
		switch (id) {
		case TOP:
			selector.setPosition(0);
			break;
		case BOTTOM:
			selector.setPosition(0.6981f);
			break;
		default:
			assert false : id;
		}
		selectedID = id;
	}

	public int getFPS() {
		return 25;
	}

	public void setFPS(int fps) {
	}

	@Override
	public void setParam(CameraID cameraId, CameraParam id, int value) {
	}

	public void setResolution(Resolution res) {
	}

	public float getHorizontalFieldOfView() {
		return hFov;
	}

	public float getVerticalFieldOfView() {
		return vFov;
	}

	public void updateImage(Image imgObj) {
		assert imgObj instanceof Webots6Image;
		Webots6Image img = (Webots6Image) imgObj;
		img.buffer = IntBuffer.wrap(camera.getImage());
		img.buffer.position(0);
		img.timestamp = time;
		img.width = camera.getWidth();
		img.height = camera.getHeight();
		assert img.buffer.remaining() == img.width * img.height;
	}
}
