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
import jp.ac.fit.asura.nao.glue.naimon.NaimonServlet;
import jscheme.JScheme;

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

		ServletHolder holder1 = new ServletHolder(new NaimonServlet(
				robotContext));
		handler.addServletWithMapping(holder1, "/naimon");

		ServletHolder holder2 = new ServletHolder(new SchemeServlet());
		handler.addServletWithMapping(holder2, "/");

		try {
			server.start();
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public void stop() {
		try {
			server.stop();
		} catch (Exception e) {
			log.error("", e);
		}
	}

	public boolean isRunning() {
		return server != null && server.isRunning();
	}

	class SchemeServlet extends HttpServlet {
		protected void doGet(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			String eval = request.getParameter("eval");
			if (eval != null) {
				glue.eval(eval);
			}

			response.setContentType("text/html");
			response.setStatus(HttpServletResponse.SC_OK);
			PrintWriter w = response.getWriter();
			w.println("<html><head><title>TinyHttpd</title></head>");
			w.println("<body><form method=\"get\" action=\"/\">");
			w.println("<textarea name=\"eval\"rows=\"30\" cols=\"100\">");
			if (eval != null)
				w.println(eval);
			w.println("</textarea>");
			w.println("<input type=\"submit\">");
			w.println("</form></body></html>");
		}
	}
}
