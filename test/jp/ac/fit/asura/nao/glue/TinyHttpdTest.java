/*
 * 作成日: 2008/05/14
 */
package jp.ac.fit.asura.nao.glue;

import java.net.URL;

import jscheme.JScheme;
import junit.framework.TestCase;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class TinyHttpdTest extends TestCase {
	public void testHttpd() throws Exception {
		JScheme js = new JScheme();
		TinyHttpd httpd = new TinyHttpd(js);
		assertFalse(httpd.isRunning());
		httpd.start(8080);
		assertTrue(httpd.isRunning());
		Thread.sleep(1000*100);
		new URL("http://localhost:8080/?eval=(display%20%22test%22)").openStream().read();
		httpd.stop();
	}
}
