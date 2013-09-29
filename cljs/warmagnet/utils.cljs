(ns warmagnet.utils
  (:require [clojure.string :as string]
            [warmagnet.world :refer [world world-transition]]))

(defn log [& args]
  (.apply (.-log js/console) js/console
          (into-array (map clj->js args))))

(defn send-message [message]
  (swap! world world-transition message))

(defn send-message-srv [msg]
  (let [out (.stringify js/JSON (clj->js msg))]
    (log (str "-> " out))
    (.send js/ws out)))

(defn setup-auth
  [logged-in-user login logout]
  (.watch navigator/id
          (clj->js
           {:loggedInUser nil
            :onlogin login
            :onlogout logout})))
