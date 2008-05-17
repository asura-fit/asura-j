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

import jscheme.JScheme;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;

public class TinyHttpd {
	private Server server;
	private JScheme js;

	public TinyHttpd(JScheme js) {
		this.js = js;
		this.server = null;
	}

	public void start(int port) {
		server = new Server();
		Connector connector = new SocketConnector();
		connector.setPort(port);
		server.setConnectors(new Connector[] { connector });

		ServletHandler handler = new ServletHandler();
		server.setHandler(handler);

		ServletHolder holder = new ServletHolder(new TestServlet(js));
		handler.addServletWithMapping(holder, "/");

		try {
			server.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void stop() {
		try {
			server.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isRunning() {
		return server != null && server.isRunning();
	}

	public static class TestServlet extends HttpServlet {
		JScheme js = null;

		public TestServlet(JScheme js) {
			this.js = js;
		}

		protected void doGet(HttpServletRequest request,
				HttpServletResponse response) throws ServletException,
				IOException {
			String eval = request.getParameter("eval");
			if (eval != null) {
				js.eval(eval);
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
