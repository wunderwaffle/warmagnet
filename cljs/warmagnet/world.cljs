(ns warmagnet.world
  (:require [warmagnet.crossover.data :refer [game-transition]]))

(def world (atom {:user nil
                  :route "/"
                  :games {}}))

(defn world-transition [world {:keys [type data] :as msg}]
  (condp = (if (string? type) (keyword type) type)
    :login (assoc world :user data)
    :route (assoc world :route data)
    :game (update-in world [(:game-id msg)] game-transition data)))
