(ns warmagnet.games
  (:require [warmagnet.db :as db]
            ))

(def all-games (atom {}))

;; internal api
(defn process-game-state [game game-logs]
	{:game game :logs game-logs})

(defn get-game-state [game]
	(let [logs (db/get-game-logs (:id game))]
		(process-game-state game logs)))

;; public api
(defn create-game [data]
	(let [game (db/new-game data)]
		(get-game-state game)))

(defn load-game [id]
	(let [game (db/get-game id)]
		(if-not (nil? game)
			(swap! all-games assoc id (get-game-state game)))
		game))

(defn get-game [id]
	(if-let [game (id @all-games)]
		game
		(load-game id)))
