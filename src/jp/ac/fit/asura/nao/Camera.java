/*
 * 作成日: 2009/03/27
 */
package jp.ac.fit.asura.nao;

/**
 * カメラのインターフェイス.
 *
 * TODO 複数のカメラは一つのCameraインスタンスではなく複数のCameraインスタンスで管理する.
 *
 * @author sey
 *
 * @version $Id: $
 *
 */
public interface Camera {
	@Deprecated
	public enum CameraType {
		WEBOTS6, V4L2
	}

	public enum CameraID {
		TOP, BOTTOM;
	}

	public enum CameraParam {
		AWB, AGC, AEC, Brightness, Exposure, Gain, Contrast, Saturation, Hue, RedChroma, BlueChroma,
	}

	public enum PixelFormat {
		RGB444, // Webotsで使用可能.
		YUYV, // NaoV3Rで使用可能.
	}

	public enum Resolution {
		VGA(640, 480), QVGA(320, 240), QQVGA(160, 120);

		private int w;
		private int h;

		private Resolution(int w, int h) {
			this.w = w;
			this.h = h;
		}

		public int getWidth() {
			return w;
		}

		public int getHeight() {
			return h;
		}
	}

	public Image createImage();

	@Deprecated
	public CameraType getType();

	public float getHorizontalFieldOfView();

	public float getVerticalFieldOfView();

	public int getFPS();

	public int getParam(CameraID camera, CameraParam id);

	public Resolution getResolution();

	public CameraID getSelectedId();

	public boolean isSupported(CameraParam id);

	public boolean isSupported(Resolution id);

	public boolean isSupportedFPS(int fps);

	public void selectCamera(CameraID id);

	public void setFPS(int fps);

	public void setParam(CameraID camera, CameraParam id, int value);

	public void setResolution(Resolution res);

	public void updateImage(Image img);

	public void init();

	public void before();

	public void after();
}
