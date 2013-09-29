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

   [:p.lead "Stats"]
   [:div.stats.well
    [:table.table
     [:thead
      [:tr
       [:th
        [:th "Name"] [:th "Regions"] [:th "Troops"] [:th "Bonus"]]]]
     [:tbody
      (for [player (vals (:players game))]
        [:tr [:td ] [:td player] [:td 22] [:td 11] [:td 33]])]]]

   [:p.lead "Game Log"]
   [:div.log.well

    [:ul
   (for [gamelog (:log game)]
     [:li.text-success (log->text gamelog)])]]
   [:p (pr-str game)]])
