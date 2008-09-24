/*
 * 作成日: 2008/09/25
 */
package jp.ac.fit.asura.nao.misc;

import junit.framework.TestCase;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class NDFilterTest extends TestCase {
	public void testEval() {
		NDFilter.Float f = new NDFilter.Float();
		
		// 5x' = 5
		assertTrue(Float.isNaN(f.eval(5)));
		assertTrue(Float.isNaN(f.eval(10)));
		assertEquals(5.0f, f.eval(15), 0.0001f);
		assertEquals(5.0f, f.eval(20), 0.0001f);

		// -3x' = -3
		f.eval(-3);
		f.eval(-6);
		assertEquals(-3.0f, f.eval(-9), 0.0001f);

		// e^x' = e^x
		f.eval((float) Math.exp(2));
		f.eval((float) Math.exp(3));
		assertEquals(Math.exp(4), (float) Math.exp(4), 0.0001f);

		// 3x^2 = 6x
		f.eval(3);
		f.eval(0);
		assertEquals(6f, f.eval(3), 0.0001f);
	}
}
