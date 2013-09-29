(ns warmagnet.db
  (:require [clojure.string :as s]
            [korma.db :refer [defdb postgres]]
            [korma.core :as sql]
            [korma.sql.engine :refer [infix]]))

;; functions
(defn sql-inc [k v]
  (infix k "+" v))

;; db
(def dbspec (postgres {:db "warmagnet"}))
(defdb db dbspec)

;; tables
(sql/defentity users
  (sql/entity-fields
   :name :email :token))

(sql/defentity games
  (sql/entity-fields
   :data :size :players))

(sql/defentity gamelogs
  (sql/entity-fields
   :game_id :type :data))

(sql/defentity user_games
  (sql/entity-fields
   :game_id :user_id))


;; users
(defn new-user [email token]
  (sql/insert users (sql/values {:email email :token token})))

(defn get-user [email]
  (->
   (sql/select users
               (sql/where (= :email email)))
   first
   (dissoc :token)))

(defn get-user-by-token [token]
  (->
   (sql/select users
               (sql/where (= :token token)))
   first
   (dissoc :token)))

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

(defn get-or-create-user [email token]
  (let [user (get-user email)]
    (if (nil? user)
      (new-user email token)
      (do
        (if (not= (:token user) token)
          (update-user (:id user) {:token token}))
        user))))

;; games
(defn new-game [data size]
  (sql/insert games (sql/values {:data data :size size :players 0})))

(defn add-game-log [game-id type data]
  (sql/insert gamelogs
              (sql/values {:game_id game-id :type type :data data})))

(defn get-game [id]
(->
  (sql/select games
              (sql/where (= :id id)))
  first))

(defn get-game-list []
  (sql/select games
              (sql/fields :id :name :size :players)))

(defn get-game-log [id]
  (sql/select gamelogs
              (sql/where (= :game_id id))
              (sql/order :id :ASC)))

(defn add-user-to-game [game-id user-id]
  (sql/insert user_games
              (sql/values {:user_id user-id :game_id game-id}))
  (sql/update games
              (sql/set-fields {:players (sql-inc :players 1)})
              (sql/where (= :id game-id))))

(defn is-user-in-game [game-id user-id]
  (->
    (sql/select user_games
               (sql/aggregate (count :*) :count)
               (sql/where {:game_id game-id :user_id user-id}))
    first
    :count
    pos?))

(defn get-user-games [user-id]
  (sql/select user_games
              (sql/where (= :user_id user-id))))
