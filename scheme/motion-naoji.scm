(import "jp.ac.fit.asura.nao.naoji.motion.*")
(define naojiwalker (NaojiWalker. jalmotion 27))
;(.setWalkConfig jalmotion 0.05f 0.01f 0.01f 0.3f 0.015f 0.015f)
(.setWalkConfig jalmotion 0.06f 0.01f 0.03f 0.4f 0.015f 0.015f)
(.setWalkExtraConfig jalmotion 4.0f -4.0f 0.22f 3.0f)
; 腕を使用するか?
(.setWalkArmsEnable jalmotion #f)
(mc-registmotion2 80 naojiwalker)
