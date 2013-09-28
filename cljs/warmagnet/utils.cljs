(ns warmagnet.utils
  (:require
   [warmagnet.api :refer [ws]]))

(defn send-message [message]
  (swap! world world-transition message))

(defn send-message-srv [msg]
  (.send ws (.stringify js/JSON (clj->js msg))))

(defn setup-auth
  [logged-in-user login logout]
  (.watch navigator/id
          (clj->js
           {:loggedInUser nil
            :onlogin login
            :onlogout logout})))
