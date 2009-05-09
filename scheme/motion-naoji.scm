(import "jp.ac.fit.asura.nao.naoji.motion.*")
(define naojiwalker (NaojiWalker. jalmotion 25))
(.setWalkConfig jalmotion 0.05f 0.01f 0.001f 0.3f 0.015f 0.015f)
(.setWalkExtraConfig jalmotion 4.0f -4.0f 0.22f 3.0f)
; 腕を使用するか?
(.setWalkArmsEnable jalmotion #f)
(mc-registmotion2 80 naojiwalker)
