
(import "jp.ac.asura.nao.naoji.motion*")

(define naojiwalker (NaojiWalker. jalmotion))
; (.setWalkConfig jalmotion 1 1 1 1 1 1)
; (.setWalkExtraConfig jalmotion 1 1 1 1 1 1)
(mc-registmotion 200 naojiwalker)
