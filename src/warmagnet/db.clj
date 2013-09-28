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

(sql/defentity games
  (sql/entity-fields
   :data))

(sql/defentity gamelogs
  (sql/entity-fields
   :game_id :type :data))

;; users
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

;; games
(defn new-game [data]
  (sql/insert games (sql/values {:data data})))

(defn add-game-log [game-id data]
  (let [type (:type data)]
    (sql/insert gamelogs (sql/values {:game_id game-id :type data :data data}))))

(defn get-game [id]
(->
  (sql/select games
              (sql/where (= :id id)))
  first))

(defn get-game-logs [id]
  (sql/select gamelogs
              (sql/where (= :game_id id))
              (sql/order :id :ASC)))

