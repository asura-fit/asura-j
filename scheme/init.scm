(display "Welcome to the ASURA distributed environment.\n")

(load "asura-js.scm")

(load "motor-cortex.scm")

(start-httpd 8080)

(show-plane #t)
(saveimage-interval 100)
