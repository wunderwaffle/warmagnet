(ns warmagnet.views.onegame
  (:require-macros [pump.def-macros :refer [defr]]))

(defr Game
  [C P S]
  [:div (pr-str P)])
