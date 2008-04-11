/*
 * 作成日: 2008/04/10
 */
package jp.ac.fit.asura.nao;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JLabel;

import com.cyberbotics.webots.Controller;

/**
 * @author $Author: sey $
 * 
 * @version $Id: $
 * 
 */
public class WebotsPlayer extends Controller {

    public static final int SIMULATION_STEP = 40;

    public static int position, color;

    static int head_yaw, head_pitch, l_shoulder_pitch, l_shoulder_roll,
            l_elbow_yaw, l_elbow_roll, r_shoulder_pitch, r_shoulder_roll,
            r_elbow_yaw, r_elbow_roll, r_hip_yaw_pitch, r_hip_pitch,
            r_hip_roll, r_knee_pitch, r_ankle_pitch, r_ankle_roll,
            l_hip_yaw_pitch, l_hip_pitch, l_hip_roll, l_knee_pitch,
            l_ankle_pitch, l_ankle_roll, camera, left_ultrasound_sensor,
            right_ultrasound_sensor, accelerometer, right_fsr[], left_fsr[],
            emitter, receiver, logo_led;

    static BufferedImage bufferedImage; // image (for GUI)

    static JLabel imageLabel; // swing widget for the image (GUI)

    static JLabel batteryLabel;

    static Map<String, List<float[]>> motions = new HashMap<String, List<float[]>>();

    public static void servo_set_position_deg(int servo, float position) {
        servo_set_position(servo, position * (float) Math.PI / 180);
    }

    public static void die() {
        robot_console_print("die method called\n");
    }

    public static void reset() {
        String name = robot_get_name();
        right_fsr = new int[4];
        left_fsr = new int[4];
        int i;
        if (name.equals("red goal keeper")) {
            position = 0;
            color = 0;
        } else if (name.equals("red player 1")) {
            position = 1;
            color = 0;
        } else if (name.equals("red player 2")) {
            position = 2;
            color = 0;
        } else if (name.equals("red player 3")) {
            position = 3;
            color = 0;
        } else if (name.equals("blue goal keeper")) {
            position = 0;
            color = 1;
        } else if (name.equals("blue player 1")) {
            position = 1;
            color = 1;
        } else if (name.equals("blue player 2")) {
            position = 2;
            color = 1;
        } else if (name.equals("blue player 3")) {
            position = 3;
            color = 1;
        } else
            robot_console_print("unable to recognize player position: " + name
                    + "\n");
        head_yaw = robot_get_device("HeadYaw");
        head_pitch = robot_get_device("HeadPitch");
        r_shoulder_pitch = robot_get_device("RShoulderPitch");
        r_shoulder_roll = robot_get_device("RShoulderRoll");
        r_elbow_yaw = robot_get_device("RElbowYaw");
        r_elbow_roll = robot_get_device("RElbowRoll");
        l_shoulder_pitch = robot_get_device("LShoulderPitch");
        l_shoulder_roll = robot_get_device("LShoulderRoll");
        l_elbow_yaw = robot_get_device("LElbowYaw");
        l_elbow_roll = robot_get_device("LElbowRoll");
        r_hip_yaw_pitch = robot_get_device("RHipYawPitch");
        r_hip_pitch = robot_get_device("RHipPitch");
        r_hip_roll = robot_get_device("RHipRoll");
        r_knee_pitch = robot_get_device("RKneePitch");
        r_ankle_pitch = robot_get_device("RAnklePitch");
        r_ankle_roll = robot_get_device("RAnkleRoll");
        l_hip_yaw_pitch = robot_get_device("LHipYawPitch");
        l_hip_pitch = robot_get_device("LHipPitch");
        l_hip_roll = robot_get_device("LHipRoll");
        l_knee_pitch = robot_get_device("LKneePitch");
        l_ankle_pitch = robot_get_device("LAnklePitch");
        l_ankle_roll = robot_get_device("LAnkleRoll");
        camera = robot_get_device("camera");
        camera_enable(camera, 4 * SIMULATION_STEP);
        accelerometer = robot_get_device("accelerometer");
        accelerometer_enable(accelerometer, SIMULATION_STEP);
        left_ultrasound_sensor = robot_get_device("left ultrasound sensor");
        distance_sensor_enable(left_ultrasound_sensor, SIMULATION_STEP);
        right_ultrasound_sensor = robot_get_device("right ultrasound sensor");
        distance_sensor_enable(right_ultrasound_sensor, SIMULATION_STEP);
        right_fsr[0] = robot_get_device("RFsrFL");
        right_fsr[1] = robot_get_device("RFsrFR");
        right_fsr[2] = robot_get_device("RFsrBR");
        right_fsr[3] = robot_get_device("RFsrBL");
        left_fsr[0] = robot_get_device("LFsrFL");
        left_fsr[1] = robot_get_device("LFsrFR");
        left_fsr[2] = robot_get_device("LFsrBR");
        left_fsr[3] = robot_get_device("LFsrBL");
        for (i = 0; i < 4; i++) {
            touch_sensor_enable(right_fsr[i], SIMULATION_STEP);
            touch_sensor_enable(left_fsr[i], SIMULATION_STEP);
        }
        logo_led = robot_get_device("logo led");
        emitter = robot_get_device("emitter");
        receiver = robot_get_device("receiver");
        receiver_enable(receiver, SIMULATION_STEP);

        /* set initial position of the robot */
        servo_set_position_deg(head_pitch, 0);
        servo_set_position_deg(head_yaw, 0);
        servo_set_position_deg(l_shoulder_pitch, 110);
        servo_set_position_deg(l_shoulder_roll, 20);
        servo_set_position_deg(l_elbow_yaw, -80);
        servo_set_position_deg(l_elbow_roll, -90);
        servo_set_position_deg(r_shoulder_pitch, 110);
        servo_set_position_deg(r_shoulder_roll, -20);
        servo_set_position_deg(r_elbow_yaw, 80);
        servo_set_position_deg(r_elbow_roll, 90);
        servo_set_position_deg(l_hip_yaw_pitch, 0);
        servo_set_position_deg(r_hip_yaw_pitch, 0);
        servo_set_position_deg(l_hip_pitch, -25);
        servo_set_position_deg(l_hip_roll, 0);
        servo_set_position_deg(l_knee_pitch, 40);
        servo_set_position_deg(l_ankle_pitch, -20);
        servo_set_position_deg(l_ankle_roll, 0);
        servo_set_position_deg(r_hip_pitch, -25);
        servo_set_position_deg(r_hip_roll, 0);
        servo_set_position_deg(r_knee_pitch, 40);
        servo_set_position_deg(r_ankle_pitch, -20);
        servo_set_position_deg(r_ankle_roll, 0);
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
            robot_console_print("Unable to read walk.txt file: " + e + "\n");
        }
    }

    static int ts = 0;

    static int motionStep = 0;

    public static int run(int step) {
        int image[];

        List<float[]> m = motions.get("walk");
        assert m != null;
        if (ts > 3000) {
            if (motionStep < m.size()) {
                float[] frame = m.get(motionStep);

                servo_set_position(head_yaw, frame[0]);
                servo_set_position(head_pitch, frame[1]);
                servo_set_position(l_shoulder_pitch, frame[2]);
                servo_set_position(l_shoulder_roll, frame[3]);
                servo_set_position(l_elbow_yaw, frame[4]);
                servo_set_position(l_elbow_roll, frame[5]);
                servo_set_position(l_hip_yaw_pitch, frame[6]);
                servo_set_position(l_hip_pitch, frame[7]);
                servo_set_position(l_hip_roll, frame[8]);
                servo_set_position(l_knee_pitch, frame[9]);
                servo_set_position(l_ankle_pitch, frame[10]);
                servo_set_position(l_ankle_roll, frame[11]);
                servo_set_position(r_hip_yaw_pitch, frame[12]);
                servo_set_position(r_hip_pitch, frame[13]);
                servo_set_position(r_hip_roll, frame[14]);
                servo_set_position(r_knee_pitch, frame[15]);
                servo_set_position(r_ankle_pitch, frame[16]);
                servo_set_position(r_ankle_roll, frame[17]);
                servo_set_position(r_shoulder_pitch, frame[18]);
                servo_set_position(r_shoulder_roll, frame[19]);
                servo_set_position(r_elbow_yaw, frame[20]);
                servo_set_position(r_elbow_roll, frame[21]);
                motionStep++;
            } else {
                motionStep = 0;
            }

        }

        // read sensor information
        image = camera_get_image(camera);

        ts += SIMULATION_STEP;
        return SIMULATION_STEP;
    }

    public static void main(String args[]) {
        robot_console_print("main method called\n");
        int step = 0;
        while (true) {
            step++;
            robot_step(run(step));
        }
    }
}
