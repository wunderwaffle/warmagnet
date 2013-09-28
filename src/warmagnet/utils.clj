(ns warmagnet.utils
  (:require
   [taoensso.timbre :as timbre
    :refer (trace debug info warn error fatal spy with-log-level)]))

(defn wrap-logging [handler]
  (fn [req]
    (let [resp (handler req)]
      (debug (str (if (:websocket? req) "WS req: " "req: ")
                  (:uri req) " " (:status resp)))
      resp)))
