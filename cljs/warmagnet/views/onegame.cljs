(ns warmagnet.views.onegame
  (:require-macros [pump.def-macros :refer [defr]])
  (:require [warmagnet.views.gamemap :refer [GameMap]]))

(defr Game
  [C {:keys [map game]} S]
  [:div
   [GameMap map]
   [:p (pr-str game)]])
