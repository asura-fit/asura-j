/*
 * 作成日: 2009/05/02
 */
package jp.ac.fit.asura.nao.glue.naimon;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.event.VisualEventListener;
import jp.ac.fit.asura.nao.localization.Localization;
import jp.ac.fit.asura.nao.localization.WorldObject;
import jp.ac.fit.asura.nao.localization.WorldObjects;
import jp.ac.fit.asura.nao.localization.self.MonteCarloLocalization;
import jp.ac.fit.asura.nao.localization.self.SelfLocalization;
import jp.ac.fit.asura.nao.localization.self.MonteCarloLocalization.Candidate;
import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualCortex;

import org.apache.log4j.Logger;

/**
 * @author sey
 *
 * @version $Id: $
 *
 */
public class NaimonLocalizationServlet extends HttpServlet {
	private static final Logger log = Logger
			.getLogger(NaimonLocalizationServlet.class);
	private RobotContext robotContext;

	public NaimonLocalizationServlet(RobotContext context) {
		this.robotContext = context;
	}

	protected void doGet(final HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		log.info("process Localization request");
		resp.setStatus(HttpServletResponse.SC_OK);
		resp.setContentType("application/octet-stream");

		String samplesStr = req.getParameter("mcl.samples");
		final int samples;
		if (samplesStr != null && !samplesStr.equals("")) {
			samples = Integer.parseInt(samplesStr);
			assert samples >= 0;
		} else {
			samples = 32;
		}

		final Localization loc = robotContext.getLocalization();
		OutputStream os = resp.getOutputStream();
		final DataOutputStream dos = new DataOutputStream(os);
		final Object lock = new Object();

		VisualEventListener listener = new VisualEventListener() {
			public void updateVision(VisualContext context) {
				try {
					dos.writeInt(WorldObjects.values().length);
					for (WorldObjects e : WorldObjects.values()) {
						WorldObject wo = loc.get(e);
						dos.writeInt(e.ordinal());
						dos.writeInt(wo.getWorldX());
						dos.writeInt(wo.getWorldY());
						dos.writeInt(wo.getDistance());
						dos.writeFloat(wo.getHeading());
						dos.writeInt(wo.getConfidence());
						dos.writeFloat(wo.getYaw());
					}
					SelfLocalization self = loc.getSelf();
					if (self instanceof MonteCarloLocalization) {
						MonteCarloLocalization mcl = (MonteCarloLocalization) self;
						Candidate[] candidates = mcl.getCandidates();
						int size = samples;
						if (candidates.length < size)
							size = candidates.length;
						dos.writeInt(size);
						for (int i = 0; i < size; i++) {
							Candidate c = candidates[i];
							dos.writeInt(c.x);
							dos.writeInt(c.y);
							dos.writeFloat(c.h);
							dos.writeFloat(c.w);
						}
					} else {
						dos.writeInt(0);
					}
					dos.flush();
				} catch (EOFException e) {
					synchronized (lock) {
						lock.notifyAll();
					}
				} catch (IOException e) {
					log.warn("Connection closed.", e);
					synchronized (lock) {
						lock.notifyAll();
					}
				}
			}
		};

		VisualCortex vc = robotContext.getVision();
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
