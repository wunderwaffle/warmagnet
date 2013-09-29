(ns warmagnet.utils
  (:require
   [cheshire.core :as json]
   [taoensso.timbre :as timbre
    :refer (trace debug info warn error fatal spy with-log-level)]))

(defn load-json [filename]
  (json/decode (slurp filename) true))

(defn wrap-logging [handler]
  (fn [req]
    (let [resp (handler req)]
      (debug (str (if (:websocket? req) "WS req: " "req: ") (:uri req) " " (:status resp)))
      resp)))
