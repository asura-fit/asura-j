/*
 * 作成日: 2008/04/24
 */
package jp.ac.fit.asura.nao.motion;

/**
 * @author $Author$
 * 
 * @version $Id$
 * 
 */
public abstract class Motion {
	protected String name;
	protected int totalFrames;
	protected int currentStep;

	public Motion() {
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
	
	public boolean canStop(){
		return currentStep == totalFrames || currentStep <= 0;
	}

	public int getTotalFrames() {
		return totalFrames;
	}

	public boolean hasNextStep() {
		return totalFrames > currentStep;
	}

	public abstract float[] stepNextFrame(float[] current);
}
