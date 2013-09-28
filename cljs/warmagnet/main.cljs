(ns warmagnet.main
  (:require-macros [pump.def-macros :refer [defr]])
  (:require [pump.core]
            [warmagnet.api :refer [ws]]
            [warmagnet.handlers :as handlers]
            [warmagnet.utils :refer [send-message setup-auth]]
            [warmagnet.world :refer [world]]
            [warmagnet.components :as components]
            [warmagnet.views.games :as games]
            [warmagnet.views.index :as index]))

(aset ws "onmessage"
      (fn [e]
        (send-message (js->clj (.parse js/JSON (.-data e))
                               :keywordize-keys true))))

(defr Root
  {:render (fn [C props S]
             (let [{:keys [user games route] :as P} (aget props "props")]
               [:div
                [components/Navbar P]
                [:div.container {:style {:margin-top "70px"}}
                 (.log js/console (:route P))

                 (if-not user
                   [index/Index]

                   (condp = (:route P)
                     "" [games/List games]
                     "preferences" [components/Preferences user]
                     "profile" [components/Profile user]
                     "games" [games/List games]
                     "games/new" [games/NewGame]
                     [:div (str "UNKNOWN ROUTE: " route)]))]]))})

(defn current-route
  []
  (.slice (.. js/window -location -hash) 1))

(defn ^:export main
  []
  (aset js/window "ws" ws)
  (setup-auth (:user @world) handlers/login handlers/logout)
  (send-message {:type :route :data (current-route)})

  (let [root-el (.-body js/document)
        root (React/renderComponent (Root (js-obj "props" @world)) root-el)]
    (.addEventListener js/window "hashchange"
                       (fn [e]
                         (send-message {:type :route :data (current-route)})))
    (add-watch world :world-watcher
               (fn [key ref old new]
                 (.setProps root (js-obj "props" new))))))
