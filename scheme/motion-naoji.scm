(import "jp.ac.fit.asura.nao.naoji.motion.*")
(define naojiwalker (NaojiWalker. jalmotion))
(define circleturn (NaojiCircleTurn. jalmotion naojiwalker))
(define (set-walk-jointpower joint value) (.setJointStiffness naojiwalker joint value))
(define (set-walk-samples samples) (.setWalkSamples naojiwalker samples))

(define (set-circleturn-jointpower joint value) (.setJointStiffness circleturn joint value))
(define (set-circleturn-sidedist dist) (.setSideDist circleturn dist))
(define (set-circleturn-angle angle) (.setAngle circleturn angle))
(define (set-circleturn-samples samples) (.setSamples circleturn samples))

;WalkConfigScheduler用コマンド
(define (set-walkConfig-mode mode) (.setWalkConfigMode glue mode))
(define (set-walkConfig-side mode side) (.setWalkConfigMode glue mode side))

; setWalkConfigの値が使えないと標準出力にエラーがでる
; 去年のやつをちょっとかえたもの
;(.setWalkConfig jalmotion 0.055f 0.02f 0.025f 0.25f 0.22f 3.3f)
; 標準のやつ
;(.setWalkConfig jalmotion 0.035f 0.01f 0.04f 0.20f 0.23f 3.0f)
;(.setWalkTrapezoidConfig jalmotion 5.0f -5.0f)
; モノセン赤(2011/03/09)
;(.setWalkConfig jalmotion 0.035f 0.015f 0.025f 0.20f 0.22f 3.3f)
;資料館 63(2011/04/15)
;(.setWalkConfig jalmotion 0.055f 0.01f 0.025f 0.25f 0.22f 3.0f)
;資料館 61(2011/04/15)
;(.setWalkConfig jalmotion 0.055f 0.02f 0.025f 0.25f 0.22f 3.3f)
;資料館 61(2011/04/19)
;(.setWalkConfig jalmotion 0.055f 0.01f 0.025f 0.25f 0.22f 3.3f)
;資料館 62(2011/04/20)
;(.setWalkConfig jalmotion 0.055f 0.01f 0.025f 0.25f 0.22f 3.0f)

; JapanOpen2011 64/54
;(.setWalkConfig jalmotion 0.035f 0.01f 0.025f 0.25f 0.22f 3.3f)
; JapanOpen2011 61/51
;(.setWalkConfig jalmotion 0.055f 0.01f 0.025f 0.25f 0.22f 3.3f)
; JapanOpen2011 62/52
;(.setWalkConfig jalmotion 0.025f 0.013f 0.02f 0.15f 0.215f -0.98f)
;(.setWalkTrapezoidConfig jalmotion 6.0f -6.0f)
; JapanOpen2011 63/53
;(.setWalkConfig jalmotion 0.055f 0.0092f 0.025f 0.25f 0.22f 3.0f)
;修練道場 51/61
;(.setWalkConfig jalmotion 0.055f 0.01f 0.025f 0.25f 0.22f 3.3f)
;修練道場 52/62
;(.setWalkConfig jalmotion 0.03f 0.013f 0.015f 0.23f 0.22f -0.98f)
;(.setWalkTrapezoidConfig jalmotion 6.0f -6.0f)
;修練道場 53/63
;(.setWalkConfig jalmotion 0.055f 0.017f 0.025f 0.23f 0.25f 3.0f)
;修練道場naojiターン54/64
;(.setWalkConfig jalmotion 0.035f 0.015f 0.02f 0.20f 0.22f 3.3f)
;修練道場53/63改
;(.setWalkConfig jalmotion 0.015f 0.01f 0.025f 0.3f 0.214f 6.0f)
;(.setWalkTrapezoidConfig jalmotion 5.0f -1.0f)
;修練道場51/61改
;(.setWalkConfig jalmotion 0.02f 0.01f 0.025f 0.4f 0.14f 5.0f)
;(.setWalkTrapezoidConfig jalmotion 5.0f -5.0f)



; 腕を使用するか?
(.setWalkArmsEnable jalmotion #t)
; 腕を使用する場合はパラメータを設定
(.setWalkArmsConfig jalmotion 1.5f 0.1f 0.1f 0.0f)


(mc-registmotion2 80 naojiwalker)
(mc-registmotion2 81 circleturn)
