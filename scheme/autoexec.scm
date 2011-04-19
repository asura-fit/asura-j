
(start-httpd 8080)
(start-logd 59000)

;(saveimage-interval 100)

;(loglevel "jp.ac.fit.asura.nao.communication" DEBUG)
;(loglevel "jp.ac.fit.asura.nao.misc.Kinematics" DEBUG)
;(loglevel "jp.ac.fit.asura.nao.motion" DEBUG)
;(loglevel "jp.ac.fit.asura.nao.motion.parameterized" DEBUG)
;(loglevel "jp.ac.fit.asura.nao.motion.MotorCortex" DEBUG)
;(loglevel "jp.ac.fit.asura.nao.vision.perception" INFO)
;(loglevel "jp.ac.fit.asura.nao.vision.perception" TRACE)
;(loglevel "jp.ac.fit.asura.nao.vision" DEBUG)
;(loglevel "jp.ac.fit.asura.nao.sensation" DEBUG)
;(loglevel "jp.ac.fit.asura.nao.strategy" DEBUG)
;(loglevel "jp.ac.fit.asura.nao.localization.self" INFO)
;(loglevel "jp.ac.fit.asura.nao.naoji.motion" DEBUG)
;(loglevel "jp.ac.fit.asura.nao.naoji.NaojiPlayer" TRACE)
;(loglevel "jp.ac.fit.asura.nao.naoji.NaojiDriver" TRACE)
;(loglevel "jp.ac.fit.asura.nao.naoji.NaojiCamera" TRACE)
;(loglevel "jp.ac.fit.asura.nao.naoji.NaojiPlayer" DEBUG)
;(loglevel "jp.ac.fit.asura.nao.naoji.NaojiDriver" DEBUG)
;(loglevel "jp.ac.fit.asura.nao.naoji.NaojiCamera" DEBUG)
;(loglevel "jp.ac.fit.asura.nao.MultiThreadController" DEBUG)
(loglevel "jp.ac.fit.asura.nao.glue.naimon" DEBUG)

;(ss-set-team "Red")
;(ss-set-role "Striker")
;(ss-set-role "Goalie")

(ss-scheduler "ExperimentalScheduler")

(mc-motorpower 1.0f)
(mc-jointpower "RHipPitch" 0.8f)
(mc-jointpower "LHipPitch" 0.8f)
(mc-jointpower "RHipYawPitch" 0.6f)
(mc-jointpower "LHipYawPitch" 0.6f)
(mc-jointpower "RHipRoll" 0.7f)
(mc-jointpower "LHipRoll" 0.7f)
(mc-jointpower "RAnkleRoll" 0.9f)
(mc-jointpower "LAnkleRoll" 0.9f)
(mc-jointpower "RKneePitch" 0.7f)
(mc-jointpower "LKneePitch" 0.7f)
(mc-jointpower "RAnklePitch" 0.5f)
(mc-jointpower "LAnklePitch" 0.5f)

(mc-jointpower "HeadYaw" 0.5f)
(mc-jointpower "HeadPitch" 0.5f)

; 歩く時の関節のStiffnessを設定
(set-walk-jointpower "RHipPitch" 0.8f)
(set-walk-jointpower "LHipPitch" 0.8f)
(set-walk-jointpower "RHipYawPitch" 0.6f)
(set-walk-jointpower "LHipYawPitch" 0.6f)
(set-walk-jointpower "RHipRoll" 0.7f)
(set-walk-jointpower "LHipRoll" 0.7f)
(set-walk-jointpower "RAnkleRoll" 0.9f)
(set-walk-jointpower "LAnkleRoll" 0.9f)
(set-walk-jointpower "RKneePitch" 0.7f)
(set-walk-jointpower "LKneePitch" 0.7f)
(set-walk-jointpower "RAnklePitch" 0.5f)
(set-walk-jointpower "LAnklePitch" 0.5f)

; Walkのパラメータ samplesの設定
(set-walk-samples 20)

(vc-select-camera TOP)
(vc-load-tmap "colorTable/museum2011/normal.tm2")
(load "colorTable/museum2011/cameraConf.scm")
(core-set-visual-cycle 100)

; 試合の時にはロボットごとに異なるteam-configを入れて、このload文を有効にする
;(load "team-config.scm")

; いろいろ
; (set-param USE_HOUGH #t)
; (set-param BALL_DIST_CALIBa 17000.0f)
; (set-param BALL_COMPARE 0.5f)

;(mc-motorpower 0.0f)
