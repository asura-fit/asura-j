/*
 * 作成日: 2008/04/24
 */
package jp.ac.fit.asura.nao.localization;

import java.util.HashMap;
import java.util.Map;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class Localization implements RobotLifecycle {
	private Map<WorldObjects, WorldObject> map;

	public Localization() {
		map = new HashMap<WorldObjects, WorldObject>();
	}

	public void init(RobotContext rctx) {
		// TODO Auto-generated method stub

	}

	public void start() {
		// TODO Auto-generated method stub

	}

	public void step() {
		// TODO Auto-generated method stub

	}

	public void stop() {
		// TODO Auto-generated method stub

	}

	public WorldObject get(WorldObjects wo) {
		return map.get(wo);
	}
}
