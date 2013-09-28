(ns warmagnet.main
  (:gen-class)
  (:require [clojure.tools.cli              :refer [cli]]
            [org.httpkit.server             :as hk]
            [ring.middleware.reload         :as reload]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.util.response             :refer [resource-response]]
            [compojure.core                 :refer [defroutes GET POST]]
            [compojure.handler              :as handler]
            [compojure.route                :as route]
            [taoensso.timbre                :refer [info]]

            [warmagnet.permacookie :refer [permacookie]]
            [warmagnet.utils       :refer [wrap-logging]]
            [warmagnet.app         :refer [ws-handler]]))

(defroutes app-routes
  (GET "/" [] (resource-response "index.html"))
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
      (permacookie "ring-session")
      (wrap-logging)))

(defn -main
  [& args]
  (let [[opts args help]
        (cli args
             ["-p" "--port" "Listen on this port" :default 3000 :parse-fn #(Integer. %)]
             ["-i" "--ip" "IP to listen" :default "127.0.0.1"]
             ["-d" "--dev" "Development mode (auto-reload)" :flag true])
        app (if (:dev opts) (reload/wrap-reload app) app)]

    (info (if (:dev opts) "Development mode" "Production mode"))
    (info "Starting to listen on " (:port opts))
    (hk/run-server app (select-keys opts [:port :ip]))))
