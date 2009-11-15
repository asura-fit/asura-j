/**
 */
package jp.ac.fit.asura.nao.misc;

import junit.framework.TestCase;

/**
 * @author $Author: sey $
 *
 * @version $Id: $
 *
 */
public class IntegralFilterTest extends TestCase {
	public void testEval() {
		IntegralFilter.Float f = new IntegralFilter.Float();

		// f(x) = 5x, F(x) = 5*2*x^2
		// assertTrue(Float.isNaN(f.eval(0)));
		// assertTrue(Float.isNaN(f.eval(5*1)));
		// assertTrue(Float.isNaN(f.eval(5*2)));
		// assertEquals(5.0f/2*0*0, f.eval(5*0), 0.0001f);
		assertEquals(5.0f / 2 * 0 * 0, f.eval(5 * 0), 0.0001f);
		assertEquals(5.0f / 2 * 1 * 1, f.eval(5 * 1), 0.0001f);
		// f.eval(5*1);
		assertEquals(5.0f / 2 * 2 * 2, f.eval(5 * 2), 0.0001f);
		assertEquals(5.0f / 2 * 3 * 3, f.eval(5 * 3), 0.0001f);
		// f.eval(5*3);
		assertEquals(5.0f / 2 * 4 * 4, f.eval(5 * 4), 0.0001f);

		f.clear();
		// f(x) = -3x, F(x) = -3*2*x^2
		// f.eval(-3*0);
		// f.eval(-3*1);
		// f.eval(-3*2);
		assertEquals(-3.0f * 0 * 0 / 2, f.eval(-3 * 0), 0.0001f);
		assertEquals(-3.0f * 1 * 1 / 2, f.eval(-3 * 1), 0.0001f);
		assertEquals(-3.0f * 2 * 2 / 2, f.eval(-3 * 2), 0.0001f);
		assertEquals(-3.0f * 3 * 3 / 2, f.eval(-3 * 3), 0.0001f);

		f.clear();
		// f(x) = e^x, F(x) = e^x
		f.eval((float) Math.exp(0));
		f.eval((float) Math.exp(1));
		f.eval((float) Math.exp(2));
		f.eval((float) Math.exp(3));
		// assertEquals(Math.exp(2), f.eval((float) Math.exp(2)), 0.0001f);
		// assertEquals(Math.exp(3), f.eval((float) Math.exp(3)), 0.0001f);
		assertEquals(Math.exp(4), f.eval((float) Math.exp(4)), 10);
		assertEquals(Math.exp(5), f.eval((float) Math.exp(5)), 10);
		assertEquals(Math.exp(6), f.eval((float) Math.exp(6)), 10);

		f.clear();
		// f(x) = 3x^2, F(x) = 3/3*x^3
		// assertEquals(-1 * -1 * -1, f.eval(3 * -1 * -1), 0.0001f);
		f.eval(0);
		// assertEquals(0 * 0 * 0, f.eval(3 * 0 * 0), 0.0001f);
		f.eval(3 * 1 * 1);
		// assertEquals(1 * 1 * 1, f.eval(3 * 1 * 1), 0.0001f);
		assertEquals(2 * 2 * 2, f.eval(3 * 2 * 2), 0.01f);
		assertEquals(3 * 3 * 3, f.eval(3 * 3 * 3), 1.0f);
		assertEquals(4 * 4 * 4, f.eval(3 * 4 * 4), 0.01f);
	}
}
