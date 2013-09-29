(ns warmagnet.views.games
  (:require-macros [pump.def-macros :refer [defr]]
                   [warmagnet.macros :refer [cx]])
  (:require [clojure.string :refer [capitalize]]
            [pump.core :refer [assoc-in-state e-value]]

            [warmagnet.components :refer [tags]]
            [warmagnet.utils :refer [log send-message send-message-srv]]
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
    [:div.btn-group.btn-panel
     (for [n (range 2 8)]
       (button (str n " players") n size
               #(assoc-in-state C :size %)))]]

   [:div.form-group
    [:label.control-label "Round time"]
    [:div.btn-group.btn-panel
     (for [[name val] [["5 minutes" :short]
                       ["24 hours" :long]]]
       (button name val duration
               #(assoc-in-state C :duration %)))]]

   [:div.form-group
    [:label.control-label "Reinforcements"]
    [:div.btn-group.btn-panel
     (for [val [:adjacent :chained :unlimited]]
       (button (capitalize (name val)) val reinforcement
               #(assoc-in-state C :reinforcement %)))]]

   [:button.btn.btn-success.btn-lg {:type "submit"} "Create"]])

(defr GameItem
  [C {:keys [gamelog game]} S]
  (log (pr-str game))
  [:div.well
;;   [:img {:src game-map}]
;;   [:p "Players"]
;;   [:ul (tags :li (map :name players))]
   [:p [:b "Round duration: "] (:duration game)]
   [:p [:b "Reinforcement: "] (:reinforcement game)]])

(defr GameList
  [C {:keys [games]} S]
  (if (empty? games)
    [:div [:p.lead "No Games. "
           [:a {:href "#games/new"}
            "Go and create one!"]]]
    [:div.col-md-offset-2.col-md-6 (for [[id [log game]] games]
            [GameItem {:gamelog log :game  game}])]))

(defr AllGameList
  :component-will-mount (fn [C P S]
                          (if (empty? P)
                            (send-message-srv {:type :game-list})))
  [C {:keys [games]} S]
  (if (empty? games)
    [:div "SPIN SPIN SPIN"]
    [:div (pr-str games)]))

