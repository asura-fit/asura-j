(display "Welcome to the ASURA distributed environment.\n")

(load "scheme/asura-js.scm")

(load "scheme/motor-cortex.scm")

(start-httpd 8080)

(show-plane #t)
(saveimage-interval 100)
