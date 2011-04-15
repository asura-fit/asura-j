
(start-httpd 8080)
;(start-logd 59000)

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
;(ss-set-role "Defender")

;(ss-scheduler "ExperimentalScheduler")

(mc-motorpower 1.0f)

; nao_robocup_webots6/controllers/nao_soccer_player_redに色切りファイル
; http://asura.fit.ac.jp/private/hg/utilities-nao/raw-file/tip/colorTable/robotstadium2009/normal.tm2
; を入れること
(vc-load-tmap "normal.tm2")

; 指定フレームごとに nao_robocup_webots6/controllers/nao_soccer_player_COLOR/snapshot に ppm ファイルを保存します
;(saveimage-interval 100)

; いろいろ
; (set-param USE_HOUGH #t)
; (set-param BALL_BLOB_THRESHOLD 50)
; (set-param GOAL_BLOB_THRESHOLD 50)
; (set-param BALL_DIST_CALIBa 34293.2f)
; (set-param BALL_DIST_CALIBb 2.55062f)
; (set-param BALL_DIST_CALIBc -67.2262f)
