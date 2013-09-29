(ns warmagnet.views.onegame
  (:require-macros [pump.def-macros :refer [defr]])
  (:require [warmagnet.views.gamemap :refer [GameMap]]))

(defn log->text [log]
  (case (keyword (:type log))
    :join (str "User " (:user-name log) " joined the game")
    :start "Game started"
    :set-district nil
    (str "Event: " (:type log) " by " (:user-name log))))

(defr Game
  [C {:keys [map game]} S]
  [:div
   [GameMap map]

   [:p.lead "Stats"]
   [:div [:b "Round duration: "] (:duration (:options game))]
   [:div [:b "Reinforcement: "] (:reinforcement (:options game))]

   [:div.stats.well.clearfix
    [:table.table
     [:thead
      [:tr
       [:th
        [:th "Name"] [:th "Regions"] [:th "Troops"] [:th "Bonus"]]]]
     [:tbody
      (for [player (:players game)]
        [:tr [:td ] [:td (:name player)] [:td 22] [:td 11] [:td 33]])]]]

   [:p.lead "Game Log"]
   [:div.log.well

    [:ul
     (for [gamelog (:log game)
           :let [text (log->text gamelog)]]
       (if text
         [:li.text-success text]))]]
   [:p (pr-str game)]])
