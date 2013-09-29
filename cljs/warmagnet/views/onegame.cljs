(ns warmagnet.views.onegame
  (:require-macros [pump.def-macros :refer [defr]])
  (:require
   [warmagnet.utils :refer [log]]
   [warmagnet.views.gamemap :refer [GameMap]]))

(defn get-player [game user-id]
  (first (filter #(= (:id %) user-id) (:players game))))

(defn get-stats [game player]
  ((keyword (str (:id player))) (:player-state game)))

(defn log->text [game {:keys [type user-id] :as log}]
  (let [{:keys [name]} (get-player game user-id)]
    (case (keyword type)
      :join (str "User " name " joined the game")
      :start "Game started"
      :set-district nil
      :turn (str "Turn of " name)
      :supply (str name " received " (:amount log)
                   " troops because of " (:reason log))
      (str "Event: " (:type log) " by " name))))

(defn user-stats [districts user-id]
  (let [stats {:regions 0 :troops 0}]
    (log (vals districts))
    (for [district (vals districts)]
      (if (= (:user-id district) user-id)
        (do (assoc stats :regions (inc (:regions stats)))
            (assoc stats :troops (+ (:troops stats) (:amount district))))))
    stats))

(defr Game
  [C {:keys [game] :as P} S]
  [:div
   [GameMap P]

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
      (for [player (:players game)
            :let [stats (get-stats game player)]]
        (if (:player-state game)
        [:tr [:td ] [:td (:name player)] [:td 22] [:td 11] [:td (:supply stats)]]))]]]

   [:p.lead "Game Log"]
   [:div.log.well

    [:ul
     (for [gamelog (:log game)
           :let [text (log->text game gamelog)]]
       (if text
         [:li.text-success [:small text]]))]]
   #_ [:p (pr-str game)]])
