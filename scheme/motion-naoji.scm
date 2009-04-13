
(if (not (null? naoji))
  (begin
    (import "jp.ac.fit.asura.nao.naoji.motion.*")
    (define naojiwalker (NaojiWalker. jalmotion 30))
    (.setWalkConfig jalmotion 0.04f 0.02f 0.02f 0.3f 0.02f 0.018f)
    (.setWalkExtraConfig jalmotion 3.5f -3.5f 0.23f 0)
    (mc-registmotion2 80 naojiwalker)
  )
)
