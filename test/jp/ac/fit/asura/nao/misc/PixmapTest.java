/*
 * 作成日: 2008/05/24
 */
package jp.ac.fit.asura.nao.misc;

import jp.ac.fit.asura.nao.misc.TMap;
import junit.framework.TestCase;

/**
 * @author $Author: sey $
 *
 * @version $Id: GCDTest.java 709 2008-11-23 07:40:31Z sey $
 *
 */
public class PixmapTest extends TestCase {
	public void testTMap() throws Exception {
		TMap ppm = new TMap("test/normal.tm2");
		byte[] tmap = ppm.getData();
		assertEquals(9, tmap[0]);
		assertEquals(9, tmap[1]);
		assertEquals(7, tmap[tmap.length - 2]);
		assertEquals(7, tmap[tmap.length - 1]);
	}
}
