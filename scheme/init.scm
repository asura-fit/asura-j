(display "Welcome to the ASURA distributed environment.")

(define (start-httpd port) (.glueStartHttpd glue port))

(load "scheme/motor-cortex.scm")

(start-httpd 8080)

