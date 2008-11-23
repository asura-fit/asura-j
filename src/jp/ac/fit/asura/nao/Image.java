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
	private float horizontalFieldOfView;
	private float verticalFieldOfView;

	public Image(int[] data, int width, int height,
			float horizontalFieldOfView, float verticalFieldOfView) {
		this.data = data;
		this.width = width;
		this.height = height;
		this.horizontalFieldOfView = horizontalFieldOfView;
		this.verticalFieldOfView = verticalFieldOfView;
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

	/**
	 * @return the horizontalFieldOfView
	 */
	public float getHorizontalFieldOfView() {
		return horizontalFieldOfView;
	}

	/**
	 * @return the verticalFieldOfView
	 */
	public float getVerticalFieldOfView() {
		return verticalFieldOfView;
	}
}
