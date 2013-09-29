(ns warmagnet.crossover.data)

(defn game-transition [world {:keys [type] :as msg}]
  (condp = type
    ; this is not relevant, game transitions should be here
    :user (assoc world :user nil)))
