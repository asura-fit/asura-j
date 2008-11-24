/*
 * 作成日: 2008/11/23
 */
package jp.ac.fit.asura.nao.glue.naimon;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JPanel;

import jp.ac.fit.asura.nao.vision.VisualContext;
import jp.ac.fit.asura.nao.vision.VisualCortex;

/**
 * @author sey
 * 
 * @version $Id: $
 *
 */
class VisionPanel extends JPanel {
	private VisualCortex vision;
	private BufferedImage image = null;

	public VisionPanel(VisualCortex vision) {
		this.vision = vision;
		image = new BufferedImage(160, 120, BufferedImage.TYPE_INT_RGB);
		setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
	}

	protected void paintComponent(Graphics g) {
		VisualContext vc = vision.getVisualContext();
		byte[] plane = vc.gcdPlane;
		if (plane == null)
			return;

		int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer())
				.getData();
		vision.getGCD().gcd2rgb(plane, pixels);
		g.drawImage(image, 0, 0, Color.black, null);
	}
}

