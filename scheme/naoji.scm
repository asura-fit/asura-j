
(import "jp.ac.fit.asura.nao.naoji.motion.*")

(define naojiwalker (NaojiWalker. jalmotion 40))
; (.setWalkConfig jalmotion 1 1 1 1 1 1)
; (.setWalkExtraConfig jalmotion 1 1 1 1 1 1)
(mc-registmotion2 200 naojiwalker)
