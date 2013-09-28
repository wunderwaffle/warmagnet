(ns warmagnet.crossover.data)

(defn world-transition [world {:keys [type value] :as message}]
  (condp = type
    :user (assoc world :user value)))
