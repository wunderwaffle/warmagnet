(ns warmagnet.main
  (:require-macros [pump.def-macros :refer [defr]])
  (:require [pump.core :refer [assoc-state]]
            [warmagnet.handlers :as handlers]
            [warmagnet.utils :refer [log send-message setup-auth]]
            [warmagnet.world :refer [world]]
            [warmagnet.components :as components]
            [warmagnet.views.navbar :refer [Navbar]]
            [warmagnet.views.games :as games]
            [warmagnet.views.gamemap :as gamemap]
            [warmagnet.views.index :as index]
            [warmagnet.views.prefs :as prefs]))

(aset js/window "WS_URL" "ws://localhost:3000/ws")

(defr Root
  :component-did-mount (fn [C P S node]
                         (send-message {:type :container-width
                                        :data (.-clientWidth node)}))

  [C {:keys [user allgames games route map container-width]} S]
  [:div.container {:style {:margin-top "70px"}}
   (log route)

   (if-not user
     [index/Index]

     (case route
       "" [games/GameList games]
       "preferences" [prefs/Preferences user]
       "profile" [components/Profile user]
       "browse" [games/AllGameList {:games allgames}]
       "games" [games/GameList {:games games}]
       "games/new" [games/NewGame]
       "map" [gamemap/GameMap (assoc map :container-width container-width)]
       [:div (str "UNKNOWN ROUTE: " route)]))])

(defr Wrapper
  [C props S]
  (let [P (aget props "props")]
    [:div
     [Navbar P]
     [Root P]]))

(defn current-route
  []
  (.slice (.. js/window -location -hash) 1))

(defn ^:export main
  []

  (aset js/ws "onmessage"
        (fn [e]
          (log (str "<- " (.-data e)))
          (send-message (js->clj (.parse js/JSON (.-data e))
                                 :keywordize-keys true))))

  (setup-auth (:user @world) handlers/login handlers/logout)
  (if (:user @world)
    (handlers/do-login (handlers/get-token)))

  (send-message {:type :route :data (current-route)})

  (let [root-el (.-body js/document)
        root (React/renderComponent (Wrapper (js-obj "props" @world)) root-el)]
    (.addEventListener js/window "hashchange"
                       (fn [e]
                         (send-message {:type :route :data (current-route)})))
    (add-watch world :world-watcher
               (fn [key ref old new]
                 (.setProps root (js-obj "props" new))))))
