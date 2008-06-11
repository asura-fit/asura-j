/*
 * 作成日: 2008/05/07
 */
package jp.ac.fit.asura.nao;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public interface Sensor {
	public Image getImage();

	public float getJoint(Joint joint);

}
