(ns warmagnet.permacookie
  (:import java.util.Date)
  (:require [clj-time [core :as t]
                      [format :as f]
                      [coerce :as coerce]]))

(defn cookie-date-format
  [date]
  (let [date (if (instance? Date date)
               (coerce/from-date date)
               date)]
    (.print (f/formatters :rfc822) date)))

(defn permacookie
  [handler cookie-name & {:keys [expires path]
                          :or {expires nil, path "/"}}]
  (fn [request]
    (let [resp (handler request)
          old-cookie (get-in request [:cookies cookie-name])
          new-cookie (get-in resp [:cookies cookie-name])
          cookie (or new-cookie old-cookie)
          expires (cookie-date-format
                   (or expires (-> 1 t/months t/from-now)))]
      (if (and cookie
               (not= (cookie :expires) expires))
        (let [nc (-> cookie
                      (assoc :expires expires)
                      (assoc :path path))
              resp (assoc-in resp [:cookies cookie-name] nc)]
          resp)
        resp))))
