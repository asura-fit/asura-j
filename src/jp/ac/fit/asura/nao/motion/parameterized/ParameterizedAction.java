/*
 * 作成日: 2008/07/05
 */
package jp.ac.fit.asura.nao.motion.parameterized;

import jp.ac.fit.asura.nao.RobotContext;

@Deprecated
/**
 * このクラスはMotion.start	
 * 
 * @author sey
 * 
 * @version $Id: ParameterizedAction.java 691 2008-09-26 06:40:26Z sey $
 * 
 */
public abstract class ParameterizedAction {
	private int id;
	private String name;

	public ParameterizedAction() {
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

	public void init(RobotContext context) {
	}
}
