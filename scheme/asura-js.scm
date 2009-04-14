
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

(define (start-httpd port) (.glueStartHttpd glue port))

; show-plane is deprecated.
(define (show-plane b) (.glueSetShowPlane glue b))
(define (show-naimon b) (.glueSetShowNaimon glue b))
(define (saveimage-interval i) (.glueSetSaveImageInterval glue i))

; Naimon
(define VISION 0)
(define FIELD 1)
(define SCHEME 2)
(define MAKEMOTIONHELPER 3)
(define PRESSURE 4)

(define (naimon-frames . frames) (.glueNaimonFrames glue frames))

; Define motor-cortex functions
(define (mc-registmotion id name type a) (.mcRegistmotion glue id name type a))
(define (mc-registmotion2 id obj) (.mcRegistmotion glue id obj))
(define (mc-makemotion id) (.mcMakemotion glue id))
(define (mc-motorpower sw) (.mcMotorPower glue sw))

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

; Define visual-cortex functions
(define (vc-load-tmap file) (.vcLoadTMap glue file))
(define (vc-get-param id) (.vcGetParam glue id))
(define (vc-set-param id value) (.vcSetParam glue id value))

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

