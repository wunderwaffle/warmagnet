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
(defn new-user [email]
  (sql/insert users (sql/values {:email email})))

(defn get-user [email]
  (->
   (sql/select users
               (sql/where (= :email email)))
   first))

(defn update-user [id profile]
  (if-not (empty? profile)
    (sql/update users
            (sql/set-fields profile)
            (sql/where (= :id id)))))

(defn user-exists [email]
  (->
   (sql/select users
               (sql/aggregate (count :*) :count)
               (sql/where (= :email email)))
   first
   :count
   pos?))

(defn get-or-create-user [email]
  (let [user (get-user email)]
    (if (nil? user)
      (new-user email)
      user)))

