(ns warmagnet.main
  (:require-macros [pump.def-macros :refer [defr]])
  (:require [pump.core]
            [warmagnet.api :refer [ws]]))

(def world (atom {:user nil}))

(defr Root {:render (fn [C {:keys [user]} S]
                      [:div (str "Username is" user)
                       [:button {:on-click #(swap! atom assoc :user "tralala")} "fuck"]])})

(defn ^:export main
  []
  (aset js/window "ws" ws)
  (let [root-el (.-body js/document)
        root (React/renderComponent (Root @world) root-el)]
    (add-watch world :world-watcher
               (fn [key ref old new]
                 (.setProps root new)))))
