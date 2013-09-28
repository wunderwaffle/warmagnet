(ns warmagnet.main
  (:require-macros [pump.def-macros :refer [defr]])
  (:require [pump.core]
            [warmagnet.crossover.data :refer [world-transition]]
            [warmagnet.api :refer [ws]]
            [warmagnet.handlers :refer [setup-auth]]
            [warmagnet.components :as components]))

(def world (atom {:user nil}))

(defn send-message [message]
  (swap! world world-transition message))

(defr Root
  {:render (fn [C props S]
             (let [P (aget props "props")]
               (.log js/console P)
               [:div
               [components/Navbar (:user P)]
                [:div {:style {:margin-top "50px"}}  (str "Username is " (:user P))
                [:br]
                [:button {:on-click #(send-message {:type :user :value "hahaha"})} "change"]]]))})

(defn ^:export main
  []
  (aset js/window "ws" ws)
  (setup-auth)
  (let [root-el (.-body js/document)
        root (React/renderComponent (Root (js-obj "props" @world)) root-el)]
    (add-watch world :world-watcher
               (fn [key ref old new]
                 (.setProps root (js-obj "props" new))))))
