(ns warmagnet.main
  (:require-macros [pump.def-macros :refer [defr]])
  (:require [pump.core]))

(def ws-url "ws://localhost:8081/ws")
(def ws (js/WebSocket. ws-url))

(defr Root {:render (fn [C P S]
                      [:div "text"])})

(defn ^:export main
  []
  (React/renderComponent (Root nil) (.-body js/document)))
