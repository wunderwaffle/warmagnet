(ns warmagnet.views.onegame
  (:require-macros [pump.def-macros :refer [defr]])
  (:require [warmagnet.views.gamemap :refer [GameMap]]))

(defn log->text [log]
  (case (:type log)
    "join" "User " (:user-name log) " joined the game"
    "Event: " (:type log) " by " (:user-name log)))

(defr Game
  [C {:keys [map game]} S]
  [:div
   [GameMap map]
   [:p (pr-str game)]])
