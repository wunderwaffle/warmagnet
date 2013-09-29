(ns warmagnet.components
  (:require-macros [pump.def-macros :refer [defr]])
  (:require
   [pump.core :refer [react]]
   [warmagnet.utils :refer [log]]
   [warmagnet.handlers :as handlers]))

(defr Preferences
  {:render (fn [C user S]
             [:div.col-md-4.col-md-offset-4
              [:h1 "Preferences"]
              [:form.well {:role "form" :on-submit #(handlers/save-prefs % C)}
               (for [input ["login" "name"]]
                 [:div.form-group
                  [:label {:html-for input} (.toUpperCase input)]
                  [:input.form-control {:ref input :value ((keyword input) user)}]])

               [:button.btn.btn-primary {:type "submit"} "Save"]
               ]])})

(defn tags [tagname values]
  (map (fn [v] [tagname v]) values))

(defr Leaderboard
  {:render (fn [C {:keys [players]} S]
             [:table.table.table-striped
              [:thead [:tr (tags :td ["Name" "Score" "Completed" "Won" "User Rating"])]]
              [:tbody (map (fn [p] [:tr (tags :td [(p :name) (p :score)
                                                   (p :completed) (p :won)
                                                   (p :user-rating)])])
                           players)]])})

(defr GameItem
  {:render (fn [C {:keys [game-map participants round-time reinforcements]} S]
             [:div
              [:img {:src game-map}]
              [:p "Players"]
              [:ul [tags :li (map :name participants)]]
              [:p round-time]
              [:p reinforcements]])})

(defr Profile
  {:render (fn [C P S]
             [:div "Profile!"])})
