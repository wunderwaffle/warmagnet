(ns warmagnet.handlers
  (:require
   [warmagnet.utils :refer [send-message-srv]]))

(defn new-game [e state]
  (.preventDefault e)
  (send-message-srv {:type :start-game
                     :data state}))

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

