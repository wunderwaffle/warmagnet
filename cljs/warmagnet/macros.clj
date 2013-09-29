(ns warmagnet.macros)

(defmacro cx [& classes]
  (let [class-map# (partition 2 classes)]
    `(str ~@(apply concat (for [[class# test#] class-map#]
                            [`(if ~test# (name ~class#)) " "])))))
