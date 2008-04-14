/*
 * 作成日: 2008/04/12
 */
package jp.ac.fit.asura.nao;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public interface Effector {
    public enum Joint {
	HeadYaw, HeadPitch, RShoulderPitch, RShoulderRoll, RElbowYaw, RElbowRoll, LShoulderPitch, LShoulderRoll, LElbowYaw, LElbowRoll, RHipYawPitch, RHipPitch, RHipRoll, RKneePitch, RAnklePitch, RAnkleRoll, LHipYawPitch, LHipPitch, LHipRoll, LKneePitch, LAnklePitch, LAnkleRoll
    };

    public float getJoint(Joint joint);

    public void setJoint(Joint joint, float valueInRad);

    public void setJointMicro(Joint joint, int valueInMicroRad);

    public void setJointDegree(Joint joint, float valueInDeg);
}
