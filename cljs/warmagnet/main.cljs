(ns warmagnet.main
  (:require-macros [pump.def-macros :refer [defr]])
  (:require [pump.core :refer [assoc-state]]
            [warmagnet.handlers :as handlers]
            [warmagnet.utils :refer [log send-message setup-auth]]
            [warmagnet.world :refer [world]]
            [warmagnet.components :as components]
            [warmagnet.views.navbar :refer [Navbar]]
            [warmagnet.views.onegame :refer [Game]]
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
   ;(log route)

   (cond
    (not user) [index/Index]
    (not (:name user)) [prefs/Preferences user]

    :else
    (condp (comp seq re-seq) route
       #"^$" [games/GameList {:games games}]
       #"preferences" [prefs/Preferences user]
       #"profile" [components/Profile user]
       #"browse" [games/AllGameList {:games allgames}]
       #"games/new" [games/NewGame]
       #"games/(\d+)" :>> (fn [[[_ id]]]
                            [Game {:game (games (js/parseInt id))
                                   :map (assoc map
                                          :container-width container-width)}])
       #"games" [games/GameList {:games games}]
       #"map" [gamemap/GameMap (assoc map :container-width container-width)]
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
  (aset js/window "pr" pr-str)
  (aset js/window "cjj" clj->js)
  (aset js/window "deref" deref)

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
