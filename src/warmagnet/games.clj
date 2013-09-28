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
	 :log game-log})

(defn get-game-state [game]
	(let [game-log (db/get-game-log (:id game))]
		(process-game-state game game-log)))

;; public api
(defn create-game [data]
	(let [game (db/new-game (json/encode data))]
		(process-game-state game [])))

(defn load-game [id]
	(let [game (db/get-game id)]
		(if-not (nil? game)
			(swap! all-games assoc id (get-game-state game))
			game)))

(defn get-game [id]
	(println "x" @all-games id)
	(let [game (@all-games id)]
		(if (nil? game)
			(load-game id)
			game)))
