/*
 * 作成日: 2008/05/24
 */
package jp.ac.fit.asura.nao.vision;

/**
 * @author $Author$
 * 
 * @version $Id$
 *
 */
public class VisionUtils {

	public static final int getBlue(int pix) {
		return (pix & 0x0000FF);
	}

	public static final int getGreen(int pix) {
		return (pix & 0x00FF00) >> 8;
	}

	public static final int getRed(int pix) {
		return (pix & 0xFF0000) >> 16;
	}
}
