(ns warmagnet.main
  (:require-macros [pump.def-macros :refer [defr]])
  (:require [pump.core]
            [warmagnet.api :refer [ws]]
            [warmagnet.handlers :as handlers]
            [warmagnet.utils :refer [send-message setup-auth]]
            [warmagnet.world :refer [world]]
            [warmagnet.components :as components]))

(aset ws "onmessage"
      #(send-message (js->clj (.parse js/JSON (.-data %))
                              :keywordize-keys true)))

(defr Root
  {:render (fn [C props S]
             (let [P (aget props "props")]
               (.log js/console P)
               [:div
               [components/Navbar P]
                [:div {:style {:margin-top "50px"}}  (str "Username is " (:user P))
                [:br]
                [:button {:on-click #(send-message {:type :user :value "hahaha"})} "change"]]]))})

(defn ^:export main
  []
  (aset js/window "ws" ws)
  (setup-auth (:user @world) handlers/login handlers/logout)
  (let [root-el (.-body js/document)
        root (React/renderComponent (Root (js-obj "props" @world)) root-el)]
    (add-watch world :world-watcher
               (fn [key ref old new]
                 (.setProps root (js-obj "props" new))))))
