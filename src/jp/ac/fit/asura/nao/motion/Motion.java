/*
 * 作成日: 2008/04/24
 */
package jp.ac.fit.asura.nao.motion;

import java.util.Date;
import java.util.List;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class Motion {
	private String name;
	private Date timestamp;
	private List<float[]> data;
	private int[] frameStep;

	public String getName() {
		return name;
	}

	public void setName(String id) {
		this.name = id;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public List<float[]> getData() {
		return data;
	}

	public void setData(List<float[]> data) {
		this.data = data;
	}

	public int[] getFrameStep() {
		return frameStep;
	}

	public void setFrameStep(int[] step) {
		this.frameStep = step;
	}

}
