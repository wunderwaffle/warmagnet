(ns warmagnet.utils)

(defn send-message [message]
  (swap! world world-transition message))

(defn send-message-srv [msg]
  (.send ws (clj->cljson msg)))

(defn setup-auth
  [logged-in-user login logout]
  (.watch navigator/id
          (clj->js
           {:loggedInUser nil
            :onlogin login
            :onlogout logout})))
