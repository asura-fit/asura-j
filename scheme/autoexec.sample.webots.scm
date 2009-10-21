
(start-httpd 8080)

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

;(ss-set-team "Red")
;(ss-set-role "Striker")
;(ss-set-role "Goalie")

;(ss-scheduler "ExperimentalScheduler")

(mc-motorpower 1.0f)

; nao_robocup_webots6/controllers/nao_soccer_player_redに色切りファイル
; http://asura.fit.ac.jp/private/hg/utilities-nao/raw-file/tip/colorTable/robotstadium2009/normal.tm2
; を入れること
(vc-load-tmap "normal.tm2")


; いろいろ
; (set-param jp.ac.fit.asura.nao.vision.VisualParam$Boolean.USE_HOUGH$ #t)
