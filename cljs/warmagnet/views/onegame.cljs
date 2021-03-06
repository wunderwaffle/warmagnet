(ns warmagnet.views.onegame
  (:require-macros [pump.macros :refer [defr]])
  (:require [goog.string :as gs]
            [goog.string.format]
            [warmagnet.utils :refer [send-log log]]
            [warmagnet.views.gamemap :refer [player-color GameMap]]))

(defn get-player [game user-id]
  (first (filter #(= (:id %) user-id) (:players game))))

(defn get-stats [game user-id]
  ((:player-state game) user-id))

(defn log->text [game {:keys [type user-id] :as log}]
  (let [{:keys [name]} (get-player game user-id)]
    (case (keyword type)
      :join (str "User " name " joined the game")
      :start "Game started"
      :set-district nil
      :turn (gs/format "Turn of %s" name)
      :supply (gs/format "%s received %d troops because of %s"
                         name (:amount log) (:reason log))
      :deploy (gs/format "%s deployed %d troops on %s"
                         name (:amount log) (:district log))
      :phase nil
      :attack (gs/format "%s attacked %s from %s with %d troops"
                         name (:attack-to log) (:attack-from log) (:amount log))
      :conquer (gs/format "%s conquered %s"
                          name (:district log))
      (str "Event: " type ", " (pr-str log)))))

(defn regions [game user-id]
  (count
   (filter #(= (:user-id %) user-id) (vals (:districts game)))))

(defn troops [game user-id]
  (reduce +
          (map #(:amount % )
               (filter #(= (:user-id %) user-id)
                       (vals (:districts game))))))

(defr Game
  [C {:keys [game user] :as P} S]
  (if-not game
    [:div "Loading"]

    [:div
     (let [active-player-id (:turn-by game)
           user-id (:id user)
           phase (:phase (get-stats game active-player-id))
           game-id (:id game)]
       [:p.lead "Turn by: "
        (:name (get-player game active-player-id))
        " | Phase: " phase " "
        (if (= user-id active-player-id)
          (case phase
            "attack" [:button.btn.btn-warning
                      {:on-click #(send-log game-id {:type :attack-end})}
                      "End attack"]
            "reinforce" [:button.btn.btn-warning
                         {:on-click #(send-log game-id {:type :reinforce-end})}
                         "End reinforcements"]
            nil))])
     
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
              :let [id (:id player)
                    stats (get-stats game id)]]
          [:tr [:td
                {:style {:background-color (player-color game id)}}]
           [:td (:name player)]
           [:td (regions game id)]
           [:td (troops game id)]
           [:td (:supply stats)]])]]]

     [:p.lead "Game Log"]
     [:div.log.well

      [:ul
       (for [gamelog (:log game)
             :let [text (log->text game gamelog)]]
         (if text
           [:li.text-success [:small text]]))]]
     #_ [:p (pr-str game)]]))
