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

	public Webots6Camera(Robot robot) {
		camera = robot.getCamera("camera");
		selector = robot.getServo("CameraSelect");
	}

	public void after() {
	}

	public void before() {
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

	public int getParam(CameraParam id) {
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
			selector.setPosition(0.7f);
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

	public void setParam(CameraParam id, int value) {
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
		img.timestamp = System.currentTimeMillis() * 1000L;
		img.width = camera.getWidth();
		img.height = camera.getHeight();
		assert img.buffer.remaining() == img.width * img.height;
	}
}
