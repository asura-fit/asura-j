(define RAW 1)
(define LINER 2)
(define COMPATIBLE 3)

; 100~199
(load "motion-webots.scm")

; 60~69
(load "motion-getup.scm")

; 40~59
(load "motion-shot.scm")

; 70~79
(load "motion-pose.scm")

; 10~29
(load "motion-turn.scm")

; 30~39
(load "motion-walk.scm")

(mc-registmotion 3 "kagami1" COMPATIBLE #(
#(
  #( 0 0 120 20 -80 -90 0 -30 0 35 -20 0 0 -30 0 35 -20 0 120 -20 80 90 )
) #( 10)
)
)


(mc-registmotion 1 "stop2" LINER #(
#(
#(0 0 90 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 90 0 0 0)
) #(20)
)
)

(mc-registmotion 0 "stop" LINER #(
#(
  #(0 0   110 20 -80 -90   0  -25 0 40 -20 0    0  -25 0 40 -20 0    110 -20 80 90)
) #(20)
)
)


(mc-makemotion 0)
