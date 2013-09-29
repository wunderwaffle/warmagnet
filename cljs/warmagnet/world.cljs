(ns warmagnet.world
  (:require [warmagnet.crossover.data :refer [game-transition]]))

(defn get-user []
  (let [user (.getItem (.-localStorage js/window) "user")
        json (.parse js/JSON (clj->js user))]
    (.log js/console "USER" json)
    json))

(defn set-user [user]
  (.setItem (.-localStorage js/window) "user"
            (.stringify js/JSON (clj->js user))))

(defn remove-user []
  (.removeItem (.-localStorage js/window) "user"))

(def world (atom {:user (get-user)
                  :route "/"
                  :games {}}))

(defn world-transition [world {:keys [type data] :as msg}]
  (case (if (string? type) (keyword type) type)
    :error (do (js/alert "Server error")
               world)
    :login (do (set-user data) (assoc world :user data))
    :logout (do (remove-user) (dissoc world :user))
    :update-user (update-in world [:user] merge data)
    :route (assoc world :route data)
    :game-state (assoc-in world [:games (:id data)]
                          [(:log data)
                           (:options data)])
    :game (update-in world [(:id data)] game-transition data)
    :map-received (assoc world :map data)))
