(ns warmagnet.handlers
  (:require
   [warmagnet.utils :refer [send-message-srv]]))

(defn new-game [e]
  (do
    (.preventDefault e)))

(defn persona-sign-in []
  (.request navigator/id))

(defn persona-sign-out []
  (.logout navigator/id))

(defn save-prefs [e]
    (.preventDefault e))

(defn login [token]
  (send-message-srv {:type "login" :token token}))

(defn logout []
  (.log js/console "logout"))

