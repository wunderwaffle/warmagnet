(ns warmagnet.views.games
  (:require-macros [pump.def-macros :refer [defr]]
                   [warmagnet.macros :refer [cx]])
  (:require [clojure.string :refer [capitalize]]
            [pump.core :refer [assoc-state e-value]]

            [warmagnet.components :refer [tags]]
            [warmagnet.utils :refer [send-message]]
            [warmagnet.handlers :as handlers]))

(defn button [name value current on-click]
  (let [primary (= value current)]
    [:button {:class (cx :btn true
                         :btn-default (not primary)
                         :btn-primary primary)
              :on-click #(do (.preventDefault %) (on-click value))}
     name])
  )

(defr NewGame
  :get-initial-state #(identity {:size 2
                                 :duration :short
                                 :reinforcement :adjacent})
  [C P
   {:keys [size duration reinforcement] :as S}]

  [:form
   {:role "form" :on-submit #(handlers/new-game % S)}

   [:div.form-group
    [:label.control-label "Participants"]
    [:div.btn-group
     (for [n (range 2 8)]
       (button (str n " players") n size
               #(assoc-state C :size %)))]]

   [:div.form-group
    [:label.control-label "Round time"]
    [:div.btn-group
     (for [[name val] [["5 minutes" :short]
                       ["24 hours" :long]]]
       (button name val duration
               #(assoc-state C :duration %)))]]

   [:div.form-group
    [:label.control-label "Reinforcements"]
    [:div.btn-group
     (for [val [:adjacent :chained :unlimited]]
       (button (capitalize (name val)) val reinforcement
               #(assoc-state C :reinforcement %)))]]

   [:button.btn.btn-success {:type "submit"} "Create"]])

(defr GameItem
  {:render (fn [C {:keys [game-map participants round-time reinforcements]} S]
             [:div
              [:img {:src game-map}]
              [:p "Players"]
              [:ul (tags :li (map :name participants))]
              [:p round-time]
              [:p reinforcements]])})

(defr GameList
  {:render (fn [C P S]
             (if (empty? P)
               [:div "NO GAMES"]
               [:div
                (for [[id game] P]
                  [GameItem game])]))})
