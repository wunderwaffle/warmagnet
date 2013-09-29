(ns warmagnet.world
  (:require [warmagnet.crossover.data :refer [game-transition]]))

(defn get-user []
  (let [user (.getItem (.-localStorage js/window) "user")
        json (.parse js/JSON (clj->js user))]
    (.log js/console "USER" user)
    (js->clj user)))

(defn set-user [user]
  (.setItem (.-localStorage js/window) "user"
            (.stringify js/JSON (clj->js user))))

(def world (atom {:user (get-user)
                  :route "/"
                  :games {}}))

(defn world-transition [world {:keys [type data] :as msg}]
  (condp = (if (string? type) (keyword type) type)
    :error (do (js/alert "Server error")
               world)
    :login (do (assoc world :user data) (set-user data))
    :logout (do (dissoc world :user) (set-user nil))
    :route (assoc world :route data)
    :game-state (assoc-in world [:games (:id data)]
                          [(:log data)
                           (:options data)])
    :game (update-in world [(:id data)] game-transition data)
    :map-received (assoc world :map data)))
