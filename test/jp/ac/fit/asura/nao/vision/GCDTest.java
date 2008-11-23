/*
 * 作成日: 2008/05/24
 */
package jp.ac.fit.asura.nao.vision;

import junit.framework.TestCase;

/**
 * @author $Author: sey $
 * 
 * @version $Id$
 * 
 */
public class GCDTest extends TestCase {
	public void testLoadTMap() throws Exception {
		GCD gcd = new GCD();
		gcd.loadTMap("test/normal.tm2");
		byte[] tmap = gcd.tmap;
		assertEquals(9, tmap[0]);
		assertEquals(9, tmap[1]);
		assertEquals(7, tmap[tmap.length - 2]);
		assertEquals(7, tmap[tmap.length - 1]);
	}
}
