/*
 * 作成日: 2008/11/23
 */
package jp.ac.fit.asura.naimon;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ContainerListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.DataInputStream;
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
	private boolean isBlobOn = true;	//blob表示モード切替
	private BufferedImage image = null;
	private BufferedImage blobImg = null;
	private Graphics2D blbuf = null;
	private byte[] plane;

	//blob colour in vision panel
	private static Color bcBall = new Color(255, 0, 255, 255);
	private static Color bcBGoal = new Color(255, 0, 255, 255);
	private static Color bcYGoal = new Color(255, 0, 255, 255);

	private Rectangle blob = null;

	public VisionPanel() {
		image = new BufferedImage(320, 240, BufferedImage.TYPE_INT_RGB);
		blobImg = new BufferedImage(320, 240, BufferedImage.TYPE_4BYTE_ABGR);
		blbuf = blobImg.createGraphics();
		blbuf.setBackground(new Color(0, 0, 0, 0));
		blob = new Rectangle(0, 0, 0, 0);

		plane = new byte[image.getWidth()*image.getHeight()];
		setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));

		Thread thread = new Thread() {
			public void run() {
				try {
					URL url = new URL("http://192.168.1.53:8080/naimon?threshold=15" + "");
					InputStream is = url.openStream();
					DataInputStream dis = new DataInputStream(is);
					int read = 0;
					int offset = 0;
					int length = plane.length;
					while ((read = dis.read(plane, offset, length)) > 0) {
						offset += read;
						length -= read;
						if (length == 0) {
							int[] pixels = ((DataBufferInt) image.getRaster()
									.getDataBuffer()).getData();
							GCD.gcd2rgb(plane, pixels);
							offset = 0;
							length = plane.length;

							//blob読み
							blbuf.clearRect(0, 0, 320, 240);

							for (int i=0; i<3; i++) {
								byte c;
								int n;
								int xmax, ymax;

								c = dis.readByte();
								n = dis.readInt();
								drawType(c, n , 1, 10+13*i, 45);
								for (int j=0; j<n; j++) {
									blob.x = dis.readInt();
									xmax = dis.readInt();
									blob.y = dis.readInt();
									ymax = dis.readInt();
									blob.width = xmax - blob.x;
									blob.height = ymax - blob.y;
									drawBlob(blob, c);
									drawBallBlobSize(c, n, blob);
								}
							}
							//ball distance
							drawStr("bDist: " + dis.readInt(), 1, 238, 65);

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
		if (isBlobOn)
			g.drawImage(blobImg, 0, 0, null, null);
	}

	protected void drawType(byte c, int n, int x, int y, int w) {
		switch (c) {
		case GCD.cORANGE:
			drawStr("Bl: " + n, x, y, w);
			break;
		case GCD.cCYAN:
			drawStr("Bg: " + n, x, y, w);
			break;
		case GCD.cYELLOW:
			drawStr("Yg: " + n, x, y, w);
			break;
		}
	}

	protected void drawBallBlobSize(byte c, int n, Rectangle b) {
		if (c != GCD.cORANGE)
			return;

		drawStr("bW: " + b.width, 1, 225, 50);
		drawStr("bH: " + b.height, 1, 212, 50);
	}

	protected void drawStr(String str, int x, int y, int w) {
		blbuf.setColor(new Color(0, 0, 0, 100));
		blbuf.fillRect(x-2, y-11, w, 13);
		blbuf.setColor(new Color(0xFFFFFF));
		blbuf.drawString(str, x, y);
	}

	protected void drawBlob(Rectangle b, byte c) {
		switch (c) {
		case GCD.cORANGE:
			blbuf.setColor(bcBall);
			break;
		case GCD.cCYAN:
			blbuf.setColor(bcBGoal);
			break;
		case GCD.cYELLOW:
			blbuf.setColor(bcYGoal);
			break;
		}

		blbuf.drawRect(b.x, b.y, b.width, b.height);
//		blbuf.drawRect(xmin-1, ymin-1, xmax - xmin+2, ymax - ymin+2);

//		System.out.println("xmin: " + xmin);
//		System.out.println("xmax: " + xmax);
//		System.out.println("ymin: " + ymin);
//		System.out.println("ymax: " + ymax);
	}
}
