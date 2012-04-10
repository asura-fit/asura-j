; 192.168.1.52/62

; 多目的ホール2011
;(set-walk-jointpower "RHipPitch" 0.8f)
;(set-walk-jointpower "LHipPitch" 0.8f)
;(set-walk-jointpower "RHipYawPitch" 0.7f)
;(set-walk-jointpower "LHipYawPitch" 0.8f)
;(set-walk-jointpower "RHipRoll" 0.7f)
;(set-walk-jointpower "LHipRoll" 0.7f)
;(set-walk-jointpower "RAnkleRoll" 0.5f)
;(set-walk-jointpower "LAnkleRoll" 0.5f)
;(set-walk-jointpower "RKneePitch" 0.7f)
;(set-walk-jointpower "LKneePitch" 0.7f)
;(mc-jointpower "RAnklePitch" 0.5f)
;(mc-jointpower "LAnklePitch" 0.5f)

; JapanOpen2011
;(set-walk-jointpower "RHipPitch" 0.8f)
;(set-walk-jointpower "LHipPitch" 0.8f)
;(set-walk-jointpower "RHipYawPitch" 0.6f)
;(set-walk-jointpower "LHipYawPitch" 0.6f)
;(set-walk-jointpower "RHipRoll" 0.8f)
;(set-walk-jointpower "LHipRoll" 0.8f)
;(set-walk-jointpower "RAnkleRoll" 0.9f)
;(set-walk-jointpower "LAnkleRoll" 0.9f)
;(set-walk-jointpower "RKneePitch" 0.67f)
;(set-walk-jointpower "LKneePitch" 0.755f)
;(set-walk-jointpower "RAnklePitch" 0.8f)
;(set-walk-jointpower "LAnklePitch" 0.8f)
;  修練道場
(set-walk-jointpower "RHipPitch" 0.8f)
(set-walk-jointpower "LHipPitch" 0.8f)
(set-walk-jointpower "RHipYawPitch" 0.7f)
(set-walk-jointpower "LHipYawPitch" 0.7f)
(set-walk-jointpower "RHipRoll" 0.83f)
(set-walk-jointpower "LHipRoll" 0.83f)
(set-walk-jointpower "RAnkleRoll" 0.9f)
(set-walk-jointpower "LAnkleRoll" 0.9f)
(set-walk-jointpower "RKneePitch" 0.7f)
(set-walk-jointpower "LKneePitch" 0.78f)
(set-walk-jointpower "RAnklePitch" 0.8f)
(set-walk-jointpower "LAnklePitch" 0.86f)

;修練道場 52/62
(.setWalkConfig jalmotion 0.03f 0.013f 0.015f 0.23f 0.22f -0.98f)
(.setWalkTrapezoidConfig jalmotion 6.0f -6.0f)
; Walkのパラメータ samplesの設定
(set-walk-samples 40)