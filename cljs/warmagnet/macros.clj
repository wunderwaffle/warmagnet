(ns warmagnet.macros)

(defmacro cx [& classes]
  (let [class-map# (partition 2 classes)]
    `(str ~@(for [[class# test#] class-map#]
              `(if ~test# (str (name ~class#) " ") "")))))
