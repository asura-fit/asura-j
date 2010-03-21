/*
 * 作成日: 2008/05/12
 */
package jp.ac.fit.asura.nao.glue;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.glue.naimon.Naimon2Servlet;
import jp.ac.fit.asura.nao.glue.naimon.NaimonLocalizationServlet;
import jp.ac.fit.asura.nao.glue.naimon.NaimonServlet;
import jp.ac.fit.asura.nao.glue.naimon.NaimonValuesServlet;
import jscheme.SchemeException;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;

public class TinyHttpd {
	private static final Logger log = Logger.getLogger(TinyHttpd.class);
	private Server server;
	private RobotContext robotContext;
	private SchemeGlue glue;

	public TinyHttpd() {
	}

	public void init(RobotContext context) {
		this.glue = context.getGlue();
		this.robotContext = context;
		this.server = null;
	}

	public void start(int port) {
		log.info("Start httpd.");
		server = new Server();
		Connector connector = new SocketConnector();
		connector.setPort(port);
		server.setConnectors(new Connector[] { connector });

		ServletHandler handler = new ServletHandler();
		server.setHandler(handler);

		// ServletHolder holder1 = new ServletHolder(new NaimonServlet(
		// robotContext));
		// handler.addServletWithMapping(holder1, "/naimon");

		ServletHolder naimon2 = new ServletHolder(new Naimon2Servlet(
				robotContext));
		handler.addServletWithMapping(naimon2, "/naimon2/*");

		ServletHolder holder2 = new ServletHolder(new SchemeServlet());
		handler.addServletWithMapping(holder2, "/");
		handler.addServletWithMapping(holder2, "/xscheme");

		// ServletHolder holder3 = new ServletHolder(new NaimonValuesServlet(
		// robotContext));
		// handler.addServletWithMapping(holder3, "/naimon/values");

		// ServletHolder holder4 = new ServletHolder(
		// new NaimonLocalizationServlet(robotContext));
		// handler.addServletWithMapping(holder4, "/naimon/localization");

		ServletHolder kinematicsServlet = new ServletHolder(
				new KinematicsServlet(robotContext));
		handler.addServletWithMapping(kinematicsServlet, "/kinematics");

		try {
			server.start();
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public void stop() {
		try {
			server.stop();
			server.destroy();
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public boolean isRunning() {
		return server != null && server.isRunning();
	}

	class SchemeServlet extends HttpServlet {
		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp)
				throws ServletException, IOException {
			doGet(req, resp);
		}

		@Override
		protected void doGet(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			if (request.getServletPath().equals("/xscheme")) {
				processXMLRequest(request, response);
			} else {
				processHTMLRequest(request, response);
			}
		}

		protected void processHTMLRequest(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			String eval = request.getParameter("eval");
			if (eval != null) {
				glue.load(eval.trim());
			}

			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
			PrintWriter w = response.getWriter();
			w.println("<html><head><title>TinyHttpd</title></head>");
			w.println("<body><form method=\"post\" action=\"/\">");
			w.println("<textarea name=\"eval\"rows=\"30\" cols=\"100\">");
			if (eval != null)
				w.println(eval);
			w.println("</textarea>");
			w.println("<input type=\"submit\">");
			w.println("</form></body></html>");
		}

		protected void processXMLRequest(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			response.setContentType("text/xml");
			response.setStatus(HttpServletResponse.SC_OK);
			PrintWriter w = response.getWriter();
			w.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			w.println("<result>");

			String load = request.getParameter("load");
			String eval = request.getParameter("eval");
			try {
				if (load != null) {
					boolean isSucceeded = glue.load(load.trim());
					w.println(isSucceeded);
				} else if (eval != null) {
					Object evalResult = glue.eval(eval.trim());
					w.println(evalResult);
				}
			} catch (SchemeException e) {
				w.println("<exception class=" + e.getClass() + ">");
				w.println(e);
				w.println("</exception>");
				log.error("", e);
			}

			w.println("</result>");
		}
	}
}
