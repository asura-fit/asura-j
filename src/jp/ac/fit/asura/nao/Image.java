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
public class Image {
	private int width;
	private int height;
	private int[] data;

	public Image(int[] data, int width, int height) {
		this.data = data;
		this.width = width;
		this.height = height;
	}

	/**
	 * @return the data
	 */
	public int[] getData() {
		return data;
	}
	
	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}
}
