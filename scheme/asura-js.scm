
; Define system functions  
(define (start-httpd port) (.glueStartHttpd glue port))

; Define motor-cortex functions
(define (mc-registmotion id name type a) (.mcRegistmotion glue id name type a))
(define (mc-makemotion id) (.mcMakemotion glue id))
