
; Define system functions  
(define (start-httpd port) (.glueStartHttpd glue port))
(define (show-plane b) (.glueSetShowPlane glue b))
(define (saveimage-interval i) (.glueSetSaveImageInterval glue i))

; Define motor-cortex functions
(define (mc-registmotion id name type a) (.mcRegistmotion glue id name type a))
(define (mc-makemotion id) (.mcMakemotion glue id))

; Define strategy-system functions
(define (ss-scheduler name) (.ssSetScheduler glue name))
