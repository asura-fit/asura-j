/*
 * 作成日: 2008/07/05
 */
package jp.ac.fit.asura.nao.event;

import java.util.EventListener;

import jp.ac.fit.asura.nao.vision.VisualContext;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public interface VisualEventListener extends EventListener {
	public void updateVision(VisualContext context);
}
