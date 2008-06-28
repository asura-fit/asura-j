
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

; Define motor-cortex functions
(define (mc-registmotion id name type a) (.mcRegistmotion glue id name type a))
(define (mc-makemotion id) (.mcMakemotion glue id))

; Define strategy-system functions
(define (ss-scheduler name) (.ssSetScheduler glue name))
