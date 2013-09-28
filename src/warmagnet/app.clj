(ns warmagnet.app
  (:require [org.httpkit.server :as hk]
            [cheshire.core :as json]
            [taoensso.timbre :refer [debug info warn error fatal]]
            ))

; User management
(def all-users (atom {}))

(defn add-user [state]
  (swap! all-users assoc (:user-id @state) state)
  (println "add" all-users))

(defn remove-user [state]
  (swap! all-users dissoc (:user-id @state))
  (println "del" all-users))

; Helpers
(defn send-answer [state & more]
  (let [data (json/encode (apply hash-map more))]
    (hk/send! (:conn @state) data)))

; Message Handlers
(defn msg-user [state msg]
  ; TODO: Token decoding to get user id
  (if-let [token (:token msg)]
    (do
      (swap! state assoc :user-id token)
      (add-user state)
      (send-answer state :msg "login" :status "success"))
    (send-answer state :msg "login" :status "invalid-user")))

(defn msg-ping [state msg]
  (let [answer (json/encode {:msg "pong" :text (:text msg)})]
    (hk/send! (:conn @state) answer)))

(defn msg-unknown [state]
  (let [answer (json/encode {:msg "error" :status "invalid-msg"})]
    (hk/send! (:conn @state) answer)))

; State management
(defn init-state [state]
    {:conn state :user-id nil})

(defn handle-msg [state raw-msg]
  (let [msg (json/decode raw-msg true)]
    (case (:msg msg)
      ; Packet handlers
      "user" (msg-user state msg)
      "ping" (msg-ping state msg)
      ; Default handler
      (msg-unknown state))))

(defn handle-close [state]
  (if-not (nil? (:user-id @state))
   (remove-user state)))

; Websocket handler
(defn ws-handler [req]
  (hk/with-channel req chan
    (let [state (atom (init-state chan))]
      (hk/on-receive chan (fn [msg] (handle-msg state msg)))
      (hk/on-close chan (fn [status] (handle-close state))))))
