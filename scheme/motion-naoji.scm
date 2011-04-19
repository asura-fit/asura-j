(import "jp.ac.fit.asura.nao.naoji.motion.*")
(define naojiwalker (NaojiWalker. jalmotion))
(define circleturn (NaojiCircleTurn. jalmotion naojiwalker))
(define (set-walk-jointpower joint value) (.setJointStiffness naojiwalker joint value))
(define (set-walk-samples samples) (.setWalkSamples naojiwalker samples))


; setWalkConfigの値が使えないと標準出力にエラーがでる
; 去年のやつをちょっとかえたもの
;(.setWalkConfig jalmotion 0.055f 0.02f 0.025f 0.25f 0.22f 3.3f)
; 標準のやつ
;(.setWalkConfig jalmotion 0.035f 0.01f 0.04f 0.20f 0.23f 3.0f)
(.setWalkTrapezoidConfig jalmotion 5.0f -5.0f)
; モノセン赤(2011/03/09)
(.setWalkConfig jalmotion 0.035f 0.015f 0.025f 0.20f 0.22f 3.3f)
;資料館 63(2011/04/15)
;(.setWalkConfig jalmotion 0.055f 0.01f 0.025f 0.25f 0.22f 3.0f)
;資料館 61(2011/04/15)
;(.setWalkConfig jalmotion 0.055f 0.02f 0.025f 0.25f 0.22f 3.3f)
;資料館 61(2011/04/19)
;(.setWalkConfig jalmotion 0.055f 0.01f 0.025f 0.25f 0.22f 3.3f)

; 腕を使用するか?
(.setWalkArmsEnable jalmotion #t)
; 腕を使用する場合はパラメータを設定
(.setWalkArmsConfig jalmotion 1.5f 0.1f 0.1f 0.0f)

(mc-registmotion2 80 naojiwalker)
(mc-registmotion2 81 circleturn)
