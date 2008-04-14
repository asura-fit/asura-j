/*
 * 作成日: 2008/04/13
 */
package jp.ac.fit.asura.nao.motion;

import static jp.ac.fit.asura.nao.Effector.Joint.HeadPitch;
import static jp.ac.fit.asura.nao.Effector.Joint.HeadYaw;
import static jp.ac.fit.asura.nao.Effector.Joint.LAnklePitch;
import static jp.ac.fit.asura.nao.Effector.Joint.LAnkleRoll;
import static jp.ac.fit.asura.nao.Effector.Joint.LElbowRoll;
import static jp.ac.fit.asura.nao.Effector.Joint.LElbowYaw;
import static jp.ac.fit.asura.nao.Effector.Joint.LHipPitch;
import static jp.ac.fit.asura.nao.Effector.Joint.LHipRoll;
import static jp.ac.fit.asura.nao.Effector.Joint.LHipYawPitch;
import static jp.ac.fit.asura.nao.Effector.Joint.LKneePitch;
import static jp.ac.fit.asura.nao.Effector.Joint.LShoulderPitch;
import static jp.ac.fit.asura.nao.Effector.Joint.LShoulderRoll;
import static jp.ac.fit.asura.nao.Effector.Joint.RAnklePitch;
import static jp.ac.fit.asura.nao.Effector.Joint.RAnkleRoll;
import static jp.ac.fit.asura.nao.Effector.Joint.RElbowRoll;
import static jp.ac.fit.asura.nao.Effector.Joint.RElbowYaw;
import static jp.ac.fit.asura.nao.Effector.Joint.RHipPitch;
import static jp.ac.fit.asura.nao.Effector.Joint.RHipRoll;
import static jp.ac.fit.asura.nao.Effector.Joint.RHipYawPitch;
import static jp.ac.fit.asura.nao.Effector.Joint.RKneePitch;
import static jp.ac.fit.asura.nao.Effector.Joint.RShoulderPitch;
import static jp.ac.fit.asura.nao.Effector.Joint.RShoulderRoll;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import jp.ac.fit.asura.nao.Effector;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class MotorCortex {
    private Map<String, List<float[]>> motions;
    private Effector effector;
    private int motionStep;

    private int time;
    private List<float[]> currentMotion;

    /**
     * 
     */
    public MotorCortex(Effector ef) {
	motions = new HashMap<String, List<float[]>>();
	effector = ef;
    }

    public void init() {
	currentMotion = null;
	motionStep = 0;
	time = 0;

	// set default position
	setDefaultPosition();

	/* set initial position of the robot */
	List<float[]> motion = new ArrayList<float[]>();
	try {
	    BufferedReader walk;
	    walk = new BufferedReader(new FileReader("walk.txt"));
	    String line = null;
	    while ((line = walk.readLine()) != null) {
		float[] frame = new float[22];
		StringTokenizer tokens = new StringTokenizer(line, ",");

		for (int j = 0; tokens.hasMoreTokens(); j++) {
		    String str = tokens.nextToken();
		    try {
			frame[j] = Float.parseFloat(str);
			// robot_console_print("read: " + frame[j] +
			// "\n");
		    } catch (NumberFormatException e) {
			System.out
				.println("Error '" + str + "' is not a float");
		    }
		}
		motion.add(frame);
	    }
	    motions.put("walk", motion);
	} catch (IOException e) {
	    System.out.println("Unable to read walk.txt file: " + e + "\n");
	}
    }

    private void setDefaultPosition() {
	effector.setJointDegree(HeadPitch, 0);
	effector.setJointDegree(HeadYaw, 0);
	effector.setJointDegree(LShoulderPitch, 110);
	effector.setJointDegree(LShoulderRoll, 20);
	effector.setJointDegree(LElbowYaw, -80);
	effector.setJointDegree(LElbowRoll, -90);
	effector.setJointDegree(RShoulderPitch, 110);
	effector.setJointDegree(RShoulderRoll, -20);
	effector.setJointDegree(RElbowYaw, 80);
	effector.setJointDegree(RElbowRoll, 90);
	effector.setJointDegree(LHipYawPitch, 0);
	effector.setJointDegree(RHipYawPitch, 0);
	effector.setJointDegree(LHipPitch, -25);
	effector.setJointDegree(LHipRoll, 0);
	effector.setJointDegree(LKneePitch, 40);
	effector.setJointDegree(LAnklePitch, -20);
	effector.setJointDegree(LAnkleRoll, 0);
	effector.setJointDegree(RHipPitch, -25);
	effector.setJointDegree(RHipRoll, 0);
	effector.setJointDegree(RKneePitch, 40);
	effector.setJointDegree(RAnklePitch, -20);
	effector.setJointDegree(RAnkleRoll, 0);
    }

    public void step(int ts) {
	List<float[]> m = currentMotion;
	if (m == null) {
	    setDefaultPosition();
	} else if (motionStep < m.size()) {
	    float[] frame = m.get(motionStep);
	    effector.setJoint(HeadYaw, frame[0]);
	    effector.setJoint(HeadPitch, frame[1]);
	    effector.setJoint(LShoulderPitch, frame[2]);
	    effector.setJoint(LShoulderRoll, frame[3]);
	    effector.setJoint(LElbowYaw, frame[4]);
	    effector.setJoint(LElbowRoll, frame[5]);
	    effector.setJoint(LHipYawPitch, frame[6]);
	    effector.setJoint(LHipPitch, frame[7]);
	    effector.setJoint(LHipRoll, frame[8]);
	    effector.setJoint(LKneePitch, frame[9]);
	    effector.setJoint(LAnklePitch, frame[10]);
	    effector.setJoint(LAnkleRoll, frame[11]);
	    effector.setJoint(RHipYawPitch, frame[12]);
	    effector.setJoint(RHipPitch, frame[13]);
	    effector.setJoint(RHipRoll, frame[14]);
	    effector.setJoint(RKneePitch, frame[15]);
	    effector.setJoint(RAnklePitch, frame[16]);
	    effector.setJoint(RAnkleRoll, frame[17]);
	    effector.setJoint(RShoulderPitch, frame[18]);
	    effector.setJoint(RShoulderRoll, frame[19]);
	    effector.setJoint(RElbowYaw, frame[20]);
	    effector.setJoint(RElbowRoll, frame[21]);
	    motionStep++;

	    effector.setJointDegree(HeadPitch, (float) (Math.sin(time * Math.PI
		    / 8000.0) * 20.0F) + 20.0F);
	    effector.setJointDegree(HeadYaw, (float) (Math.sin(time * Math.PI
		    / 4000.0) * 60.0F));

	} else {
	    currentMotion = null;
	}
	time += ts;
    }

    public void makemotion(String motion, Object param) {
	assert motions.containsKey(motion);
	List<float[]> m = motions.get(motion);
	if (m != currentMotion) {
	    motionStep = 0;
	    currentMotion = m;
	}
    }
}
