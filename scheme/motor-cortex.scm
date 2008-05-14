(define RAW 1)
(define LINER 2)

(define (mc-registmotion id name type a) (.mcRegistmotion glue id name type a))

(mc-registmotion 1 "okiagari1" LINER #(
#(
  #(0 0 -100   0 -90 -90   0  -60  0 90    0   0   0   10  0  90  90  0   0 0 90 0)
  #(0 0  -90   0   0   0   0    0  0  0    0   0   0    0  0   0   0  0   0 0  0 0)
  #(0 0  -90   0   0   0   0   90  0  0    0   0   0  -75  0  90   0  0 -90 0  0 0)
  #(0 0  -90   0   0   0   0    0  0  0    0   0   0    0  0   0   0  0   0 0  0 0)
  #(0 0    0   0   0   0 -75 -110  0 100 -60 -34 -75 -110  0 100 -60 34   0 0  0 0)
  #(0 0    0   0   0   0 -75 -110  0 100 -20 -34 -75 -110  0 100 -20 34   0 0  0 0)
  #(0 0    0   0   0   0 -60 -110  0 100 -30 -34 -60 -110  0 100 -30 34   0 0  0 0)
  #(0 0    0   0   0   0   0    0  0   0   0   0   0    0  0   0   0  0   0 0  0 0)
  #(0 0    0   0   0   0   0    0  0   0   0   0   0    0  0   0   0  0   0 0  0 0)
) #(20 20 10 30 45 45 50 60 120)
)
)
