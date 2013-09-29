(ns warmagnet.views.games
  (:require-macros [pump.def-macros :refer [defr]])
  (:require [pump.core :refer [assoc-state e-value]]

            [warmagnet.utils :refer [send-message]]
            [warmagnet.handlers :as handlers]))

(defr NewGame
  {:get-initial-state #(identity {:duration "short"
                                  :reinforcement "adjacent"})
   :render (fn [C P
                {:keys [duration reinforcement] :as S}]
             [:form
              {:role "form" :on-submit #(handlers/new-game % S)}

              [:div.form-group
               [:label.control-label {:htmlFor "round_time"} "Round time"]

               [:select#round_time.form-control.col-lg-10
                {:onChange #(assoc-state C :duration (e-value %))
                 :value duration}
                [:option {:value "short"} "5 minutes"]
                [:option {:value "long"} "24 hours"]]]

              [:div.form-group
               [:label.control-label {:htmlFor "reinforcement"} "Reinforcements"]
               [:select#reinforcement.form-control
                {:onChange #(assoc-state C :reinforcement (e-value %))
                 :value reinforcement}
                [:option {:value "adjacent"} "Adjacent"]
                [:option {:value "chained"} "Chained"]
                [:option {:value "unlimited"} "Unlimited"]]]

              [:button.btn.btn-success {:type "submit"} "Create"]])})

(defr GameItem
  {:render (fn [C {:keys [game-map participants round-time reinforcements]} S]
             [:div
              [:img {:src game-map}]
              [:p "Players"]
              [:ul [tags :li (map :name participants)]]
              [:p round-time]
              [:p reinforcements]])})

(defr GameList
  {:render (fn [C P S]
             (if (empty? P)
               [:div "NO GAMES"]
               [:div
                (for [[id game] P]
                  [GameItem game])]))})
