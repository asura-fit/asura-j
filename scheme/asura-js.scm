
; Define system functions

; Logger
(define OFF "OFF")
(define FATAL "FATAL")
(define ERROR "ERROR")
(define WARN "WARN")
(define INFO "INFO")
(define DEBUG "DEBUG")
(define TRACE "TRACE")

(define (loglevel name level) (.glueSetLogLevel glue name level))

; core
(define core (.getCore robot-context))
(define (core-set-visual-cycle cycle) (.setTargetVisualCycleTime core cycle))

; Service control
(define (start-httpd port) (.glueStartHttpd glue port))
(define (stop-httpd port) (.glueStopHttpd glue))
(define (start-logd port) (.glueStartLogd glue port))
(define (stop-logd port) (.glueStopLogd glue))

; Utilities
(define (saveimage-interval interval) (.glueSetSaveImageInterval glue interval))
(define (set-param key value) (.glueSetParam glue key value))
(define (get-value key) (.getValue glue key))
(define (set-value key value) (.setValue glue key value))

; Define motor-cortex functions
(define (mc-registmotion id name type a) (.mcRegistmotion glue id name type a))
(define (mc-registmotion2 id obj) (.mcRegistmotion glue id obj))
(define (mc-makemotion id) (.mcMakemotion glue id))
(define (mc-motorpower power) (.mcMotorPower glue power))
(define (mc-jointpower joint power) (.mcJointPower glue joint power))

; Define sensory-cortex functions
(define (sc-create-frame id . parameters) (.scCreateFrame glue id parameters) )
(define (sc-create-robot . frames) (.scCreateRobot glue frames) )
(define (sc-set-robot robot) (.scSetRobot glue robot) )

; Define strategy-system functions
(define STRIKER "Striker")
(define GOALIE "Goalie")
(define REDTEAM "Red")
(define BLUETEAM "Blue")

(define (ss-scheduler name) (.ssSetScheduler glue name))
(define (ss-set-role id) (.ssSetRole glue id))
(define (ss-set-team id) (.ssSetTeam glue id))
(define (ss-set-id id) (.setId core id))
(define (ss-set-team-id id) (.setTeamId core id))
(define (ss-push-queue task) (.ssPushQueue glue task))
(define (ss-abort-task) (.ssAbortTask glue))

; Define visual-cortex functions
(define (vc-load-tmap file) (.vcLoadTMap glue file))
(define (vc-get-param id) (.vcGetParam glue id))
(define (vc-set-param id value) (.vcSetParam glue id value))
(define (vc-get-param2 camera id) (.vcGetParam camera glue id))
(define (vc-set-param2 camera id value) (.vcSetParam2 glue camera id value))
(define (vc-select-camera camera) (.vcSelectCamera glue camera))

; VC prameters
(define AWB 0)
(define AGC 1)
(define AEC 2)
(define Brightness 3)
(define Exposure 4)
(define Gain 5)
(define Contrast 6)
(define Saturation 7)
(define Hue 8)
(define RedChroma 9)
(define BlueChroma 10)

(define TOP "TOP")
(define BOTTOM "BOTTOM")

(define USE_HOUGH jp.ac.fit.asura.nao.vision.VisualParam$Boolean.USE_HOUGH$)
(define BALL_BLOB_THRESHOLD jp.ac.fit.asura.nao.vision.VisualParam$Int.BALL_BLOB_THRESHOLD$)
(define GOAL_BLOB_THRESHOLD jp.ac.fit.asura.nao.vision.VisualParam$Int.GOAL_BLOB_THRESHOLD$)
(define BALL_DIST_CALIBa jp.ac.fit.asura.nao.vision.VisualParam$Float.BALL_DIST_CALIBa$)
(define BALL_DIST_CALIBb jp.ac.fit.asura.nao.vision.VisualParam$Float.BALL_DIST_CALIBb$)
(define BALL_DIST_CALIBc jp.ac.fit.asura.nao.vision.VisualParam$Float.BALL_DIST_CALIBc$)
