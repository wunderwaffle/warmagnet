(ns warmagnet.handlers
  (:require
   [warmagnet.utils :refer [log send-message send-message-srv]]))

(defn new-game [e state]
  (.preventDefault e)
  (send-message-srv {:type :start-game
                     :data state}))

(defn persona-sign-in []
  (.request navigator/id))

(defn persona-sign-out []
  (send-message {:type :logout})
  (.logout navigator/id))

(defn get-input-value [input]
  (let [node (.getDOMNode (clj->js input))
        tag-name (.toLowerCase (.-tagName node))]
    (if (= tag-name "select")
      (.-value (clj->js (nth (vector (js->clj (.-options node))) (- (.-selectedIndex node) 1))))
      (.-value node))))

(defn serialize-form [inputs callback]
  (aset js/window "inputs" (clj->js inputs))
  (let [keys (keys inputs)
        values (map get-input-value (vals inputs))]

    (log keys)
    (callback values)))

(defn save-prefs [e prefs]
  (.preventDefault e)
  (send-message-srv {:type :update-user :data prefs})
  (send-message {:type :update-user :data prefs}))

(defn save-token [token]
  (.setItem (.-localStorage js/window) "token" (clj->js token)))

(defn get-token []
  (let [token (.getItem (.-localStorage js/window) "token")]
    (log "TOKEN" token)
    (if token (js->clj token) nil)))

(defn login [token]
  (save-token token)
  (do-login token))

(defn do-login [token]
  (send-message-srv {:type :login :token token}))

(defn logout []
  (log "logout"))

