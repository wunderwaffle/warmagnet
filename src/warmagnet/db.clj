(ns warmagnet.db
  (:require [clojure.string :as s]
            [clojure.tools.reader.edn :as edn]
            [korma.db :refer [defdb postgres]]
            [korma.core :as sql]))

(def dbspec (postgres {:db "warmagnet"}))
(defdb db dbspec)

(sql/defentity users
  (sql/entity-fields
   :name :email))

;; api
(defn new-user
  [data]
  (sql/insert users (sql/values data)))

(defn get-user
  [email]
  (->
   (sql/select users
               (sql/where (= :email email)))
   first))

(defn user-exists
  [email]
  (->
   (sql/select users
               (sql/aggregate (count :*) :count)
               (sql/where (= :email email)))
   first
   :count
   pos?))
