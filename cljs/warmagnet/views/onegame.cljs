(ns warmagnet.views.onegame
  (:require-macros [pump.def-macros :refer [defr]])
  (:require [warmagnet.views.gamemap :refer [GameMap]]))

(defn log->text [log]
  (case (:type log)
    "join" (str "User " (:user-name log) " joined the game")
    (str "Event: " (:type log) " by " (:user-name log))))

(defr Game
  [C {:keys [map game]} S]
  [:div
   [GameMap map]
   [:div.log.well
    [:p.lead "Game Log"]
    [:ul
   (for [gamelog (:log game)]
     [:li.text-success (log->text gamelog)])]]
   [:p (pr-str game)]])
