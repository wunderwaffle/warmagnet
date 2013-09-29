(ns warmagnet.main
  (:require-macros [pump.def-macros :refer [defr]])
  (:require [pump.core]
            [warmagnet.api :refer [ws]]
            [warmagnet.handlers :as handlers]
            [warmagnet.utils :refer [log send-message setup-auth]]
            [warmagnet.world :refer [world]]
            [warmagnet.components :as components]
            [warmagnet.views.navbar :refer [Navbar]]
            [warmagnet.views.games :as games]
            [warmagnet.views.gamemap :as gamemap]
            [warmagnet.views.index :as index]
            [warmagnet.views.prefs :as prefs]))

(aset ws "onmessage"
      (fn [e]
        (send-message (js->clj (.parse js/JSON (.-data e))
                               :keywordize-keys true))))

(defr Root
  {:render (fn [C props S]
             (let [{:keys [user games route] :as P} (aget props "props")]
               [:div
                [Navbar P]
                [:div.container {:style {:margin-top "70px"}}
                 (log route)

                 (if-not user
                   [index/Index]

                   (condp = (:route P)
                     "" [games/GameList games]
                     "preferences" [prefs/Preferences user]
                     "profile" [components/Profile user]
                     "games" [games/GameList games]
                     "games/new" [games/NewGame]
                     "map" [gamemap/GameMap (:map P)]
                     [:div (str "UNKNOWN ROUTE: " route)]))]]))})

(defn current-route
  []
  (.slice (.. js/window -location -hash) 1))

(defn ^:export main
  []
  (aset js/window "ws" ws)
  (setup-auth (:user @world) handlers/login handlers/logout)
  (if (:user @world) (aset ws "onopen"
                           #(handlers/do-login (handlers/get-token))))

  (send-message {:type :route :data (current-route)})

  (let [root-el (.-body js/document)
        root (React/renderComponent (Root (js-obj "props" @world)) root-el)]
    (.addEventListener js/window "hashchange"
                       (fn [e]
                         (send-message {:type :route :data (current-route)})))
    (add-watch world :world-watcher
               (fn [key ref old new]
                 (.setProps root (js-obj "props" new))))))
