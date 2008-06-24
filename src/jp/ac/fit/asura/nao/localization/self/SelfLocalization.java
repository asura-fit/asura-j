/*
 * 作成日: 2008/06/23
 */
package jp.ac.fit.asura.nao.localization.self;

import jp.ac.fit.asura.nao.RobotLifecycle;

/**
 * @author sey
 * 
 * @version $Id: $
 *
 */
public abstract class SelfLocalization implements RobotLifecycle {
	public abstract void reset();
	public abstract int getX();
	public abstract int getY();
	public abstract float getHeading();
	public abstract int getConfidence();
}
