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

(set-walk-jointpower "RHipPitch" 0.8f)
(set-walk-jointpower "LHipPitch" 0.8f)
(set-walk-jointpower "RHipYawPitch" 0.6f)
(set-walk-jointpower "LHipYawPitch" 0.6f)
(set-walk-jointpower "RHipRoll" 0.8f)
(set-walk-jointpower "LHipRoll" 0.8f)
(set-walk-jointpower "RAnkleRoll" 0.8f)
(set-walk-jointpower "LAnkleRoll" 0.8f)
(set-walk-jointpower "RKneePitch" 0.6f)
(set-walk-jointpower "LKneePitch" 0.7f)
(set-walk-jointpower "RAnklePitch" 0.8f)
(set-walk-jointpower "LAnklePitch" 0.8f)



;修練道場naojiターン54/64
(.setWalkConfig jalmotion 0.015f 0.01f 0.025f 0.4f 0.23f 3.0f)
(.setWalkTrapezoidConfig jalmotion 4.0f -4.0f)
		;修練道場赤い子
		;samples = 33;
		;angle = 0.245f;
		;sideDist = 0.04f;

; Walkのパラメータ samplesの設定
(set-walk-samples 20)