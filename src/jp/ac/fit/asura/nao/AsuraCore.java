/*
 * 作成日: 2008/04/12
 */
package jp.ac.fit.asura.nao;

import jp.ac.fit.asura.nao.motion.MotorCortex;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class AsuraCore {
    public enum Team {
	Red, Blue
    };

    private int id;

    private Team team;

    private int time;

    private Effector effector;

    private MotorCortex motion;

    /**
     * 
     */
    public AsuraCore() {
	effector = new WebotsEffector();
	motion = new MotorCortex(effector);
    }

    /**
     * @param id
     *                the id to set
     */
    public void setId(int id) {
	this.id = id;
    }

    /**
     * @param team
     *                the team to set
     */
    public void setTeam(Team team) {
	this.team = team;
    }

    public void init() {
	time = 0;

	motion.init();
    }

    public void run(int ts) {
	motion.makemotion("walk", null);
	motion.step(ts);
	time += ts;
    }
}
