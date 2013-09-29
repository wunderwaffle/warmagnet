(ns warmagnet.components
  (:require-macros [pump.def-macros :refer [defr]])
  (:require [warmagnet.utils :refer [log]]
            [warmagnet.handlers :as handlers]))

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

(defr Profile
  {:render (fn [C P S]
             [:div "Profile!"])})
