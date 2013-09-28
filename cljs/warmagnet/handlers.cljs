(ns warmagnet.handlers
  (:require
   [warmagnet.utils :refer [send-message-srv]]))

(defn new-game [e]
  (do
    (.preventDefault e)))

(defn persona-sign-in []
  (.request navigator/id))

(defn login [token]
  (send-message-srv {:type "login" :token token}))

(defn logout [a b c]
  (.log js/console "logout"))

