;
; HeadYawだけaxisを追加する必要があるので注意

(let
    (

     (Body (sc-create-frame Body '(
                                   (translation . (0 0 0)) (axis . (0 1 0))
                                   (max . 0) (min . 0) (mass . 1.2171)
                                   )))

; begin auto generated parameters
(HeadYaw (sc-create-frame HeadYaw '(
 (translation . (0 160 -20))
 (axis . (0 1 0))
 (max . 1.0472) (min . -1.0472) (mass . 0.050))))

(HeadPitch (sc-create-frame HeadPitch '(
 (translation . (0 60 0))
 (axis . (1 0 0))
 (max . 0.7854) (min . -0.7854) (mass . 0.351))))

(Camera (sc-create-frame Camera '(
 (translation . (0 30 58))
 (axis . (0 1 0))
 (angle . 3.1416))))

(RShoulderPitch (sc-create-frame RShoulderPitch '(
 (translation . (-85 145 -20))
 (axis . (1 0 0))
 (max . 2.0944) (min . -2.0944) (mass . 0.053))))

(RShoulderRoll (sc-create-frame RShoulderRoll '(
 (axis . (0 1 0))
 (max . 0.2618) (min . -1.658) (mass . 0.11))))

(RElbowYaw (sc-create-frame RElbowYaw '(
 (translation . (-6 9 90))
 (axis . (0 0 1))
 (max . 2.095) (min . -2.095) (mass . 0.030))))

(RElbowRoll (sc-create-frame RElbowRoll '(
 (axis . (0 1 0))
 (max . 1.5709) (min . 0) (mass . 0.057))))

(LShoulderPitch (sc-create-frame LShoulderPitch '(
 (translation . (85 145 -20))
 (axis . (1 0 0))
 (max . 2.0944) (min . -2.0944) (mass . 0.053))))

(LShoulderRoll (sc-create-frame LShoulderRoll '(
 (axis . (0 1 0))
 (max . 1.658) (min . -0.2618) (mass . 0.11))))

(LElbowYaw (sc-create-frame LElbowYaw '(
 (translation . (-6 9 90))
 (axis . (0 0 1))
 (max . 2.095) (min . -2.095) (mass . 0.030))))

(LElbowRoll (sc-create-frame LElbowRoll '(
 (axis . (0 1 0))
 (max . 0) (min . -1.5709) (mass . 0.057))))

(RHipYawPitch (sc-create-frame RHipYawPitch '(
 (translation . (-55 -45 -30))
 (axis . (0.7071 0.7071 0))
 (max . 0.1745) (min . -1.5708) (mass . 0.100))))

(RHipRoll (sc-create-frame RHipRoll '(
 (axis . (0 0 1))
 (max . 0.3491) (min . -0.7845) (mass . 0.140))))

(RHipPitch (sc-create-frame RHipPitch '(
 (axis . (1 0 0))
 (max . 0.4363) (min . -1.5708) (mass . 0.293))))

(RKneePitch (sc-create-frame RKneePitch '(
 (translation . (0 -120 5))
 (axis . (1 0 0))
 (max . 2.2689) (min . 0) (mass . 0.423))))

(RAnklePitch (sc-create-frame RAnklePitch '(
 (translation . (0 -100 0))
 (axis . (1 0 0))
 (max . 0.5236) (min . -1.3963) (mass . 0.058))))

(RAnkleRoll (sc-create-frame RAnkleRoll '(
 (axis . (0 0 1))
 (max . 0.7854) (min . -0.3491) (mass . 0.100))))

(LHipYawPitch (sc-create-frame LHipYawPitch '(
 (translation . (55 -45 -30))
 (axis . (0.7071 -0.7071 0))
 (max . 0.1745) (min . -1.5708) (mass . 0.100))))

(LHipRoll (sc-create-frame LHipRoll '(
 (axis . (0 0 1))
 (max . 0.7854) (min . -0.3491) (mass . 0.140))))

(LHipPitch (sc-create-frame LHipPitch '(
 (axis . (1 0 0))
 (max . 0.4363) (min . -1.5708) (mass . 0.293))))

(LKneePitch (sc-create-frame LKneePitch '(
 (translation . (0 -120 5))
 (axis . (1 0 0))
 (max . 2.2689) (min . 0) (mass . 0.423))))

(LAnklePitch (sc-create-frame LAnklePitch '(
 (translation . (0 -100 0))
 (axis . (1 0 0))
 (max . 0.5236) (min . -1.3963) (mass . 0.058))))

(LAnkleRoll (sc-create-frame LAnkleRoll '(
 (axis . (0 0 1))
 (max . 0.3491) (min . -0.7854) (mass . 0.100))))
     
     ;
     (RSole (sc-create-frame RSole '(
                                     (translation . (0 -55 0))
                                     (axis . (1 0 0))
                                     )))

     (RSoleFL (sc-create-frame RSoleFL '(
                                         (translation . (23.17 0 69.91))
                                         (axis . (1 0 0))
                                         (angle . 1.57) (mass . 0.01)
                                         )))

     (RSoleFR (sc-create-frame RSoleFR '(
                                         (translation . (-29.98 0 69.93))
                                         (axis . (1 0 0))
                                         (angle . 1.57) (mass . 0.01)
                                         )))

     (RSoleBL (sc-create-frame RSoleBL '(
                                         (translation . (-26.96 0 -30.62))
                                         (axis . (1 0 0))
                                         (angle . 1.57) (mass . 0.01)
                                         )))

     (RSoleBR (sc-create-frame RSoleBR '(
                                         (translation . (19.11 0 -30.02))
                                         (axis . (1 0 0))
                                         (angle . 1.57) (mass . 0.01)
                                         )))

     ;
     (LSole (sc-create-frame LSole '(
                                     (translation . (0 -55 0))
                                     (axis . (1 0 0))
                                     )))

     (LSoleFL (sc-create-frame LSoleFL '(
                                         (translation . (29.98 0 69.93))
                                         (axis . (1 0 0))
                                         (angle . 1.57) (mass . 0.01)
                                         )))

     (LSoleFR (sc-create-frame LSoleFR '(
                                         (translation . (-23.17 0 69.91))
                                         (axis . (1 0 0))
                                         (angle . 1.57) (mass . 0.01)
                                         )))

     (LSoleBL (sc-create-frame LSoleBL '(
                                         (translation . (-19.11 0 -30.02))
                                         (axis . (1 0 0))
                                         (angle . 1.57) (mass . 0.01)
                                         )))

     (LSoleBR (sc-create-frame LSoleBR '(
                                         (translation . (26.96 0 -30.62))
                                         (axis . (1 0 0))
                                         (angle . 1.57) (mass . 0.01)
                                         )))
     )

  (set! robot (sc-create-robot
   (list (list (list (list (list
                            Body HeadYaw HeadPitch Camera)
                           RShoulderPitch RShoulderRoll RElbowYaw RElbowRoll)
                     LShoulderPitch LShoulderRoll LElbowYaw LElbowRoll)
               RHipYawPitch RHipRoll RHipPitch RKneePitch RAnklePitch RAnkleRoll
               (list (list (list (list RSole RSoleFL) RSoleFR) RSoleBL) RSoleBR))
         LHipYawPitch LHipRoll LHipPitch LKneePitch LAnklePitch LAnkleRoll
         (list (list (list (list LSole LSoleFL) LSoleFR) LSoleBL) LSoleBR))
   )
  )
  )
