/**
 *
 */
package jp.ac.fit.asura.nao;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import jp.ac.fit.asura.nao.Camera.PixelFormat;
import jp.ac.fit.asura.nao.misc.MathUtils;
import junit.framework.TestCase;

/**
 * @author sey
 *
 * @version $Id: AsuraCoreTest.java 717 2008-12-31 18:16:20Z sey $
 *
 */
public class AsuraCoreTest extends TestCase {
	public static DatagramService createDatagramServiceStub() {
		return new DatagramService() {
			public byte[] receive() {
				return null;
			}

			public void receive(ByteBuffer buf) {
			}

			public void send(ByteBuffer buf) {
			}
		};
	}

	public static Effector createEffectorStub() {
		return new Effector() {
			public void setJoint(Joint joint, float valueInRad) {
			}

			@Override
			public void setJoint(Joint joint, float valueInRad,
					int durationInMills) {
			}

			public void setJointDegree(Joint joint, float valueInDeg) {
			}

			public void setJointMicro(Joint joint, int valueInMicroRad) {
			}

			@Override
			public void setJoint(Joint joint, float[] angleValues,
					int[] durationInMills) {
			}

			@Override
			public void setBodyJoints(float[] angleMatrix, int[] durationInMills) {
			}

			public void setForce(Joint joint, float valueTorque) {
			}

			@Override
			public void setLed(String ledName, float luminance) {
			}

			public void init() {
			}

			public void after() {
			}

			public void before() {
			}

			public void setPower(float power) {
			}

			public void setPower(Joint joint, float power) {
			}
		};
	}

	public static Sensor createSensorStub(final float[] values) {
		return new Sensor() {
			@Override
			public void init() {
			}

			@Override
			public void after() {
			}

			@Override
			public void before() {
			}

			@Override
			public SensorContext create() {
				return new TestSensorContext();
			}

			@Override
			public void poll() {
				try {
					Thread.sleep(40);
				} catch (Exception e) {
				}
			}

			@Override
			public void update(SensorContext context) {
			}

			@Override
			public boolean isSupported(Function func) {
				return false;
			}
		};
	}

	static class TestSensorContext extends SensorContext {
		public float getAccelX() {
			return 0;
		}

		public float getAccelY() {
			return 0;
		}

		public float getAccelZ() {
			return 0;
		}

		public float getGyroX() {
			return 0;
		}

		public float getGyroZ() {
			return 0;
		}

		public float[] getJointAngles() {
			return new float[Joint.values().length];
		}

		public float getJointDegree(Joint joint) {
			return MathUtils.toDegrees(getJoint(joint));
		}

		public float getForce(PressureSensor ts) {
			return 4.0f;
		}

		public float getForce(Joint joint) {
			return 0;
		}

		@Override
		public float getJoint(Joint joint) {
			return 0;
		}

		@Override
		public long getTime() {
			return 0;
		}
	};

	public static Camera createCameraStub() {
		return new Camera() {
			public Image createImage() {
				IntBuffer buf = IntBuffer.allocate(9);
				return AsuraCoreTest.createImage(buf, 3, 3, 0L);
			}

			public int getFPS() {
				return 1;
			}

			public float getHorizontalFieldOfView() {
				return 0.8f;
			}

			@Override
			public int getParam(CameraID camera, CameraParam id) {
				return 0;
			}

			public Resolution getResolution() {
				return null;
			}

			public CameraID getSelectedId() {
				return null;
			}

			public CameraType getType() {
				return null;
			}

			public float getVerticalFieldOfView() {
				return 0.8f;
			}

			public boolean isSupported(CameraParam id) {
				return false;
			}

			public boolean isSupported(Resolution id) {
				return false;
			}

			public boolean isSupportedFPS(int fps) {
				return false;
			}

			public void selectCamera(CameraID id) {
			}

			public void setFPS(int fps) {
			}

			@Override
			public void setParam(CameraID camera, CameraParam id, int value) {
			}

			public void setResolution(Resolution res) {
			}

			public void updateImage(Image img) {
				IntBuffer buf = img.getIntBuffer();
				buf.clear();
				buf.put(Color.black.getRGB());
				buf.put(Color.blue.getRGB());
				buf.put(Color.cyan.getRGB());
				buf.put(Color.darkGray.getRGB());
				buf.put(Color.GRAY.getRGB());
				buf.put(Color.lightGray.getRGB());
				buf.put(Color.magenta.getRGB());
				buf.put(Color.orange.getRGB());
				buf.put(Color.pink.getRGB());
				buf.flip();

			}

			public void init() {
			}

			public void after() {
			}

			public void before() {
			}
		};
	}

	public static Image createImage(final IntBuffer buf, final int width,
			final int height, final long time) {
		return new Image() {
			public BufferType getBufferType() {
				return BufferType.INT;
			}

			public ByteBuffer getByteBuffer()
					throws UnsupportedOperationException {
				throw new UnsupportedOperationException();
			}

			public int getHeight() {
				return height;
			}

			public IntBuffer getIntBuffer()
					throws UnsupportedOperationException {
				return buf;
			}

			public PixelFormat getPixelFormat() {
				return PixelFormat.RGB444;
			}

			public long getTimestamp() {
				return time;
			}

			public int getWidth() {
				return width;
			}

			public void dispose() {
			}
		};
	}

	public static AsuraCore createCore() {
		AsuraCore core = new AsuraCore(createEffectorStub(),
				createSensorStub(new float[Joint.values().length]),
				createDatagramServiceStub(), createCameraStub());
		return core;
	}

	public void testCore() throws Exception {
		AsuraCore core = createCore();
		core.init();
	}
}
