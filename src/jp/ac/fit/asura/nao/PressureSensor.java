/*
 * 作成日: 2008/07/02
 */
package jp.ac.fit.asura.nao;

/**
 * @author $Author$
 *
 * @version $Id$
 *
 */
public enum PressureSensor {
	LSoleFL("LFsrFL"), LSoleFR("LFsrFR"), LSoleBR("LFsrBR"), LSoleBL("LFsrBL"), RSoleFL(
			"RFsrFL"), RSoleFR("RFsrFR"), RSoleBR("RFsrBR"), RSoleBL("RFsrBL");

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
