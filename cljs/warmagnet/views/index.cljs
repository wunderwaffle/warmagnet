(ns warmagnet.views.index
  (:require-macros [pump.def-macros :refer [defr]])
  (:require [warmagnet.handlers :as handlers]))

(def intro
  [:blockquote 
   [:p
    "War pigs deliver all their madness"
    [:br]
    "War criminals thrive on death and sadness"
    [:br]
    "Hate breeds hate breeds sing whoa"
    [:br]
    "Blood fire war hate will never end"
    [:small "Soulfly - Blood Fire War Hate"]]])

(defr Index
  {:render (fn [C P S]
             [:div.jumbotron
              [:div.container
               [:div.pull-right
                [:iframe {:width 560 :height 315
                          :src "//www.youtube.com/embed/BHOSjT8gQXY?rel=0&start=236"
                          :frameborder 0
                          :allowfullscreen true}]]
               [:h1 "War Magnet"]
               intro
               [:p "Join exciting world of rivalry and domination today!"]
               [:button.btn.btn-success.btn-lg
                {:type "button" :on-click handlers/persona-sign-in}
                "Sign In"]]])})
