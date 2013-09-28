(ns warmagnet.app
  (:require [org.httpkit.server :as hk]
            [cheshire.core :as json]
            [taoensso.timbre :refer [debug info warn error fatal]]

            [warmagnet.persona :as persona]
            [warmagnet.db :as db]
            [warmagnet.games :as games]
            ))

; User management
(def all-users (atom {}))

(defn get-user-id [state]
  (get-in @state [:user :id]))

(defn have-user [state]
  (not (nil? (:user @state))))

(defn add-user [state]
  (swap! all-users assoc (get-user-id state) state)
  (println "add" all-users))

(defn remove-user [state]
  (swap! all-users dissoc (get-user-id state))
  (println "del" all-users))

(defn log-message [prefix state text]
  (if (have-user state)
    (debug (format "%s [%s] %s" prefix (get-in @state [:user :email]) text))
    (debug (format "%s %s" prefix text))))

; Helpers
(defn send-answer [state & more]
  (let [data (json/encode (apply hash-map more))]
    (log-message "<<<" state data)
    (hk/send! (:conn @state) data)))

(defn serialize-game-state [game-state]
  ;; TODO: Filter some options
  game-state)

; Message Handlers
(defn msg-user [state msg]
  ; TODO: Token decoding to get user id
  (if-let [token (:token msg)]
    (let [user-data (persona/login token)]
      (if (= "okay" (:status user-data))
        (let [user (db/get-or-create-user (:email user-data))]
          (swap! state assoc :user user)
          (add-user state)
          (send-answer state :type "login" :status "success" :data user))
        (send-answer state :type "login" :status "invalid-token")))
    (send-answer state :type "login" :status "invalid-request")))

(defn msg-update-user [state msg]
  (let [profile (select-keys msg [:name])]
    (db/update-user (get-user-id state) profile)))

(defn msg-start-game [state msg]
  (let [game-data (:data msg)
        game-state (games/create-game game-data)]
    (send-answer state :type "game-state" :data (serialize-game-state game-state))))

(defn msg-ping [state msg]
  (send-answer state :type "pong"))

(defn msg-unknown [state]
  (send-answer state :type "error" :status "invalid-msg"))

; State management
(defn init-state [state]
    {:conn state :user nil})

(defn handle-user-msg [state msg]
  (case (:type msg)
    "ping" (msg-ping state msg)
    "update-user" (msg-update-user state msg)
    "start-game" (msg-start-game state msg)
    (msg-unknown state)))

(defn handle-anonymous-msg [state msg]
  (case (:type msg)
    "login" (msg-user state msg)
    (msg-unknown state)))

(defn handle-msg [state raw-msg]
  (let [msg (json/decode raw-msg true)]
    (log-message ">>>" state raw-msg)
    (if (have-user state)
      (handle-user-msg state msg)
      (handle-anonymous-msg state msg))))

(defn handle-close [state]
  (if (have-user state)
    (remove-user state)))

; Websocket handler
(defn ws-handler [req]
  (hk/with-channel req chan
    (let [state (atom (init-state chan))]
      (hk/on-receive chan (fn [msg] (handle-msg state msg)))
      (hk/on-close chan (fn [status] (handle-close state))))))
