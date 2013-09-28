(ns warmagnet.main
  (:gen-class)
  (:require [org.httpkit.server  :as hk]
            [ring.middleware.reload :as reload]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.util.response :refer [file-response]]
            [compojure.core      :refer [defroutes GET POST]]
            [compojure.handler   :as handler]
            [compojure.route     :as route]
            [taoensso.timbre :refer [info]]

            [warmagnet.permacookie :refer [permacookie]]
            [warmagnet.utils     :refer [wrap-logging]]
            [warmagnet.app       :refer [ws-handler]]))

(defroutes app-routes
  (GET "/" [] (file-response "resources/index.html"))
  (GET "/ws" [] ws-handler)
  ;; (GET "/games" [] (file-response "resources/games.html"))
  ;; (GET "/games/new" [] (file-response "resources/new_game.html"))
  ;; (GET "/games/placeholder" [] (file-response "resources/game_mockup.html"))
  ;; (GET "/leaderboard" [] (file-response "resources/leaderboard.html"))
  (route/resources "/static" { :root "static"})
  (route/not-found "404 Nothing to see here, move along"))

(def app
  (-> #'app-routes
      (handler/site {:session {:store (cookie-store {:key "TOP SECRET"})}})
      (reload/wrap-reload)
      (permacookie "ring-session")
      (wrap-logging)))

(defn -main
  [& args]
  (info "Starting to listen on :3000...")
  (hk/run-server app {:port 3000}))
