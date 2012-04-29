
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
;(ss-set-role "Defender")

;(ss-scheduler "ExperimentalScheduler")

(mc-motorpower 1.0f)
;(mc-jointpower "RHipPitch" 0.8f)
;(mc-jointpower "LHipPitch" 0.8f)
;(mc-jointpower "RHipYawPitch" 0.6f)
;(mc-jointpower "LHipYawPitch" 0.6f)
;(mc-jointpower "RHipRoll" 0.7f)
;(mc-jointpower "LHipRoll" 0.7f)
;(mc-jointpower "RAnkleRoll" 0.9f)
;(mc-jointpower "LAnkleRoll" 0.9f)
;(mc-jointpower "RKneePitch" 0.7f)
;(mc-jointpower "LKneePitch" 0.7f)
;(mc-jointpower "RAnklePitch" 0.5f)
;(mc-jointpower "LAnklePitch" 0.5f)

(mc-jointpower "HeadYaw" 0.5f)
(mc-jointpower "HeadPitch" 0.5f)


(mc-makemotion 739)


; 歩く時の関節のStiffness、Walkのパラメータ samplesの読み込み
;(load "motion-walkConfig1.scm")
;(load "motion-walkConfig2.scm")
;(load "motion-walkCOnfig3.scm")
;(load "motion-walkCOnfig4.scm")

(vc-select-camera TOP)
(vc-load-tmap "colorTable/B4/normal.tm2")
(load "colorTable/B4/cameraConf.scm")
(core-set-visual-cycle 100)

; 試合の時にはロボットごとに異なるteam-configを入れて、このload文を有効にする
;(load "team-config.scm")

; いろいろ
; (set-param USE_HOUGH #t)
; (set-param BALL_DIST_CALIBa 17000.0f)
; (set-param BALL_COMPARE 0.5f)

; (set-param GOAL_DIST_CALIB_POLEa 35111.1f)
; (set-param GOAL_DIST_CALIB_POLEb 3.82416f)
; (set-param GOAL_DIST_CALIB_POLEc 113.482f)
; (set-param GOAL_DIST_CALIB_HEIGHTa 173890)
; (set-param GOAL_DIST_CALIB_HEIGHTb 5.01436f)
; (set-param GOAL_DIST_CALIB_HEIGHTc 80.5422f)
; (set-param GOAL_DIST_CALIB_WIDTHa 312111.1f)
; (set-param GOAL_DIST_CALIB_WIDTHb 5.01436f)
; (set-param GOAL_DIST_CALIB_WIDTHc 80.482f)
; ゴール用の↑のパラメータはwebots用なので、aのみ二倍にして実機用に。
(set-param GOAL_DIST_CALIB_POLEa (* 2 35111.1f))
(set-param GOAL_DIST_CALIB_HEIGHTa (* 2 173890f))
(set-param GOAL_DIST_CALIB_WIDTHa (* 2 312111.1f))

;(mc-motorpower 0.0f)
