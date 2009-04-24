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
	public SensorContext create();

	public void update(SensorContext context);

	public void poll();

	public void init();

	public void before();

	public void after();

}
