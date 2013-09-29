(ns warmagnet.views.games
  (:require-macros [pump.def-macros :refer [defr]]
                   [warmagnet.macros :refer [cx]])
  (:require [clojure.string :refer [capitalize]]
            [pump.core :refer [assoc-in-state e-value]]

            [warmagnet.components :refer [tags]]
            [warmagnet.utils :refer [log redir send-message send-message-srv]]
            [warmagnet.handlers :as handlers]))

(defn button [name value current on-click]
  (let [primary (= value current)]
    [:button {:class (cx :btn true
                         :btn-default (not primary)
                         :btn-primary primary)
              :on-click #(do (.preventDefault %) (on-click value))}
     name]))

(defr NewGame
  :get-initial-state #(identity {:size 2
                                 :duration :short
                                 :reinforcement :adjacent})
  [C P
   {:keys [size duration reinforcement] :as S}]

  [:form.col-md-6.col-md-offset-2
   {:role "form" :on-submit #(do (handlers/new-game % S)
                                 (redir "games"))}

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
  [C {:keys [id gamelog game players player-list children]} S]
  #_ (log (pr-str game))

  [:div.game-item.well.clearfix
   [:h2.pull-right id]

   [:div.smallmap.col-md-3
    [:img {:src "/static/map-classic.jpg" :width 180 :height 100 }]]

   [:div.stats.col-md-4
    [:p [:b "Number of players: "] (if players (+ players " of ")) (:size game)]
    [:p [:b "Round duration: "] (:duration game)]
    [:p [:b "Reinforcement: "] (:reinforcement game)]]

   [:div.col-md-4
    [:p [:b "Players"]]
    [:ul
     (for [[id name] player-list]
       [:li name])]]

   [:div.col-md-offset-10
    children]])

(defr GameList
  [C {:keys [games]} S]
  (if (empty? games)
    [:div [:p.lead "No Games. "
           [:a {:href "#games/new"} "Go and create one!"]]]
    [:div.col-md-12
     [:div (for [[id {:keys [players log options]}] games]
            [GameItem {:key id
                       :id id
                       :gamelog log
                       :game options
                       :player-list players
                       :children [:button.btn.btn-default
                                  {:on-click #(redir (str "games/" id))} "Open"]}])]]))

(defr AllGameList
  :component-did-mount (fn [C P S]
                         (send-message-srv {:type :game-list}))
  [C {:keys [games user]} S]
  (if (empty? games)
    [:div [:p.lead "No Games. "
           [:a {:href "#games/new"} "Go and create one!"]]]

    [:div.col-md-12
     [:div
      [:button.btn.btn-default.pull-right
       {:on-click #(send-message-srv {:type :game-list})}
       "Refresh"]
      [:p.lead "This game list is not refreshed automatically. Use a button."]]

     (for [{:keys [id data players player-list]} games]
       [GameItem {:key id
                  :id id
                  :game data
                  :players players
                  :player-list (map (juxt :id :name) player-list)
                  :children (if (and (< players (:size data))
                                     (not (some #{(:id user)} (map :id player-list))))
                              [:button.btn.btn-default
                               {:on-click #(handlers/join-game id)} "Join"])}])]))


