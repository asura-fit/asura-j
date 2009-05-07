(import "jp.ac.fit.asura.nao.naoji.motion.*")
(define naojiwalker (NaojiWalker. jalmotion 30))
(.setWalkConfig jalmotion 0.05f 0.008f 0.06f 0.3f 0.015f 0.018f)
(.setWalkExtraConfig jalmotion 3.5f -3.5f 0.215f 3.0f)
; 腕を使用するか?
(.setWalkArmsEnable jalmotion #f)
(mc-registmotion2 80 naojiwalker)