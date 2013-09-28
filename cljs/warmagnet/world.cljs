(ns warmagnet.world
  (:require [warmagnet.crossover.data :refer [game-transition]]))

(def world (atom {:user nil
                  :games {}}))

(defn world-transition [world {:keys [type] :as msg}]
  (condp = (keyword type)
    :login (assoc world :user (:user msg))
    :game (update-in world [(:game-id msg)] game-transition msg)))
