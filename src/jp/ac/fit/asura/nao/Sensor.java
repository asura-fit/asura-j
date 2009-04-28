/*
 * 作成日: 2008/05/07
 */
package jp.ac.fit.asura.nao;

/**
 * @author $Author: sey $
 *
 * @version $Id: Sensor.java 717 2008-12-31 18:16:20Z sey $
 * @param <T>
 *
 */
public interface Sensor {
	public enum Function {
		ACCEL, GYRO, GPS, JOINT_ANGLE, JOINT_FORCE, FORCE, SWITCH, INERTIAL
	}

	/**
	 * センサーバッファを作成します.
	 *
	 * @return
	 */
	public SensorContext create();

	/**
	 * センサーバッファの内容を現在のセンサー値でアップデートします.
	 *
	 * 与えられるセンサーバッファは{@link #create()}によって作成されたものでなければなりません.
	 *
	 * @param context
	 */
	public void update(SensorContext context);

	/**
	 * 新たなセンサー値が得られるまで待機します.
	 */
	public void poll();

	/**
	 * funcで示される機能がこのセンサーで利用可能かどうかを問い合わせます.
	 *
	 * @param func
	 *            問い合わせるセンサーの機能
	 * @return 機能が利用可能かどうか. 利用可能であればtrue,そうでなければfalse.
	 * @see Function
	 */
	public boolean isSupported(Function func);

	public void init();

	public void before();

	public void after();
}
