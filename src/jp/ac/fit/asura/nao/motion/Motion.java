/*
 * 作成日: 2008/04/24
 */
package jp.ac.fit.asura.nao.motion;

import jp.ac.fit.asura.nao.RobotContext;

/**
 * @author $Author: sey $
 * 
 * @version $Id: Motion.java 709 2008-11-23 07:40:31Z sey $
 * 
 */
public abstract class Motion {
	private int id;
	protected String name;
	protected int totalFrames;
	protected int currentStep;

	public Motion() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void start() {
		currentStep = 0;
	}

	public void stop() {
		currentStep = -1;
	}

	public void init(RobotContext context) {
	}

	public boolean canStop() {
		return currentStep == totalFrames || currentStep <= 0;
	}

	public void requestStop() {
	}

	public void continueMotion() {
	}

	public int getTotalFrames() {
		return totalFrames;
	}

	public boolean hasNextStep() {
		return totalFrames > currentStep;
	}

	public abstract float[] stepNextFrame(float[] current);
}
