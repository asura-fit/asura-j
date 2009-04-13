(display "Welcome to the ASURA distributed environment.\n")

(load "asura-js.scm")

; set loglevel.
(loglevel "jp.ac.fit.asura.nao" INFO)
(loglevel "jp.ac.fit.asura.nao.glue" DEBUG)
(loglevel "jp.ac.fit.asura.nao.motion" INFO)
(loglevel "jp.ac.fit.asura.nao.vision" INFO)
(loglevel "jp.ac.fit.asura.nao.vision.perception" INFO)
(loglevel "jp.ac.fit.asura.nao.localization" DEBUG)
(loglevel "jp.ac.fit.asura.nao.localization.self" INFO)
(loglevel "jp.ac.fit.asura.nao.sensation" INFO)
(loglevel "jp.ac.fit.asura.nao.strategy" DEBUG)

(load "robot-webots-NaoV3R.scm")

; load motions.
(load "motor-cortex.scm")

; load naoji
(load "naoji.scm")

; camera config
(load "camera-config.scm")

; set motorpower
(mc-motorpower 0.125f)

; load user schemes
(load "autoexec.scm")
