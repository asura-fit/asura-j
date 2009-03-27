/*
 * 作成日: 2008/07/02
 */
package jp.ac.fit.asura.nao;

/**
 * @author $Author: sey $
 *
 * @version $Id: PressureSensor.java 709 2008-11-23 07:40:31Z sey $
 *
 */
public enum PressureSensor {
	LFsrFL("LFsrFL"), LFsrFR("LFsrFR"), LFsrBR("LFsrBR"), LFsrBL("LFsrBL"), RFsrFL(
			"RFsrFL"), RFsrFR("RFsrFR"), RFsrBR("RFsrBR"), RFsrBL("RFsrBL");

	private String deviceTag;

	private PressureSensor(String tag) {
		deviceTag = tag;
	}

	/**
	 * @return deviceTag
	 */
	public String getDeviceTag() {
		return deviceTag;
	}
}
