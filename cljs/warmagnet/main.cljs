(ns warmagnet.main
  (:require-macros [pump.def-macros :refer [defr]])
  (:require [pump.core]
            [warmagnet.api :refer [ws]]))

(def world (atom {:user nil}))

(defr Root
  {:render (fn [C props S]
             (let [P (aget props "props")]
               (.log js/console P)
               [:div (str "Username is " (:user P))
                [:br]
                [:button {:on-click #(swap! world assoc :user "tralala")} "change"]]))})

(defn ^:export main
  []
  (aset js/window "ws" ws)
  (let [root-el (.-body js/document)
        root (React/renderComponent (Root (js-obj "props" @world)) root-el)]
    (add-watch world :world-watcher
               (fn [key ref old new]
                 (.setProps root (js-obj "props" new))))))
