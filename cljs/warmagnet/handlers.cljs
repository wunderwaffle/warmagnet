(ns warmagnet.handlers)

(defn new-game [e]
  (do
    (.preventDefault e)))

(defn persona-sign-in []
  (.request navigator/id))
