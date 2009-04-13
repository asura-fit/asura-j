/*
 * 作成日: 2009/04/06
 */
package jp.ac.fit.asura.nao.naoji;

import static jp.ac.fit.asura.naoji.v4l2.V4L2PixelFormat.PixelFormat.V4L2_PIX_FMT_YUYV;

import java.io.IOException;

import jp.ac.fit.asura.nao.Camera;
import jp.ac.fit.asura.nao.Image;
import jp.ac.fit.asura.naoji.i2c.I2Cdev;
import jp.ac.fit.asura.naoji.v4l2.V4L2Control;
import jp.ac.fit.asura.naoji.v4l2.V4L2PixelFormat;
import jp.ac.fit.asura.naoji.v4l2.Videodev;

import org.apache.log4j.Logger;

/**
 *
 * Nao実機でのカメラの実装. NaojiV4L2を使用します.
 *
 *
 * 問題点:たぶんsetResolutionで解像度を変えると落ちる. 色を変えようとしても落ちる.
 *
 * @author sey
 *
 * @version $Id: $
 *
 */
public class NaojiCamera implements Camera {
	private static final Logger log = Logger.getLogger(NaojiCamera.class);
	private Videodev video;
	private I2Cdev i2c;
	private Resolution resolution;
	private V4L2PixelFormat format;
	private int fps;
	private CameraID cameraId;

	/**
	 *
	 */
	public NaojiCamera(String dev1, String dev2) {
		format = new V4L2PixelFormat();
		try {
			video = new Videodev(dev1);
			// i2c = new I2Cdev(dev2);
		} catch (IOException e) {
			log.fatal("", e);
			assert false;
		}
	}

	public void after() {
	}

	public void before() {
	}

	public void init() {
		int res;
		// i2c.init();
		video.setControl(V4L2Control.V4L2_CID_CAM_INIT, 0);
		video.setControl(V4L2Control.V4L2_CID_HFLIP, 1);
		video.setControl(V4L2Control.V4L2_CID_VFLIP, 1);
		video.setControl(V4L2Control.V4L2_CID_AUDIO_MUTE, 0);
		video.setControl(V4L2Control.V4L2_CID_AUTO_WHITE_BALANCE, 0);
		video.setControl(V4L2Control.V4L2_CID_AUTOGAIN, 0);
		// video.setControl(V4L2Control.V4L2_CID_RED_BALANCE, 0);
		// video.setControl(V4L2Control.V4L2_CID_BLUE_BALANCE, 0);
		format.setPixelFormat(V4L2_PIX_FMT_YUYV.getFourccCode());
		setResolution(Resolution.QVGA);
		setFPS(30);
		res = video.init();
		if (res <= 0)
			log.fatal("Video initialization failed:" + res);
		res = video.start();
		if (res != 0)
			log.error("Can't start videodev:" + res);
	}

	public Image createImage() {
		V4L2Image img = new V4L2Image(video);
		return img;
	}

	public int getFPS() {
		return fps;
	}

	/**
	 * Red doc/Hardwareより、対角(diagonal)の画角は 58 degrees.
	 *
	 * DFOV = 58, D = sqrt(640^2+480^2) = 554.25625842204073392878282928188
	 *
	 * W = 640, H = 480, W:H = 4:3
	 *
	 * よってD:W:H = 5:4:3
	 *
	 * HFOV = W/D*DFOV, VFOV = H/D*DFOV
	 *
	 */
	public float getHorizontalFieldOfView() {
		return 0.81f;
	}

	public int getParam(CameraParam id) {
		return video.getControl(mapV4L2Control(id));
	}

	public Resolution getResolution() {
		return resolution;
	}

	public CameraID getSelectedId() {
		return cameraId;
	}

	public CameraType getType() {
		return CameraType.V4L2;
	}

	public float getVerticalFieldOfView() {
		return 0.607067f;
	}

	public boolean isSupported(CameraParam id) {
		return video.isSupportedControl(mapV4L2Control(id));
	}

	public boolean isSupported(Resolution id) {
		if (id == Resolution.QVGA || id == Resolution.VGA)
			return true;
		return false;
	}

	public boolean isSupportedFPS(int fps) {
		// not implemented.
		log.warn("isSupportedFPS is not properly implemented.");
		if (fps > 0 && fps <= 30)
			return true;
		return false;
	}

	public void selectCamera(CameraID id) {
		log.error("selectCamera is not implemented.");
		if (cameraId == id)
			return;
		// not implemented.
		// video.dispose();
		// if (id == CameraID.TOP)
		// i2c.selectCamera(1);
		// else
		// i2c.selectCamera(2);
		// video.init();
	}

	public void setFPS(int fps) {
		int res = video.setFPS(fps);
		if (res != 0)
			log.error("set FPS failed. fps:" + fps + " code:" + res);
		this.fps = fps;
	}

	public void setParam(CameraParam id, int value) {
		V4L2Control ctrl = mapV4L2Control(id);
		int res = video.setControl(ctrl, value);
		if (res != 0)
			log.error("set Param failed. id:" + id + " value:" + value
					+ " code:" + res);
	}

	public void setResolution(Resolution resolution) {
		assert isSupported(resolution);
		format.setWidth(resolution.getWidth());
		format.setHeight(resolution.getHeight());
		int res = video.setFormat(format);
		if (res != 0) {
			log.error("Can't set resolution:" + resolution + " code:" + res);
		}
		this.resolution = resolution;
	}

	public void updateImage(Image imgObj) {
		assert imgObj instanceof V4L2Image;
		V4L2Image img = (V4L2Image) imgObj;
		img.width = format.getWidth();
		img.height = format.getHeight();

		img.pixelFormat = PixelFormat.YUYV;

		int res;
		res = video.retrieveImage(img.buffer);
		if (res != 0) {
			log.error("Can't retrieve image. code:" + res);
			return;
		}
		img.setValid(true);
	}

	private V4L2Control mapV4L2Control(CameraParam cp) {
		V4L2Control ctrl;
		switch (cp) {
		case AEC:
			ctrl = V4L2Control.V4L2_CID_AUDIO_MUTE;
			break;
		case AGC:
			ctrl = V4L2Control.V4L2_CID_AUTOGAIN;
			break;
		case AWB:
			ctrl = V4L2Control.V4L2_CID_AUTO_WHITE_BALANCE;
			break;
		case BlueChroma:
			ctrl = V4L2Control.V4L2_CID_BLUE_BALANCE;
			break;
		case Brightness:
			ctrl = V4L2Control.V4L2_CID_BRIGHTNESS;
			break;
		case Contrast:
			ctrl = V4L2Control.V4L2_CID_CONTRAST;
			break;
		case Exposure:
			ctrl = V4L2Control.V4L2_CID_EXPOSURE;
			break;
		case Gain:
			ctrl = V4L2Control.V4L2_CID_GAIN;
			break;
		case Hue:
			ctrl = V4L2Control.V4L2_CID_HUE;
			break;
		case RedChroma:
			ctrl = V4L2Control.V4L2_CID_RED_BALANCE;
			break;
		case Saturation:
			ctrl = V4L2Control.V4L2_CID_SATURATION;
			break;
		default:
			log.error("Unsupported Control:" + cp);
			assert false : "Unsupported Control";
			ctrl = V4L2Control.V4L2_CID_CAM_INIT;
		}
		return ctrl;
	}
}
