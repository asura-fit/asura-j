
(start-httpd 8080)

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
(loglevel "jp.ac.fit.asura.nao.glue.naimon" DEBUG)

;(ss-set-team "Red")
;(ss-set-role "Striker")
;(ss-set-role "Goalie")

;(ss-scheduler "ExperimentalScheduler")

(mc-jointpower "RHipPitch" 0.7f)
(mc-jointpower "LHipPitch" 0.7f)
(mc-jointpower "RHipYawPitch" 0.6f)
(mc-jointpower "LHipYawPitch" 0.6f)
(mc-jointpower "RHipRoll" 0.7f)
(mc-jointpower "LHipRoll" 0.7f)
(mc-jointpower "RAnkleRoll" 1.0f)
(mc-jointpower "LAnkleRoll" 1.0f)
(mc-jointpower "RKneePitch" 0.3f)
(mc-jointpower "LKneePitch" 0.3f)
(mc-jointpower "RAnklePitch" 1.0f)
(mc-jointpower "LAnklePitch" 1.0f)

(mc-motorpower 0.0f)
;(mc-jointpower "HeadYaw" 0.125f)
;(mc-jointpower "HeadPitch" 0.125f)

(vc-select-camera TOP)
(vc-load-tmap "colorTable/aiboroom2009/normal.tm2")
(load "colorTable/aiboroom2009/cameraConf.scm")
(core-set-visual-cycle 2000)

; いろいろ
; (set-param jp.ac.fit.asura.nao.vision.VisualParam$Boolean.USE_HOUGH$ #t)
