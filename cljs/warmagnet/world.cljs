(ns warmagnet.world
  (:require [warmagnet.crossover.data :refer [game-transition]]))

(defn get-user []
  (let [user (.getItem (.-localStorage js/window) "user")
        json (.parse js/JSON user)]
    (if json (js->clj json :keywordize-keys true) nil)))

(defn set-user [user]
  (.setItem (.-localStorage js/window) "user"
            (.stringify js/JSON (clj->js user))))

(defn update-user [data]
  (.setItem (.-localStorage js/window) "user"
            (.stringify js/JSON (clj->js (merge (get-user) data)))))

(defn remove-user []
  (.removeItem (.-localStorage js/window) "token")
  (.removeItem (.-localStorage js/window) "user"))

(def world (atom {:user (get-user)
                  :route "/"
                  :games {}}))

(defn fix-game-state [game]
  (-> game
      (assoc :districts
        (into {} (for [[k v] (:districts game)]
                   [(name k) v])))
      (assoc :player-state
        (into {} (for [[k v] (:player-state game)]
                   [(js/parseInt (name k) 10) v])))))

(defn world-transition [world {:keys [type data] :as msg}]
  (case (if (string? type) (keyword type) type)
    :error (do (js/alert "Server error")
               world)
    :login (do (set-user data) (assoc world :user data))
    :login-error (do (remove-user) (dissoc world :user))
    :join-error world
    :logout (do (remove-user) (dissoc world :user))
    :update-user (do (update-user data) (update-in world [:user] merge data))
    :route (assoc world :route data)
    :game-state (if-not (empty? data)
                  (assoc-in world [:games (:id data)] (fix-game-state data))
                  world)
    :game (update-in world [:games (:game-id msg)] game-transition data)
    :container-width (assoc world :container-width data)
    :map-received (assoc world :game-map data)
    :game-list (assoc world :allgames data)))
