/*
 * 作成日: 2008/11/23
 */
package jp.ac.fit.asura.naimon;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.swing.JPanel;

import jp.ac.fit.asura.nao.vision.GCD;

/**
 * @author sey
 *
 * @version $Id: $
 *
 */
class VisionPanel extends JPanel {
	private BufferedImage image = null;
	private byte[] plane;

	public VisionPanel() {
		image = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
		plane = new byte[image.getWidth()*image.getHeight()];
		setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
		Thread thread = new Thread() {
			public void run() {
				try {
					URL url = new URL("http://192.168.1.51:8080/naimon");
					InputStream is = url.openStream();
					int read = 0;
					int offset = 0;
					int length = plane.length;
					while ((read = is.read(plane, offset, length)) > 0) {
						offset += read;
						length -= read;
						if (length == 0) {
							int[] pixels = ((DataBufferInt) image.getRaster()
									.getDataBuffer()).getData();
							GCD.gcd2rgb(plane, pixels);
							offset = 0;
							length = plane.length;
							repaint();
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();
	}

	protected void paintComponent(Graphics g) {
		g.drawImage(image, 0, 0, Color.black, null);
	}
}
