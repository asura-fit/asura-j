/*
 * 作成日: 2008/04/24
 */
package jp.ac.fit.asura.nao.localization;

import java.util.HashMap;
import java.util.Map;

import jp.ac.fit.asura.nao.RobotContext;
import jp.ac.fit.asura.nao.RobotLifecycle;
import jp.ac.fit.asura.nao.vision.VisualObjects;

/**
 * @author sey
 * 
 * @version $Id$
 * 
 */
public class Localization implements RobotLifecycle {
	private Map<WorldObjects, WorldObject> map;
	private RobotContext context;

	public Localization() {
		map = new HashMap<WorldObjects, WorldObject>();
		map.put(WorldObjects.Ball, new WorldObject());
	}

	public void init(RobotContext rctx) {
		this.context = rctx;
	}

	public void start() {
		// TODO Auto-generated method stub

	}

	public void step() {
		map.get(WorldObjects.Ball).setVision(context.getVision().get(VisualObjects.Ball));
	}

	public void stop() {
		// TODO Auto-generated method stub

	}

	public WorldObject get(WorldObjects wo) {
		return map.get(wo);
	}
}
