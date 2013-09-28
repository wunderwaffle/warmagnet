(ns warmagnet.games
  (:require
            [cheshire.core :as json]

  			[warmagnet.db :as db]
            ))

(def all-games (atom {}))

;; internal api
(defn process-game-state [game game-log]
	{:id (:id game)
	 :options (json/decode (:data game) true)
	 :log (map #(json/decode (:data %) true) game-log)
	 :watchers #{}})

(defn get-game-state [game]
	(let [game-log (db/get-game-log (:id game))]
		(process-game-state game game-log)))

(defn make-join-log-item [user]
	{:type "join" :user-id (:id user) :user-name (:name user)})

(defn add-log [id data]
	(println "add >" id data)
	(db/add-game-log id (:type data) (json/encode data))
	(swap! all-games update-in [id :log] conj data))

;; public api
(defn create-game [data]
	(let [game (db/new-game (json/encode data))]
		(process-game-state game [])))

(defn load-game [id]
	(let [game (db/get-game id)]
		(if-not (nil? game)
			(let [game-state (get-game-state game)]
				(swap! all-games assoc id game-state)
				game-state))))

(defn get-game [id]
	(let [game (@all-games id)]
		(if (nil? game)
			(load-game id)
			game)))

;; watchers
(defn add-watcher [id user-id]
	(swap! all-games update-in [id :watchers] conj user-id)
	(println "games >" @all-games))

(defn remove-watcher [id user-id]
	(swap! all-games update-in [id :watchers] disj user-id))

(defn get-watchers [id]
	(get-in @all-games [id :watchers]))
