(ns warmagnet.db
  (:require [clojure.string :as s]
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

(sql/defentity user_games
  (sql/entity-fields
   :game_id :user_id))

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

(defn add-game-log [game-id type data]
  (sql/insert gamelogs
              (sql/values {:game_id game-id :type type :data data})))

(defn get-game [id]
(->
  (sql/select games
              (sql/where (= :id id)))
  first))

(defn get-game-log [id]
  (sql/select gamelogs
              (sql/where (= :game_id id))
              (sql/order :id :ASC)))

(defn add-user-to-game [game-id user-id]
  (sql/insert user_games
              (sql/values {:user_id user-id :game_id game-id})))

(defn get-user-games [user-id]
  (sql/select user_games
              (sql/where (= :user_id user-id))))
