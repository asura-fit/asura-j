; NaoV3Rのロボット定義
; Nao doc 1.2.0 - Red book - Hardwareより.
; じつは utilities/proto2scm.pl による自動作成.
; ただしHeadYawに(axis 0 1 0)を追加する必要がある.

(let
  (

; begin auto generated parameters

(Body (sc-create-frame Body '(
 (mass . 1.2171) (centerOfMass . (0 0.046466 0.007829)))))

(HeadYaw (sc-create-frame HeadYaw '(
 (translation . (0 126.5 0))
 (axis . (0 1 0))
 (max . 2.0944) (min . -2.0944)
 (mass . 0.01))))

(HeadPitch (sc-create-frame HeadPitch '(
 (axis . (1 0 0))
 (max . 0.7854) (min . -0.7854)
 (mass . 0.391) (centerOfMass . (0 0.044399 -0.000247)))))

(CameraSelect (sc-create-frame CameraSelect '(
 (translation . (0 67.9 0))
 (axis . (1 0 0))
 (max . 0.6981))))

(NaoCam (sc-create-frame NaoCam '(
 (translation . (0 0 53.9))
 (axis . (0 1 0))
 (angle . 3.1416))))

(RShoulderPitch (sc-create-frame RShoulderPitch '(
 (translation . (-98 100 0))
 (axis . (1 0 0))
 (max . 2.0944) (min . -2.0944)
 (mass . 0.01))))

(RShoulderRoll (sc-create-frame RShoulderRoll '(
 (min . -1.658)
 (mass . 0.153) (centerOfMass . (-0.002 0 0.0595)))))

(RElbowYaw (sc-create-frame RElbowYaw '(
 (translation . (0 0 90))
 (axis . (0 0 1))
 (max . 2.0944) (min . -2.0944)
 (mass . 0.01))))

(RElbowRoll (sc-create-frame RElbowRoll '(
 (max . 1.5708) (mass . 0.077) (centerOfMass . (-0.002 0 0.07615)))))

(LShoulderPitch (sc-create-frame LShoulderPitch '(
 (translation . (98 100 0))
 (axis . (1 0 0))
 (max . 2.0944) (min . -2.0944)
 (mass . 0.01))))

(LShoulderRoll (sc-create-frame LShoulderRoll '(
 (max . 1.658) (mass . 0.153) (centerOfMass . (0.002 0 0.0595)))))

(LElbowYaw (sc-create-frame LElbowYaw '(
 (translation . (0 0 90))
 (axis . (0 0 1))
 (max . 2.0944) (min . -2.0944)
 (mass . 0.01))))

(LElbowRoll (sc-create-frame LElbowRoll '(
 (min . -1.5708)
 (mass . 0.077) (centerOfMass . (0.002 0 0.07615)))))

(RHipYawPitch (sc-create-frame RHipYawPitch '(
 (translation . (-50 -85 0))
 (axis . (0.707107 0.707107 0))
 (min . -1.5708)
 (mass . 0.01))))

(RHipRoll (sc-create-frame RHipRoll '(
 (axis . (0 0 1))
 (max . 0.4363) (min . -0.7854)
 (mass . 0.01))))

(RHipPitch (sc-create-frame RHipPitch '(
 (axis . (1 0 0))
 (max . 0.4363) (min . -1.7453)
 (mass . 0.513) (centerOfMass . (0 -0.038 -0.005579)))))

(RKneePitch (sc-create-frame RKneePitch '(
 (translation . (0 -100 0))
 (axis . (1 0 0))
 (max . 2.2689) (mass . 0.423) (centerOfMass . (0 -0.0691 -0.000511)))))

(RAnklePitch (sc-create-frame RAnklePitch '(
 (translation . (0 -100 0))
 (axis . (1 0 0))
 (max . 0.7854) (min . -1.309)
 (mass . 0.01))))

(RAnkleRoll (sc-create-frame RAnkleRoll '(
 (axis . (0 0 1))
 (max . 0.7854) (min . -0.4363)
 (mass . 0.128) (centerOfMass . (0 -0.03 0.018015)))))

(LHipYawPitch (sc-create-frame LHipYawPitch '(
 (translation . (50 -85 0))
 (axis . (0.707107 -0.707107 0))
 (min . -1.5708)
 (mass . 0.01))))

(LHipRoll (sc-create-frame LHipRoll '(
 (axis . (0 0 1))
 (max . 0.7854) (min . -0.4363)
 (mass . 0.01))))

(LHipPitch (sc-create-frame LHipPitch '(
 (axis . (1 0 0))
 (max . 0.4363) (min . -1.7453)
 (mass . 0.513) (centerOfMass . (0 -0.038 -0.005579)))))

(LKneePitch (sc-create-frame LKneePitch '(
 (translation . (0 -100 0))
 (axis . (1 0 0))
 (max . 2.2689) (mass . 0.423) (centerOfMass . (0 -0.0691 -0.000511)))))

(LAnklePitch (sc-create-frame LAnklePitch '(
 (translation . (0 -100 0))
 (axis . (1 0 0))
 (max . 0.7854) (min . -1.309)
 (mass . 0.01))))

(LAnkleRoll (sc-create-frame LAnkleRoll '(
 (axis . (0 0 1))
 (max . 0.4363) (min . -0.7854)
 (mass . 0.128) (centerOfMass . (0 -0.03 0.018015)))))

(RFsrFL (sc-create-frame RFsrFL '(
 (translation . (23 -45 70.1))
 (axis . (1 0 0))
 (mass . 0.005) (angle . 1.57))))

(RFsrFR (sc-create-frame RFsrFR '(
 (translation . (-30 -45 70.1))
 (axis . (1 0 0))
 (angle . 1.57))))

(RFsrBR (sc-create-frame RFsrBR '(
 (translation . (-30 -45 -30.4))
 (axis . (1 0 0))
 (angle . 1.57))))

(RFsrBL (sc-create-frame RFsrBL '(
 (translation . (19 -45 -29.8))
 (axis . (1 0 0))
 (angle . 1.57))))

(LFsrFL (sc-create-frame LFsrFL '(
 (translation . (30 -45 70.1))
 (axis . (1 0 0))
 (angle . 1.57))))

(LFsrFR (sc-create-frame LFsrFR '(
 (translation . (-23 -45 70.1))
 (axis . (1 0 0))
 (angle . 1.57))))

(LFsrBR (sc-create-frame LFsrBR '(
 (translation . (-19 -45 -29.8))
 (axis . (1 0 0))
 (angle . 1.57))))

(LFsrBL (sc-create-frame LFsrBL '(
 (translation . (30 -45 -30.4))
 (axis . (1 0 0))
 (angle . 1.57))))

     ;
     (RSole (sc-create-frame RSole '(
                                     (translation . (0 -46 0))
                                     (axis . (1 0 0))
                                     )))

     ;
     (LSole (sc-create-frame LSole '(
                                     (translation . (0 -46 0))
                                     (axis . (1 0 0))
                                     )))
  )

  (sc-set-robot (sc-create-robot
   (list (list (list (list (list
                            Body HeadYaw HeadPitch CameraSelect NaoCam)
                           RShoulderPitch RShoulderRoll RElbowYaw RElbowRoll)
                     LShoulderPitch LShoulderRoll LElbowYaw LElbowRoll)
               RHipYawPitch RHipRoll RHipPitch RKneePitch RAnklePitch
               (list (list (list (list (list RAnkleRoll RSole) RFsrFL) RFsrFR) RFsrBL) RFsrBR))
         LHipYawPitch LHipRoll LHipPitch LKneePitch LAnklePitch
         (list (list (list (list (list LAnkleRoll LSole) LFsrFL) LFsrFR) LFsrBL) LFsrBR))
   )
  )
)
