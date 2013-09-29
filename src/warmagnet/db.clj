(ns warmagnet.db
  (:require [clojure.string :as s]
            [cheshire.core :as json]
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
(defn new-game [data]
  (let [game (sql/insert games (sql/values {:data (json/encode data) :size (:size data) :players 0}))]
    (assoc game :data data)))

(defn add-game-log [game-id data]
  (sql/insert gamelogs
              (sql/values {:game_id game-id :type (:type data) :data (json/encode data)})))

(defn get-game [id]
  (let [game (first (sql/select games
                    (sql/where (= :id id))))]
    (if-not (nil? game)
      (update-in game [:data] json/decode true))))

(defn update-game-data [game-id data]
  (sql/update games
              (sql/set-fields {:data (json/encode data)})
              (sql/where (= :id game-id))))

(defn finish-game [game-id user-id]
  (sql/update games
              (sql/set-fields {:finished true :winner user-id})
              (sql/where (= :id game-id))))

(defn get-game-players [game-id]
  (sql/select user_games
              (sql/fields [:user_id :id] :users.name)
              (sql/join users (= :users.id :user_id))
              (sql/where (= :game_id game-id))))

; TODO: Fix me
(defn -update-game-item [item]
  (assoc item :data (json/decode (:data item) true)
              :player-list (get-game-players (:id item))))

(defn get-game-list []
  (let [games (sql/select games
                          (sql/fields :id :name :players :data)
                          (sql/where (not= :finished true)))]
    (mapv -update-game-item games)))

(defn get-game-log [id]
  (let [gamelogs (sql/select gamelogs
                             (sql/fields :data)
                             (sql/where (= :game_id id))
                             (sql/order :id :ASC))]
    (mapv #(json/decode (:data %) true) gamelogs)))

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
