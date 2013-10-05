(ns warmagnet.components
  (:require-macros [pump.macros :refer [defr]])
  (:require [pump :refer [react]]
            [warmagnet.utils :refer [log]]
            [warmagnet.handlers :as handlers]))

(defn tags [tagname values]
  (map (fn [v] [tagname v]) values))

(defr Leaderboard
  [C {:keys [players]} S]
  [:table.table.table-striped
   [:thead [:tr (tags :td ["Name" "Score" "Completed" "Won" "User Rating"])]]
   [:tbody (map (fn [p] [:tr (tags :td [(p :name) (p :score)
                                        (p :completed) (p :won)
                                        (p :user-rating)])])
                players)]])

(defr Profile
  [C P S]
  [:div "Profile!"])
