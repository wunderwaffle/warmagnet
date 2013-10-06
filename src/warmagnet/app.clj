(ns warmagnet.app
  (:require [org.httpkit.server :as hk]
            [cheshire.core :as json]
            [taoensso.timbre :refer [debug info warn error fatal]]

            [warmagnet.persona :as persona]
            [warmagnet.db :as db]
            [warmagnet.games :as games]
            ))

;;; user management
(def all-users (atom {}))

(defn find-user [id]
  (@all-users id))

(defn get-user-id [state]
  (get-in @state [:user :id]))

(defn have-user [state]
  (not (nil? (:user @state))))

(defn add-user [state]
  (swap! all-users assoc (get-user-id state) state))

(defn remove-user [state]
  (swap! all-users dissoc (get-user-id state)))

(defn init-state [state]
  {:conn state :user nil :games #{}})

;;; helpers
(defn log-message [prefix state text]
  (if (have-user state)
    (debug (format "%s [%s] %s" prefix (get-in @state [:user :email]) text))
    (debug (format "%s %s" prefix text))))

(defn send-raw [state msg]
  (let [text (json/encode msg)]
    (log-message "<<<" state text)
    (hk/send! (:conn @state) text)))

(defn send-message [state & more]
  (send-raw state (apply hash-map more)))

(defn game-broadcast [game-id msg]
  (println "BCAST" game-id (games/get-watchers game-id))
  (doseq [id (games/get-watchers game-id)]
    (send-raw (find-user id) msg)))

(defn serialize-game-state [game-state]
  ;; TODO: Fix me
  (select-keys game-state
               [:id :options :log :players :districts :player-state :turn-by]))

;;; games
(defn watch-game [state game-state]
  (let [user-id (get-user-id state)
        game-id (:id game-state)]
    (swap! state update-in [:games] conj game-id)
    (games/add-watcher game-id user-id)
    (send-message state :type "game-state" :data (serialize-game-state game-state))))

(defn unwatch-game [state game-id]
  (games/remove-watcher game-id (get-user-id state)))

(defn join-game [state game-state]
  (let [user (:user @state)
        game-id (:id game-state)]
    (watch-game state game-state)
    (games/add-player game-id user)))

(defn send-joined-games [state]
  (let [games (db/get-user-games (get-user-id state))]
    (doseq [game games]
      (watch-game state (games/get-game (:game_id game))))))

;;; Message Handlers
(defn goc-user-by-token [token]
  (if-let [user (db/get-user-by-token token)]
    user
    (let [user-data (persona/login token)]
      (if (= "okay" (:status user-data))
        (db/get-or-create-user (:email user-data) token)))))

(defn msg-user [state msg]
  ;; TODO: Token decoding to get user id
  (if-let [token (:token msg)]
    (if-let [user (goc-user-by-token token)]
      (do (swap! state assoc :user user)
          (add-user state)
          (send-message state :type "login" :status "success" :data user)
          (send-joined-games state))
      (send-message state :type "login-error" :status "invalid-token"))
    (send-message state :type "login-error" :status "invalid-request")))

(defn msg-update-user [state {:keys [data]}]
  (let [profile (select-keys data [:name])]
    (db/update-user (get-user-id state) profile)
    (swap! state update-in [:user] merge profile)
    (send-message state :type :update-user :data profile)))

(defn msg-start-game [state {:keys [data] :as msg}]
  (let [game-state (games/create-game data)]
    (join-game state game-state)))

(defn msg-join-game [state {:keys [game-id] :as msg}]
  (let [game-state (games/get-game game-id)
        user-id (get-user-id state)]
    (if game-state
      (if (games/can-join-game game-id user-id)
        (join-game state game-state)
        (send-message state :type :join-error :status "already-joined"))
      (send-message state :type :join-error :status "invalid-game"))))

(defn msg-watch-game [state msg]
  (let [game-state (games/get-game (:game-id msg))]
    (watch-game state game-state)))

(defn msg-game-list [state msg]
  (let [data (db/get-game-list)]
    (send-message state :type "game-list" :data data)))

(defn msg-game [state {:keys [game-id data] :as msg}]
  (if-let [data (games/preprocess-game-log-item game-id data (get-user-id state))]
    (games/apply-log-item game-id data)
    (send-message state :type "error" :status "invalid-game-request")))

(defn msg-ping [state msg]
  (send-message state :type "pong"))

(defn msg-unknown [state]
  (send-message state :type "error" :status "invalid-msg"))

;;; handlers
(defn handle-user-msg [state {:keys [type] :as msg}]
  (case (keyword type)
    :ping (msg-ping state msg)
    :update-user (msg-update-user state msg)
    :start-game (msg-start-game state msg)
    :join-game (msg-join-game state msg)
    :watch-game (msg-watch-game state msg)
    :game-list (msg-game-list state msg)
    :game (msg-game state msg)
    (msg-unknown state)))

(defn handle-anonymous-msg [state {:keys [type] :as msg}]
  (case (keyword type)
    :login (msg-user state msg)
    (msg-unknown state)))

(defn handle-msg [state raw-msg]
  (let [msg (json/decode raw-msg true)]
    (log-message ">>>" state raw-msg)
    (if (have-user state)
      (handle-user-msg state msg)
      (handle-anonymous-msg state msg))))

(defn handle-close [state]
  (if (have-user state)
    (remove-user state))
  (if-let [game-ids (seq (:games @state))]
    (doseq [game-id game-ids]
      (unwatch-game state game-id))))

;;; Websocket handler
(defn ws-handler [req]
  (hk/with-channel req chan
    (let [state (atom (init-state chan))]
      (hk/on-receive chan (fn [msg] (handle-msg state msg)))
      (hk/on-close chan (fn [status] (handle-close state))))))
