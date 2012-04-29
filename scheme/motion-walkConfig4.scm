; 192.168.1.54/64

; 歩く時の関節のStiffnessを設定
;(set-walk-jointpower "RHipPitch" 0.8f)
;(set-walk-jointpower "LHipPitch" 0.8f)
;(set-walk-jointpower "RHipYawPitch" 0.6f)
;(set-walk-jointpower "LHipYawPitch" 0.6f)
;(set-walk-jointpower "RHipRoll" 0.7f)
;(set-walk-jointpower "LHipRoll" 0.7f)
;(set-walk-jointpower "RAnkleRoll" 0.9f)
;(set-walk-jointpower "LAnkleRoll" 0.9f)
;(set-walk-jointpower "RKneePitch" 0.7f)
;(set-walk-jointpower "LKneePitch" 0.7f)
;(set-walk-jointpower "RAnklePitch" 0.5f)
;(set-walk-jointpower "LAnklePitch" 0.5f)

;(set-walk-jointpower "RHipPitch" 0.8f)
;(set-walk-jointpower "LHipPitch" 0.8f)
;(set-walk-jointpower "RHipYawPitch" 0.6f)
;(set-walk-jointpower "LHipYawPitch" 0.6f)
;(set-walk-jointpower "RHipRoll" 0.8f)
;(set-walk-jointpower "LHipRoll" 0.8f)
;(set-walk-jointpower "RAnkleRoll" 0.8f)
;(set-walk-jointpower "LAnkleRoll" 0.8f)
;(set-walk-jointpower "RKneePitch" 0.6f)
;(set-walk-jointpower "LKneePitch" 0.7f)
;(set-walk-jointpower "RAnklePitch" 0.8f)
;(set-walk-jointpower "LAnklePitch" 0.8f)

;B4
;(set-walk-jointpower "RHipPitch" 0.7f)
;(set-walk-jointpower "LHipPitch" 0.6f)
;(set-walk-jointpower "RHipYawPitch" 0.7f)
;(set-walk-jointpower "LHipYawPitch" 0.6f)
;(set-walk-jointpower "RHipRoll" 0.6)
;(set-walk-jointpower "LHipRoll" 0.6f)
;(set-walk-jointpower "RAnkleRoll" 0.7f)
;(set-walk-jointpower "LAnkleRoll" 0.7f)
;(set-walk-jointpower "RKneePitch" 0.6f)
;(set-walk-jointpower "LKneePitch" 0.5f)
;(set-walk-jointpower "RAnklePitch" 0.7f)
;(set-walk-jointpower "LAnklePitch" 0.7f)



;(set-walk-config 0.023f 0.02f 0.015f 0.4f 0.236f 3.0f)

;(set-walk-jointpower "RHipPitch" 0.7f)
;(set-walk-jointpower "LHipPitch" 0.6f)
;(set-walk-jointpower "RHipYawPitch" 0.7f)
;(set-walk-jointpower "LHipYawPitch" 0.6f)
;(set-walk-jointpower "RHipRoll" 0.6f)
;(set-walk-jointpower "LHipRoll" 0.6f)
;(set-walk-jointpower "RAnkleRoll" 0.7f)
;(set-walk-jointpower "LAnkleRoll" 0.7f)
;(set-walk-jointpower "RKneePitch" 0.6f)
;(set-walk-jointpower "LKneePitch" 0.5f)
;(set-walk-jointpower "RAnklePitch" 0.7f)
;(set-walk-jointpower "LAnklePitch" 0.7f)


(set-walk-config 0.02f 0.017f 0.015f 0.4f 0.236f 3.0f)
(set-walk-trapezoidConfig 3.5f -3.5f)


(set-walk-jointpower "RHipPitch" 0.7f)
(set-walk-jointpower "LHipPitch" 0.7f)
(set-walk-jointpower "RHipYawPitch" 0.6f)
(set-walk-jointpower "LHipYawPitch" 0.6f)
(set-walk-jointpower "RHipRoll" 0.7f)
(set-walk-jointpower "LHipRoll" 0.8f)
(set-walk-jointpower "RAnkleRoll" 0.7f)
(set-walk-jointpower "LAnkleRoll" 0.7f)
(set-walk-jointpower "RKneePitch" 0.7f)
(set-walk-jointpower "LKneePitch" 0.7f)
(set-walk-jointpower "RAnklePitch" 0.8f)
(set-walk-jointpower "LAnklePitch" 0.8f)






;B4
;(.setWalkConfig jalmotion 0.03f 0.018f 0.015f 0.4f 0.224f 3.3f)
;(.setWalkTrapezoidConfig jalmotion 6.0f -3.0f)

;修練道場naojiターン54/64
;(.setWalkConfig jalmotion 0.015f 0.01f 0.025f 0.4f 0.23f 3.0f)
;(.setWalkTrapezoidConfig jalmotion 4.0f -4.0f)
		;修練道場赤い子
		;samples = 33;
		;angle = 0.245f;
		;sideDist = 0.04f;

; Walkのパラメータ samplesの設定
(set-walk-samples 25)


;赤たん
;(.setWalkConfig jalmotion 0.035f 0.018f 0.015f 0.4f 0.224f 3.3f)

;(.setWalkConfig jalmotion 0.035f 0.017f 0.02f 0.4f 0.224f 3.3f)


;(.setWalkTrapezoidConfig jalmotion 9.0f -9.0f)
(set-circleturn-angle 0.25f)
(set-circleturn-sidedist 0.045f)

(.setSideSamples circleturn 30)
(.setTurnSamples circleturn 30)

(set-circleturn-pose #(85 13 -7 -79 	0 0 -31 54 -27 0 	0 0 -30 54 -27 0	 87 -14 7 78))

;(set-circleturn-jointpower "RHipPitch" 0.7f)
;(set-circleturn-jointpower "LHipPitch" 0.6f)
;(set-circleturn-jointpower "RHipYawPitch" 0.7f)
;(set-circleturn-jointpower "LHipYawPitch" 0.6f)
;(set-circleturn-jointpower "RHipRoll" 0.7f)
;(set-circleturn-jointpower "LHipRoll" 0.7f)
;(set-circleturn-jointpower "RAnkleRoll" 0.6f)
;(set-circleturn-jointpower "LAnkleRoll" 0.6f)
;(set-circleturn-jointpower "RKneePitch" 0.7f)
;(set-circleturn-jointpower "LKneePitch" 0.7f)
;(set-circleturn-jointpower "RAnklePitch" 0.7f)
;(set-circleturn-jointpower "LAnklePitch" 0.7f)


;赤たん
;(set-circleturn-jointpower "RHipPitch" 0.7f)
;(set-circleturn-jointpower "LHipPitch" 0.6f)
;(set-circleturn-jointpower "RHipYawPitch" 0.7f)
;(set-circleturn-jointpower "LHipYawPitch" 0.6f)
;(set-circleturn-jointpower "RHipRoll" 0.6f)
;(set-circleturn-jointpower "LHipRoll" 0.6f)
;(set-circleturn-jointpower "RAnkleRoll" 0.7f)
;(set-circleturn-jointpower "LAnkleRoll" 0.7f)
;(set-circleturn-jointpower "RKneePitch" 0.6f)
;(set-circleturn-jointpower "LKneePitch" 0.5f)
;(set-circleturn-jointpower "RAnklePitch" 0.7f)
;(set-circleturn-jointpower "LAnklePitch" 0.7f)



;(set-turn-config 0.023f 0.02f 0.02f 0.4f 0.236f 3.0f)

;(set-circleturn-jointpower "RHipPitch" 0.6f)
;(set-circleturn-jointpower "LHipPitch" 0.6f)
;(set-circleturn-jointpower "RHipYawPitch" 0.6f)
;(set-circleturn-jointpower "LHipYawPitch" 0.6f)
;(set-circleturn-jointpower "RHipRoll" 0.6f)
;(set-circleturn-jointpower "LHipRoll" 0.6f)
;(set-circleturn-jointpower "RAnkleRoll" 0.6f)
;(set-circleturn-jointpower "LAnkleRoll" 0.6f)
;(set-circleturn-jointpower "RKneePitch" 0.6f)
;(set-circleturn-jointpower "LKneePitch" 0.6f)
;(set-circleturn-jointpower "RAnklePitch" 0.6f)
;(set-circleturn-jointpower "LAnklePitch" 0.6f)

;(set-turn-config 0.023f 0.02f 0.02f 0.4f 0.236f 3.0f)

(set-circleturn-jointpower "RHipPitch" 0.6f)
(set-circleturn-jointpower "LHipPitch" 0.6f)
(set-circleturn-jointpower "RHipYawPitch" 0.6f)
(set-circleturn-jointpower "LHipYawPitch" 0.6f)
(set-circleturn-jointpower "RHipRoll" 0.6f)
(set-circleturn-jointpower "LHipRoll" 0.6f)
(set-circleturn-jointpower "RAnkleRoll" 0.6f)
(set-circleturn-jointpower "LAnkleRoll" 0.6f)
(set-circleturn-jointpower "RKneePitch" 0.6f)
(set-circleturn-jointpower "LKneePitch" 0.5f)
(set-circleturn-jointpower "RAnklePitch" 0.6f)
(set-circleturn-jointpower "LAnklePitch" 0.6f)