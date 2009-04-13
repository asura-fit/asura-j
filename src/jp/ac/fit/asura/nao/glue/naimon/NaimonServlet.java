/*
 * 作成日: 2009/04/11
 */
package jp.ac.fit.asura.nao.glue.naimon;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.IntBuffer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.event.VisualEventListener;
import jp.ac.fit.asura.nao.vision.GCD;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualCortex;

/**
 * @author $Author: sey $
 *
 * @version $Id: $
 *
 */
public class NaimonServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(NaimonServlet.class);
	private RobotContext robotContext;

	public NaimonServlet(RobotContext context) {
		this.robotContext = context;
	}

	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// String path = req.getPathInfo();
		// if (path.equals("")) {
		// resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		// return;
		// }
		log.info("process GCD request");
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("application/octet-stream");
		final VisualCortex vc = robotContext.getVision();
		final OutputStream os = resp.getOutputStream();
		final Object lock = new Object();
		VisualEventListener listener = new VisualEventListener() {
			public void updateVision(VisualContext context) {
				byte[] gcd = context.gcdPlane;
				try {
					os.write(gcd);
				} catch (IOException e) {
					log.warn("Connection closed.", e);
					synchronized (lock) {
						lock.notifyAll();
					}
				}
			}
		};
		try {
			vc.addEventListener(listener);
			synchronized (lock) {
				lock.wait();
			}
		} catch (InterruptedException e) {
		} finally {
			vc.removeEventListener(listener);
		}
		return;
	}
}
