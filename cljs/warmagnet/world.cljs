(ns warmagnet.world
  (:require [warmagnet.crossover.data :refer [game-transition]]))

(def world (atom {:user nil
                  :route "/"
                  :games {}}))

(defn world-transition [world {:keys [type data] :as msg}]
  (condp = (if (string? type) (keyword type) type)
    :error (do (js/alert "Server error")
               world)
    :login (assoc world :user data)
    :route (assoc world :route data)
    :game-state (assoc-in world [:games (:id data)]
                          [(:log data)
                           (:options data)])
    :game (update-in world [(:id data)] game-transition data)
    :map-received (assoc world :map data)))
