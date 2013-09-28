(ns warmagnet.handlers)

(defn new-game [e]
  (do
    (.preventDefault e)))

(defn persona-sign-in []
  (.request navigator/id))

(defn setup-auth []
  (.watch navigator/id
          (clj->js
           {:loggedInUser nil
            :onlogin login
            :onlogout logout})))

(defn login [a b c]
  (.log js/console "login" a b c))

(defn logout [a b c]
  (.log js/console "logout"))

