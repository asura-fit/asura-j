/*
 * 作成日: 2008/06/11
 */
package jp.ac.fit.asura.nao.localization;

import jp.ac.fit.asura.nao.vision.objects.VisualObject;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class WorldObject {
	private VisualObject vision;

	public WorldObject() {
	}

	public VisualObject getVision() {
		return vision;
	}

	protected void setVision(VisualObject vision) {
		this.vision = vision;
	}
}
