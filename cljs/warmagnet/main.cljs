(ns warmagnet.main
  (:require-macros [pump.def-macros :refer [defr]])
  (:require [tailrecursion.cljson :refer [clj->cljson cljson->clj]]
            [pump.core]
            [warmagnet.components :as components]
            [warmagnet.crossover.data :refer [game-transition]]
            [warmagnet.handlers :refer [setup-auth]]
            [warmagnet.api :refer [ws]]))

(def world (atom {:user nil
                  :games {}}))

(defn world-transition [world {:keys [type] :as msg}]
  (condp = type
    :login (assoc world :user (:user msg))
    :game (update-in world [(:game-id msg)] game-transition msg)))

(defn send-message [message]
  (swap! world world-transition message))

(defn send-message-srv [msg]
  (.send ws (clj->cljson msg)))

(aset ws "onmessage" #(send-message (cljson->clj (.-data %))))

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
