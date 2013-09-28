(ns warmagnet.components
  (:require-macros [pump.def-macros :refer [defr]])
  (:require
   [pump.core :refer [react]]
   [warmagnet.handlers :as handlers]))

(defr NewGame
  {:render (fn [C P S]
             [:form {:role "form" :on-submit handlers/new-game}
              [:div.form-group
               [:label {:for "round_time"} "Round time"]
               [:select#round_time.form-control
                [:option {:value "short"} "5 mins"]
                [:option {:value "long"} "24 hours"]]]
              [:div.form-group
               [:label {:for "reinforcement"} "Reinforcements"]
               [:select#reinforcement.form-control
                [:option {:value "adjacent"} "Adjacent"]
                [:option {:value "chained"} "Chained"]
                [:option {:value "unlimited"} "Unlimited"]]]
              [:button.btn.btn-success {:type "submit"} "Create"]])})

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
