(import "jp.ac.fit.asura.nao.naoji.motion.*")
(define naojiwalker (NaojiWalker. jalmotion 35))
; setWalkConfigの値が使えないと標準出力にエラーがでる
; 去年のやつをちょっとかえたもの
(.setWalkConfig jalmotion 0.045f 0.01f 0.04f 0.15f 0.22f 3.0f)
; 標準のやつ
;(.setWalkConfig jalmotion 0.035f 0.01f 0.04f 0.20f 0.23f 3.0f)
(.setWalkTrapezoidConfig jalmotion 0.0f 0.0f)
; 腕を使用するか?
(.setWalkArmsEnable jalmotion #f)
(mc-registmotion2 80 naojiwalker)
