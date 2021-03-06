/*
 * 作成日: 2009/04/06
 */
package jp.ac.fit.asura.nao.naoji;

import static jp.ac.fit.asura.naoji.v4l2.V4L2PixelFormat.PixelFormat.V4L2_PIX_FMT_YUYV;

import java.io.IOException;
import java.util.EnumMap;

import jp.ac.fit.asura.nao.Camera;
import jp.ac.fit.asura.nao.Image;
import jp.ac.fit.asura.naoji.i2c.I2Cdev;
import jp.ac.fit.asura.naoji.robots.NaoV3R;
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
	private CameraID nextCameraId;
	private EnumMap<CameraID, EnumMap<V4L2Control, Integer>> params;
	private String v4lDev;
	private String i2cDev;

	/**
	 *
	 */
	public NaojiCamera(String dev1, String dev2) {
		params = new EnumMap<CameraID, EnumMap<V4L2Control, Integer>>(
				CameraID.class);
		for (CameraID id : CameraID.values())
			params
					.put(id, new EnumMap<V4L2Control, Integer>(
							V4L2Control.class));
		format = new V4L2PixelFormat();
		v4lDev = dev1;
		i2cDev = dev2;
		/*
		 * try { video = new Videodev(dev1); i2c = new I2Cdev(dev2); } catch
		 * (IOException e) { log.fatal("", e); assert false; }
		 */
	}

	public void after() {
		if (nextCameraId != null && nextCameraId != cameraId) {
			switchCamera(nextCameraId);
		}
	}

	public void before() {
	}

	/**
	 *
	 */
	public void init() {
		log.debug("NaojiCamera start init");
		try {
			video = new Videodev(v4lDev);
			i2c = new I2Cdev(i2cDev);
		} catch (IOException e) {
			log.fatal("", e);
			assert false;
		}

		// V4L2の初期化. 初期化の順番を間違えると止まる.
		// かなりイミフな挙動だがこれだと動く. 謎.
		int res;
		log.debug("NaojiCamera i2c init");
		i2c.init();
		format.setPixelFormat(V4L2_PIX_FMT_YUYV.getFourccCode());

		log.debug("NaojiCamera init top camera");
		res = i2c.selectCamera(NaoV3R.I2C_CAMERA_TOP);
		if (res != 0)
			log.error("Can't selectCamera:" + NaoV3R.I2C_CAMERA_TOP + " res:"
					+ res);

		if (i2c.getSelectedCamera() != NaoV3R.I2C_CAMERA_TOP) {
			log.error("Can't selectCamera:" + NaoV3R.I2C_CAMERA_TOP + " res:"
					+ res);
		}

		res = video.setControl(V4L2Control.V4L2_CID_CAM_INIT, 0);
		if (res != 0)
			log.error("Can't setControl:" + V4L2Control.V4L2_CID_CAM_INIT
					+ " res:" + res);
		res = video.setControl(V4L2Control.V4L2_CID_AUTOEXPOSURE, 0);
		if (res != 0)
			log.error("Can't setControl:" + V4L2Control.V4L2_CID_AUTOEXPOSURE
					+ " res:" + res);
		res = video.setControl(V4L2Control.V4L2_CID_AUTO_WHITE_BALANCE, 0);
		if (res != 0)
			log.error("Can't setControl:"
					+ V4L2Control.V4L2_CID_AUTO_WHITE_BALANCE + " res:" + res);
		res = video.setControl(V4L2Control.V4L2_CID_AUTOGAIN, 0);
		if (res != 0)
			log.error("Can't setControl:" + V4L2Control.V4L2_CID_AUTOGAIN
					+ " res:" + res);
		res = video.setControl(V4L2Control.V4L2_CID_HFLIP, 1);
		if (res != 0)
			log.error("Can't setControl:" + V4L2Control.V4L2_CID_HFLIP
					+ " res:" + res);
		res = video.setControl(V4L2Control.V4L2_CID_VFLIP, 1);
		if (res != 0)
			log.error("Can't setControl:" + V4L2Control.V4L2_CID_VFLIP
					+ " res:" + res);
		setResolution(Resolution.QVGA);
		setFPS(30);
		cameraId = CameraID.TOP;

		// 3 = 画像のリングバッファ数.
		log.trace("NaojiCamera VideoDev.init(3)");
		res = video.init(3);
		if (res != 3)
			log.fatal("Video initialization failed:" + res);
		log.trace("NaojiCamera VideoDev.start()");
		res = video.start();
		if (res != 0)
			log.error("Can't start videodev:" + res);

		V4L2Image img = new V4L2Image(video);
		log.trace("NaojiCamera test updateImage");
		updateImage(img);
		img.dispose();

		log.trace("NaojiCamera VideoDev.stop()");
		res = video.stop();
		if (res != 0)
			log.error("Can't stop videodev:" + res);

		log.debug("NaojiCamera init bottom camera");
		res = i2c.selectCamera(NaoV3R.I2C_CAMERA_BOTTOM);
		if (res != 0)
			log.error("Can't selectCamera:" + NaoV3R.I2C_CAMERA_BOTTOM
					+ " res:" + res);

		if (i2c.getSelectedCamera() != NaoV3R.I2C_CAMERA_BOTTOM) {
			log.error("Can't selectCamera expected BOTTOM but "
					+ i2c.getSelectedCamera());
		}

		res = video.setControl(V4L2Control.V4L2_CID_CAM_INIT, 0);
		if (res != 0)
			log.error("Can't setControl:" + V4L2Control.V4L2_CID_CAM_INIT
					+ " res:" + res);
		res = video.setControl(V4L2Control.V4L2_CID_AUTOEXPOSURE, 0);
		if (res != 0)
			log.error("Can't setControl:" + V4L2Control.V4L2_CID_AUTOEXPOSURE
					+ " res:" + res);
		res = video.setControl(V4L2Control.V4L2_CID_AUTO_WHITE_BALANCE, 0);
		if (res != 0)
			log.error("Can't setControl:"
					+ V4L2Control.V4L2_CID_AUTO_WHITE_BALANCE + " res:" + res);
		res = video.setControl(V4L2Control.V4L2_CID_AUTOGAIN, 0);
		if (res != 0)
			log.error("Can't setControl:" + V4L2Control.V4L2_CID_AUTOGAIN
					+ " res:" + res);
		res = video.setControl(V4L2Control.V4L2_CID_HFLIP, 0);
		if (res != 0)
			log.error("Can't setControl:" + V4L2Control.V4L2_CID_HFLIP
					+ " res:" + res);
		res = video.setControl(V4L2Control.V4L2_CID_VFLIP, 0);
		if (res != 0)
			log.error("Can't setControl:" + V4L2Control.V4L2_CID_VFLIP
					+ " res:" + res);
		setResolution(Resolution.QVGA);
		// setFPS(30);
		log.trace("NaojiCamera VideoDev.start()");
		res = video.start();
		if (res != 0)
			log.error("Can't start videodev:" + res);

		log.trace("NaojiCamera test updateImage");
		updateImage(img);
		img.dispose();

		log.trace("NaojiCamera VideoDev.stop()");
		res = video.stop();
		if (res != 0)
			log.error("Can't stop videodev:" + res);

		log.trace("NaojiCamera select top camera");
		i2c.selectCamera(NaoV3R.I2C_CAMERA_TOP);

		log.debug("NaojiCamera start");
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
	 * HFOV = W/D*DFOV = 0.81, VFOV = H/D*DFOV = 0.607067
	 *
	 * となる.
	 *
	 */
	public float getHorizontalFieldOfView() {
		return 0.81f;
	}

	@Override
	public int getParam(CameraID camera, CameraParam id) {
		// FIXME CameraID is ignored.
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
		log.trace("selectCamera:" + id);
		nextCameraId = id;
	}

	private void switchCamera(CameraID id) {
		cameraId = id;

		log.trace("stop video");
		video.stop();
		switch (id) {
		case TOP:
			log.debug("select TOP Camera.");
			i2c.selectCamera(NaoV3R.I2C_CAMERA_TOP);
			break;
		case BOTTOM:
			log.debug("select BOTTOM Camera.");
			i2c.selectCamera(NaoV3R.I2C_CAMERA_BOTTOM);
			break;
		default:
			log.error("Unknown CameraID" + id);
			assert false : id;
		}
		// video.setControl(V4L2Control.V4L2_CID_CAM_INIT, 0);
		restoreParam(id);
		// setResolution(Resolution.QVGA);
		// setFPS(30);

		log.trace("start video.");
		video.start();
		log.trace("video started.");
	}

	public void setFPS(int fps) {
		log.debug("setFPS:" + fps);
		int res = video.setFPS(fps);
		if (res != 0)
			log.error("set FPS failed. fps:" + fps + " code:" + res);
		this.fps = fps;
	}

	@Override
	public void setParam(CameraID cameraId, CameraParam id, int value) {
		V4L2Control ctrl = mapV4L2Control(id);

		log.debug("setParam camera:" + cameraId + " param:" + id + " value:"
				+ value);
		if (cameraId == getSelectedId()) {
			int res = video.setControl(ctrl, value);
			if (res != 0)
				log.error("set Param failed. id:" + id + " value:" + value
						+ " code:" + res);
		}
		params.get(cameraId).put(ctrl, value);
	}

	public void setResolution(Resolution resolution) {
		log.debug("setResolution:" + resolution);
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
		log.trace("updateImage " + imgObj);
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
			ctrl = V4L2Control.V4L2_CID_AUTOEXPOSURE;
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

	private void restoreParam(CameraID id) {
		EnumMap<V4L2Control, Integer> map = params.get(id);
		for (V4L2Control ctrl : map.keySet())
			video.setControl(ctrl, map.get(ctrl));
	}
}
