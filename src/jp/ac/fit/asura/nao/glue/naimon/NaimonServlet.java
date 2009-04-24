/*
 * 作成日: 2009/04/11
 */
package jp.ac.fit.asura.nao.glue.naimon;

import static jp.ac.fit.asura.nao.vision.VisualObjects.Ball;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.event.VisualEventListener;
import jp.ac.fit.asura.nao.vision.GCD;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualCortex;
import jp.ac.fit.asura.nao.vision.perception.BallVisualObject;
import jp.ac.fit.asura.nao.vision.perception.BlobVision;
import jp.ac.fit.asura.nao.vision.perception.BlobVision.Blob;

import org.apache.log4j.Logger;

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

	protected void doGet(final HttpServletRequest req, HttpServletResponse resp)
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
		final DataOutputStream dos = new DataOutputStream(os);
		final Object lock = new Object();

		VisualEventListener listener = new VisualEventListener() {
			public void updateVision(VisualContext context) {
				// 実機は320x240, webotsは160x120
				byte[] gcd = context.gcdPlane;
				// blob数

				try {
					// gcdデータを流す
					os.write(gcd);

					// blob
					int threshold = 5;
					String thStr = req.getParameter("threshold");
					if (thStr != null)
						threshold = Integer.parseInt(thStr);
					// blob流すぜ
					writeBlobs(dos, GCD.cORANGE, context.blobVision.findBlobs(
							GCD.cORANGE, BlobVision.MAX_BLOBS, threshold));
					writeBlobs(dos, GCD.cCYAN, context.blobVision.findBlobs(
							GCD.cCYAN, BlobVision.MAX_BLOBS, threshold));
					writeBlobs(dos, GCD.cYELLOW, context.blobVision.findBlobs(
							GCD.cYELLOW, BlobVision.MAX_BLOBS, threshold));

					// ballDistance
					dos
							.writeInt(((BallVisualObject) context.get(Ball)).distance);

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

	private void writeBlobs(DataOutputStream dos, byte c, List<Blob> list)
			throws IOException {
		dos.write(c & 0xFF);
		dos.writeInt(list.size());
		// log.debug("NaimonServlet: color is " + (c&0xFF));
		// log.debug("NaimonServlet: list.size() is " + list.size());

		for (Blob b : list) {
			dos.writeInt(b.xmin);
			dos.writeInt(b.xmax);
			dos.writeInt(b.ymin);
			dos.writeInt(b.ymax);

		}
	}
}
