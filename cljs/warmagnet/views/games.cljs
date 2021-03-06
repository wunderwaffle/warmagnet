(ns warmagnet.views.games
  (:require-macros [pump.macros :refer [defr]]
                   [warmagnet.macros :refer [cx]])
  (:require [clojure.string :refer [capitalize]]

            [warmagnet.components :refer [tags]]
            [warmagnet.utils :refer [log redir send-message send-message-srv]]
            [warmagnet.handlers :as handlers]))

(defn button-toggle [name value current on-click]
  (let [primary (= value current)]
    [:button {:class (cx :btn true
                         :btn-default (not primary)
                         :btn-primary primary)
              :on-click #(do (.preventDefault %) (on-click value))}
     name]))

(defn button [text on-click]
  [:button.btn.btn-default {:on-click on-click} text])

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
       (button-toggle (str n " players") n size
               #(swap! C assoc :size %)))]]

   [:div.form-group
    [:label.control-label "Round time"]
    [:div.btn-group.btn-panel
     (for [[name val] [["5 minutes" :short]
                       ["24 hours" :long]]]
       (button-toggle name val duration
               #(swap! C assoc :duration %)))]]

   [:div.form-group
    [:label.control-label "Reinforcements"]
    [:div.btn-group.btn-panel
     (for [val [:adjacent :chained :unlimited]]
       (button-toggle (capitalize (name val)) val reinforcement
               #(swap! C assoc :reinforcement %)))]]

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
                       :player-list (map (juxt :id :name) players)
                       :children [(button "Open" #(redir (str "games/" id)))]}])]]))

(defn refresh-all-games []
  (send-message-srv {:type :game-list}))

(defr AllGameList
  :component-did-mount refresh-all-games
  [C {:keys [games user]} S]

  [:div
   [:div
    [:button.btn.btn-default.pull-right
     {:on-click refresh-all-games}
     [:i.icon-refresh]
     " Refresh"]
    [:p.lead "This game list is not refreshed automatically. Use a button."]]

   (if (empty? games)
     [:div
      [:p.lead "No Games. "
       [:a {:href "#games/new"} "Go and create one!"]]]

     [:div.col-md-12
      (for [{:keys [id data players player-list]} games]
        [GameItem {:key id
                   :id id
                   :game data
                   :players players
                   :player-list (map (juxt :id :name) player-list)
                   :children (seq [(button "Open" #(redir (str "games/" id)))
                                   (if (and (< players (:size data))
                                            (not (some #{(:id user)} (map :id player-list))))
                                     (button "Join" #(do (handlers/join-game id)
                                                         (js/setTimeout refresh-all-games 10))))])}])])])
