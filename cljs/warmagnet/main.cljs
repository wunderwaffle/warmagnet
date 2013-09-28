(ns warmagnet.main
  (:require-macros [pump.def-macros :refer [defr]])
  (:require [pump.core]
            [warmagnet.components :as components]
            [warmagnet.crossover.data :refer [world-transition]]
            [warmagnet.api :refer [ws]]))

(def world (atom {:user nil}))

(defn send-message [message]
  (swap! world world-transition message))

(defr Root
  {:render (fn [C props S]
             (let [P (aget props "props")]
               (.log js/console P)
               [:div
                [components/Navbar]
                [:div (str "Username is " (:user P))
                 [:br]
                 [:button {:on-click #(send-message {:type :user :value "hahaha"})} "change"]]]))})

(defn setup-auth []
  (.watch navigator/id
          (clj->js
           {:loggedInUser nil
            :onlogin #(js/alert "Logged in")
            :onlogout #(js/alert "Logged out")})) )

(defn ^:export main
  []
  (aset js/window "ws" ws)
  (setup-auth)
  (let [root-el (.-body js/document)
        root (React/renderComponent (Root (js-obj "props" @world)) root-el)]
    (add-watch world :world-watcher
               (fn [key ref old new]
                 (.setProps root (js-obj "props" new))))))
